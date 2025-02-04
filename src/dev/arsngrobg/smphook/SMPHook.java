package dev.arsngrobg.smphook;

import java.util.Scanner;

import dev.arsngrobg.smphook.concurrent.Worker;
import dev.arsngrobg.smphook.server.HeapArg;
import dev.arsngrobg.smphook.server.JVMOption;
import dev.arsngrobg.smphook.server.ServerProcess;

/**
 * <p>The main program.</p>
 * 
 * @author Arsngrobg
 * @since  1.0
 */
public final class SMPHook {
    /** <p>The major version of <i>SMPHook</i>. This is incremented whenever major features are introduced.</p> */
    public static final int VERSION_MAJOR = 1;

    /** <p>The minor version of <i>SMPHook</i>. This is incremented whenever minor (e.g. bug fixes, QOL) features are introduced.</p> */
    public static final int VERSION_MINOR = 0;

    /**
     * <p>Retrieves the string representation of the version as a string, consisting of the major and minor version numbers.</p>
     * 
     * <p>The version is returned in the format of [<i>major</i>].[<i>minor</i>].</p>
     * 
     * @return the version of this SMPHook instance as [<i>major</i>].[<i>minor</i>]
     */
    public static String getVersion() {
        return String.format("%d.%d", VERSION_MAJOR, VERSION_MINOR);
    }

    public static void main(String[] args) {
        // example server parameters
        String entryPoint = "smp\\server.jar";
        HeapArg minHeap = HeapArg.fromString("2G");
        HeapArg maxHeap = HeapArg.fromString("10G");
        JVMOption[] options = {
            JVMOption.enabled("UnlockExperimentalVMOptions"),
            JVMOption.enabled("UseG1GC"),
            JVMOption.assigned("G1NewSizePercent", "20"),
            JVMOption.assigned("G1ReservePercent", "20"),
            JVMOption.assigned("MaxGCPauseMillis", "50"),
            JVMOption.assigned("G1HeapRegionSize", "32M")
        };

        ServerProcess proc = new ServerProcess(entryPoint, minHeap, maxHeap, options);
        System.out.printf("Running Minecraft Server Process with command: %s\n", proc.getInitCommand());

        Worker w = Worker.ofWaiting(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (proc.isRunning()) {
                    String command = scanner.nextLine();
                    proc.rawInput(command);
                }
            }
        });

        proc.init(true);

        w.start();

        String line;
        while (!(line = proc.rawOutput()).equals(ServerProcess.EOF)) {
            System.out.printf("[Server] :: %s\n", line);
        }
    }

    private SMPHook() {}
}
