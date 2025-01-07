package dev.arsngrobg.smphook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import dev.arsngrobg.smphook.core.HeapArg;
import dev.arsngrobg.smphook.core.IPv4;
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

    /** <p>The file where the configuration for SMPHook is located.</p> */
    public static final File PROPERTIES_FILE = new File("hook.properties");

    private static final AtomicBoolean shouldRestart = new AtomicBoolean(false);
    private static final boolean prettyPrint = properties().getProperty("prettyPrint").equals("true");

    private static final List<Thread> workers = new ArrayList<>();

    static {
        // fire all workers on JVM shutsdown (unemployment)
        SMPHook.doOnExit(() -> {
            workers.forEach(t -> t.interrupt());
            workers.clear();
        });

        // custom PrintStream for ANSI-escape codes
        PrintStream customPrintStream = new PrintStream(System.out) {
            @Override
            public void print(String s) {
                if (!prettyPrint) {
                    s = s.replaceAll("\\033(?:[@-Z\\\\-_]|\\[[0-?]*[ -/]*[@-~])", "");
                }
                super.print(s);
            }
        };
        System.setOut(customPrintStream);
    }

    /**
     * <p>The default {@link Properties} found in the {@code hook.properties} file.
     *    It will also output the defaults to {@code hook.properties} if the file does not exist.
     * </p>
     * @param forceReset - resets the {@code hook.properties} file back to the defaults if {@code true}
     * @return the default {@link Properties} for SMPHook
     */
    public static Properties defaultProperties(boolean forceReset) {
        Properties defaults = new Properties();
        defaults.setProperty("prettyPrint",            "true");
        defaults.setProperty("jarFile",                "");
        defaults.setProperty("minHeap",                "");
        defaults.setProperty("maxHeap",                "");
        defaults.setProperty("webhook-url",            "");
        defaults.setProperty("network-check-interval", "3600000"); // 1 hour

        if (!PROPERTIES_FILE.exists() || forceReset) {
            try (FileOutputStream fostream = new FileOutputStream(PROPERTIES_FILE)) {
                defaults.store(fostream, String.format("SMPHook Properties (v%s)", getVersion()));
            } catch (IOException e) { e.printStackTrace(); }
        }

        return defaults;
    }

    /**
     * <p>Retreives the {@link Properties} from the {@code hook.properties} file.
     *    If the file does not exist, the default properties are used instead.
     * </p>
     * @return the {@link Properties} found in {@code hook.properties}, or the defaults if unable to 
     */
    public static Properties properties() {
        Properties properties = new Properties(defaultProperties(false));

        if (PROPERTIES_FILE.exists()) {
            try (FileInputStream fistream = new FileInputStream(PROPERTIES_FILE)) {
                properties.load(fistream);
            } catch (IOException e) { e.printStackTrace(); }
        }

        return properties;
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

        int workerIdx = workers.size();

        Runnable wrapper = () -> {
            task.run();
            workers.remove(workerIdx);
        };

        String workerID = String.format("Worker#%d", workerIdx + 1);
        Thread thread = Thread.ofVirtual().name(workerID).unstarted(wrapper);
        thread.start();

        workers.add(thread);
    }

    /**
     * <p>Hooks to the given server {@code proc}, and initialises useful functionality.</p>
     * @param proc - the server process to hook onto
     */
    public static void hookTo(ServerProcess proc) {
        if (!proc.isRunning()) proc.init(false);

        shouldRestart.set(false);

        SMPHook.doOnExit(() -> System.out.print("\033[0m\033[2J\033[H"));
        SMPHook.doOnExit(proc::stop);

        var properties = properties();

        Runnable ipCheckTask = () -> {
            IPv4 lastKnown = IPv4.queryPublic().orElseThrow();
            while (proc.isRunning()) {
                try { Thread.sleep(Long.parseLong(properties.getProperty("network-check-interval"))); }
                catch (InterruptedException e) { e.printStackTrace(); }
                IPv4 current = IPv4.queryPublic().orElse(null);

                if (current != null && !current.equals(lastKnown)) {
                    shouldRestart.set(true);
                }
            }
        };
        SMPHook.assignWorkerTo(ipCheckTask);

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
            if (prettyPrint) {
                System.out.printf("====SMP Hook v%s=====================================================================\n", getVersion());
                System.out.print(">>> ");
            }
        }
    }

    /**
     * <p>Retrieves the string representation of the version as a string, consisting of the major and minor version numbers.</p>
     * <p>The version is returned in the format of <i>[major]</i>.<i>[minor]</i>.</p>
     * @return the version of this SMPHook instance as <i>[major]</i>.<i>[minor]</i>
     */
    public static String getVersion() {
        return String.format("%d.%d", VERSION_MAJOR, VERSION_MINOR);
    }

    public static void main(String[] args) {
        // setup
        if (args.length == 1 && args[0].equals("setup")) {
            defaultProperties(true);
            return;
        }

        // main control flow
        var properties = properties();
        String jarFile = properties.getProperty("jarFile");
        String minHeap = properties.getProperty("minHeap");
        String maxHeap = properties.getProperty("maxHeap");

        ServerProcess proc = new ServerProcess(
            jarFile,
            minHeap.isEmpty() ? null : HeapArg.fromString(minHeap),
            maxHeap.isEmpty() ? null : HeapArg.fromString(maxHeap)
        );
        proc.init(true);

        SMPHook.hookTo(proc);
    }
}
