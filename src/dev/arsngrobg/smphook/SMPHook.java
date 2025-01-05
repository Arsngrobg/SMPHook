package dev.arsngrobg.smphook;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;

import dev.arsngrobg.smphook.core.ServerProcess;

/**
 * <p>The main program.</p>
 * 
 * @author Arnsgrobg
 * @since  1.0
 */
public final class SMPHook {
    /** <p>The major version of SMPHook, is incremented whenever major features are introduced into the application.</p> */
    public static final int VERSION_MAJOR = 1;

    /** <p>The minor version of SMPHook, is incremented whenever minor changes and/or bug fixes are introduced into the application.</p> */
    public static final int VERSION_MINOR = 0;

    private static final List<Thread> workers = new ArrayList<>();

    static {
        SMPHook.doOnExit(() -> workers.forEach(t -> t.interrupt()));
    }

    /**
     * <p>Performs the {@code task} on JVM shutdown.
     *    You can add more than one task
     * </p>
     * @param exitTask - the task to perform on exit
     */
    public static void doOnExit(Runnable exitTask) {
        if (exitTask == null) return;

        Thread thread = new Thread(exitTask);
        Runtime.getRuntime().addShutdownHook(thread);
    }

    /**
     * <p>Assigns a worker thread to perform the specific {@code task}.</p>
     * @param task - the task to assign to a worker
     */
    public static void assignWorkerTo(Runnable task) {
        if (task == null) return;

        String workerID = String.format("Worker#%d", workers.size() + 1);
        Thread thread = Thread.ofVirtual().name(workerID).unstarted(task);
        thread.setDaemon(true);
        thread.start();

        workers.add(thread);
    }

    /**
     * <p>Hooks to the given server {@code proc}, and initialises the TUI.</p>
     * @param proc - the server process to hook onto
     */
    public static void hookTo(ServerProcess proc) {
        if (!proc.isRunning()) proc.init(true);

        SMPHook.doOnExit(() -> System.out.print("\033[0m\033[2J\033[H"));
        SMPHook.doOnExit(proc::stop);

        Runnable inputTask = () -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (proc.isRunning()) {
                    String input = scanner.nextLine().trim();

                    System.out.print("\033[A");

                    boolean success = proc.rawInput(input);

                    if (input.equals("stop") && success) {
                        break;
                    }
                }
            } catch (NoSuchElementException ignored) {} // usually caused by CTRL+C
        };
        SMPHook.assignWorkerTo(inputTask);

        System.out.print("\033[48;2;15;15;15m\033[2J\033[H");

        Optional<String> output;
        while ((output = proc.rawOutput()).isPresent()) {
            System.out.printf("\033[48;2;15;15;15m\033[0G\033[A\033[K\033[38;2;50;168;82m[Server]\033[97m :: %s\n", output.get());
            System.out.printf("====SMP Hook v%s=====================================================================\n", getVersion());
            System.out.print(">>> ");
        }
    }

    /**
     * <p>Retrieves the string representation of the version as a string, consisting of the major and minor version numbers.</p>
     * <p>The version is returned in the format of <i>[major]</i>.<i>[minor]</i>.</p>
     * 
     * @return the version of this SMPHook instance as <i>[major]</i>.<i>[minor]</i>
     * @since  1.0
     */
    public static String getVersion() {
        return String.format("%d.%d", VERSION_MAJOR, VERSION_MINOR);
    }

    public static void main(String[] args) {
        var proc = new ServerProcess("smp\\server.jar", null, null);
        SMPHook.hookTo(proc);
    }
}
