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
        var arg = HeapArg.fromString("300G");
        System.out.println(arg);

        System.out.println(JVMOption.enabled("UnlockExperimentalVMOptions"));
        System.out.println(JVMOption.enabled("UseG1GC"));
        System.out.println(JVMOption.disabled("UseG1GC"));
        System.out.println(JVMOption.assigned("G1NewSizePercent", "20"));

        ServerProcess proc = new ServerProcess("smp\\server.jar", HeapArg.fromString("2G"), HeapArg.fromString("8G"));
        System.out.println(proc.getInitCommand());
        System.out.println();

        //proc.init(false);

        System.out.println(Worker.ofFuture(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (proc.isRunning()) {
                    String command = scanner.nextLine();
                    proc.rawInput(command);
                }
            }
        }, 1000));

        String line;
        while (!(line = proc.rawOutput()).equals(ServerProcess.EOF)) {
            System.out.printf("[Server] :: %s\n", line);
        }

        throw SMPHookError.nullReference("TEST ERROR CASE");
    }

    private SMPHook() {}
}
