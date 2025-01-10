package dev.arsngrobg.smphook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import dev.arsngrobg.smphook.core.DiscordWebhook;
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
    private static final boolean prettyPrint = properties().getProperty("pretty-print").equals("true");

    /** <p>The file handle to the log file in the current SMPHook session.</p> */
    public static final File LOG_FILE = new File(String.format("logs%s%d.log", File.separator, System.currentTimeMillis()));

    private static final BlockingQueue<String> outputQueue = new ArrayBlockingQueue<>(10);
    private static final String TERMINATION_STRING = "TERMINATION";

    private static final List<Thread> workers = new ArrayList<>();

    /**
     * <p>The default {@link Properties} found in the {@code hook.properties} file.
     *    It will also output the defaults to {@code hook.properties} if the file does not exist.
     * </p>
     * @param forceReset - resets the {@code hook.properties} file back to the defaults if {@code true}
     * @return the default {@link Properties} for SMPHook
     */
    public static Properties defaultProperties(boolean forceReset) {
        Properties defaults = new Properties();
        defaults.setProperty("pretty-print",           "true");
        defaults.setProperty("jar-file",               "");
        defaults.setProperty("min-heap",               "");
        defaults.setProperty("max-heap",               "");
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
     * <p>Halts for the number of {@code millis}.</p>
     * <p>This method will log if it was unable to sleep.</p>
     * @param millis - the amount of time to sleep for
     */
    public static void sleep(long millis) {
        try { Thread.sleep(millis); }
        catch (InterruptedException ignored) { SMPHook.log("warn", "Unable to sleep for %d milliseconds.", millis); }
    }

    /**
     * <p>Performs the {@code task} on JVM shutdown.
     *    You can add more than one task.
     * </p>
     * @param task - a task to perform when on JVM shutdown
     */
    public static void doOnExit(Runnable task) {
        if (task == null) return;

        Thread thread = new Thread(task, "OnExit");
        Runtime.getRuntime().addShutdownHook(thread);

        SMPHook.log("JVM", "A shutdown hook has been provided to the JVM runtime.");
    }

    /**
     * <p>Assigns the given {@code task} and assigns a virtual thread (worker) to complete the task.
     *    On completion, the worker is let go and dereferenced.
     * </p>
     * @param task - the task for the worker to be assigned to
     */
    public static void assignWorkerTo(Runnable task) {
        if (task == null) return;

        int workerIdx = workers.size();

        Runnable wrapper = () -> {
            task.run();
            workers.set(workerIdx, null);
            SMPHook.log("workers", "Worker#%d has completed their task.", workerIdx + 1);
        };

        String workerID = String.format("Worker#%d", workerIdx + 1);
        Thread workerThread = Thread.ofVirtual().name(workerID).unstarted(wrapper);
        workers.add(workerThread);

        SMPHook.log("workers", "%s has been hired.", workerID);

        workerThread.start();
    }

    /**
     * <p>Logs the given {@code format} formatted with the {@code args} supplied to the method and tagged with the {@code category} and the current time.</p>
     * <p>The resulting string is then logged to the {@link #LOG_FILE},
     *    and then <b>"fancified"</b> if {@code prettyPrint} is enabled in the {@code hook.properties} file.
     * </p>
     * <p>This method also newlines the output string so is not required.</p>
     * @param category - the type of log
     * @param format - the format string to log 
     * @param args - the argument to be inserted into the format string (optional)
     */
    public static void log(String category, String format, Object...args) {
        String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        final Function<String, int[]> categoryColor = c -> {
            return switch (c.toLowerCase()) {
                case "warn"    -> new int[] {255, 255,   0};
                case "server"  -> new int[] {50 , 168, 82 };
                case "network" -> new int[] {0  , 122, 255};
                case "discord" -> new int[] {114, 137, 218};
                case "jvm"     -> new int[] {186, 148, 106};
                case "workers" -> new int[] {32,  178, 170};
                default        -> new int[] {255, 255, 255};
            };
        };

        int[] categoryColour = categoryColor.apply(category);

        String logOutput = String.format(
            "[%-10s] [%s] :: %s\n",
            category.toUpperCase(), timestamp, String.format(format, args)
        );

        try (FileWriter fwriter = new FileWriter(LOG_FILE, true)) {
            fwriter.write(logOutput);
        } catch (IOException e) { SMPHook.log("warn", "Unable to log the current line to %s.", LOG_FILE.getName()); }

        String consoleOutput = String.format(
            "\033[48;2;15;15;15m\033[0G\033[A\033[K\033[38;2;%d;%d;%dm%s\033[90m%s\033[97m%s",
            categoryColour[0], categoryColour[1], categoryColour[2],
            logOutput.substring(0, 12),
            logOutput.substring(12, 23),
            logOutput.substring(23)
        );
        outputQueue.offer(consoleOutput);
    }

    /**
     * <p>Hooks to the given server {@code proc}, and initialises useful functionality.</p>
     * @param proc - the server process to hook onto
     */
    public static void hookTo(ServerProcess proc) {
        if (!proc.isRunning()) proc.init(false);

        SMPHook.doOnExit(proc::stop);

        DiscordWebhook webhook = new DiscordWebhook(properties().getProperty("webhook-url"));

        AtomicBoolean shouldRestart = new AtomicBoolean(false);
        do {

            Optional<IPv4> lastKnownIp = IPv4.queryPublic();
            Runnable netStateTask = () -> {
                if (lastKnownIp.isEmpty()) { // if the network is offline - skip network task
                    SMPHook.log("network", "Your network is offline.");
                    return;
                }

                SMPHook.log("network", "Running this server on the IPv4 address: %s", lastKnownIp.get().getAddress());
                SMPHook.sleep(5000);

                while (proc.isRunning()) {
                    Optional<IPv4> current = IPv4.queryPublic();
                    current.ifPresentOrElse(ip -> {
                        if (!ip.equals(lastKnownIp.get())) {
                            SMPHook.log("network", "IPv4 address of your network has changed. Commencing restart...");
                            shouldRestart.set(true);
                            proc.stop();
                        }
                        SMPHook.sleep(Long.parseLong(properties().getProperty("network-check-interval")));
                    }, () -> {
                        SMPHook.log("network", "Network connection interrupted. Waiting for restablish.");
                        Optional<IPv4> newCurrent;
                        do {
                            SMPHook.sleep(5000);
                            newCurrent = IPv4.queryPublic();
                        } while (newCurrent.isEmpty());
                        SMPHook.log("network", "Connection restablished.");
                    });
                }
            };
            SMPHook.assignWorkerTo(netStateTask);

            Runnable procInputTask = () -> {
                try (Scanner scanner = new Scanner(System.in)) {
                    while (proc.isRunning()) {
                        String input = scanner.nextLine().trim();
                        System.out.print("\033[A");
    
                        boolean success = proc.rawInput(input);
                        if (input.equals("stop") && success) {
                            break;
                        }
                    }
                } catch (NoSuchElementException ignored) {} // caused by CTRL+C
            };
            SMPHook.assignWorkerTo(procInputTask);

            Runnable procOutputTask = () -> {
                Optional<String> procOutput;
                while ((procOutput = proc.rawOutput()).isPresent()) {
                    String sanitized = procOutput.get().replaceAll("%", "%%");
                    SMPHook.log("server", sanitized);
                }
                outputQueue.offer(TERMINATION_STRING); // can't put nulls so this'll have to do
            };
            SMPHook.assignWorkerTo(procOutputTask);

            Runnable notifyTask = () -> {
                SMPHook.sleep(15000); // give buffer time as inbetween the init and now it may not actually start the server
                webhook.post(String.format("{\"content\":\"The server is online. The IP is ```%s```\"}", lastKnownIp.get()));
            };
            SMPHook.assignWorkerTo(notifyTask);


            String line;
            try { while (!(line = outputQueue.take()).equals(TERMINATION_STRING)) {
                System.out.print(line);
                if (prettyPrint) {
                    System.out.printf("====SMP Hook v%s=======================================================================\n>>> ", getVersion());
                }
            }} catch (InterruptedException ignored) {}

        } while (shouldRestart.get());
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

        // configurations

        // creating the log file - if required
        File logDir = LOG_FILE.getParentFile();
        boolean success = logDir.mkdir();
        if (success) {
            SMPHook.log("info", "No log directory found. Creating logs directory.");
        }

        // interrupt and fire all workers on JVM shutdown (mass unemployment)
        SMPHook.doOnExit(() -> {
            workers.stream().filter(w -> w != null).forEach(w -> w.interrupt());
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

        // main control flow
        System.out.print("\033[48;2;15;15;15m\033[2J\033[H");
        System.out.printf("====SMP Hook v%s=======================================================================\n", getVersion());

        Properties prop = properties();
        String jarFile = prop.getProperty("jar-file");
        String minHeap = prop.getProperty("min-heap");
        String maxHeap = prop.getProperty("max-heap");

        ServerProcess proc = new ServerProcess(
            jarFile,
            minHeap != null ? HeapArg.fromString(minHeap) : null,
            maxHeap != null ? HeapArg.fromString(maxHeap) : null
        );
        proc.init(true);
        SMPHook.log("info", "Now initialising server with command: %s", proc.getInitCommand());

        SMPHook.hookTo(proc);
    }
}
