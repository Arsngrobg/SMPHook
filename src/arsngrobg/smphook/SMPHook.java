package arsngrobg.smphook;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import arsngrobg.smphook.annotations.NonNull;
import arsngrobg.smphook.annotations.UtilityClass;
import arsngrobg.smphook.config.Config;
import arsngrobg.smphook.server.HeapArg;
import arsngrobg.smphook.server.Server;

@UtilityClass
public final class SMPHook {
    public static final int VERSION_MAJOR = 1;
    public static final int VERSION_MINOR = 0;

    private static final List<Thread> workers = new ArrayList<>();

    // worker shutdown hook
    static {
        doOnExit(() -> workers.forEach(w -> w.interrupt()));
    }

    public static void doOnExit(@NonNull Runnable onExitTask) {
        if (onExitTask == null) return;

        Thread thread = new Thread(onExitTask);
        Runtime.getRuntime().addShutdownHook(thread);
    }

    public static void assignWorkerTo(@NonNull Runnable task) {
        if (task == null) return;

        String workerID = String.format("Worker#%d", workers.size() + 1);
        Thread thread = new Thread(task, workerID);
        thread.setDaemon(true);
        thread.start();

        workers.add(thread);
    }

    public static void hookTo(@NonNull Server server) {
        if (server == null) throw new Error("SMPHookError: server argument is null.");

        if (!server.isRunning()) server.init(true);

        SMPHook.doOnExit(() -> System.out.print("\033[0m\033[2J\033[H"));
        SMPHook.doOnExit(server::stop);

        Runnable inputTask = () -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (server.isRunning()) {
                    String input = scanner.nextLine().trim();
                    System.out.print("\033[A");
    
                    boolean success = server.rawInput(input);

                    if (input.equals("stop") && success) {
                        break;
                    }
                }
            } catch (NoSuchElementException ignored) {} // usually caused by CTRL+C
        };
        SMPHook.assignWorkerTo(inputTask);

        System.out.print("\033[48;2;15;15;15m\033[2J\033[H");

        String output;
        while ((output = server.rawOutput()) != null) {
            System.out.printf("\033[48;2;15;15;15m\033[0G\033[A\033[K\033[38;2;50;168;82m[Server]\033[97m :: %s\n", output);
            System.out.printf("====SMP Hook v%d.%d=====================================================================\n", VERSION_MAJOR, VERSION_MINOR);
            System.out.print(">>> ");
        }
    }

    public static void start() {
        Config config = Config.fromFile("hook.conf");

        String  entryPoint = config.getString("Server.entry_point");
        HeapArg minHeap    = HeapArg.fromString(config.getString("Server.min_heap"));
        HeapArg maxHeap    = HeapArg.fromString(config.getString("Server.max_heap"));

        Server server = new Server(entryPoint, minHeap, maxHeap);
        hookTo(server);
    }

    public static void setup() {
        final String defaultConfig = String.format("""
        [Server]
        entry_point = smp%sserver.jar
        min_heap    =
        max_heap    =

        [Discord]
        webhook_url = YOUR_URL_HER
        webhook_use = true
        """, File.separator);
        
        System.out.println("Generating defualt config file: hook.conf");
        Config config = Config.fromSource(defaultConfig);
        boolean success = config.export("hook.conf");
        System.out.println(success ? "Successfully exported default config file." : "Failed to export default config file.");
    }

    public static void main(String[] args) {
             if (args.length == 0)                                     start();
        else if (args.length == 1 && args[0].equals("setup")) setup();
        else throw new Error("SMPHookError: invalid executable arguments.");
    }
}
