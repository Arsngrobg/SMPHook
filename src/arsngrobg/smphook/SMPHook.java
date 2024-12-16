package arsngrobg.smphook;

import java.io.File;
import java.util.Scanner;

import arsngrobg.smphook.annotations.UtilityClass;
import arsngrobg.smphook.server.Server;

@UtilityClass
public final class SMPHook {
    public static final int VERSION_MAJOR = 1;
    public static final int VERSION_MINOR = 0;

    public static void hookTo(Server server) {
        if (!server.isRunning()) server.init(true);

        Thread inputThread = new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                String input;
                boolean success;
                do {
                    input = scanner.nextLine();
                    System.out.print("\033[A");
                    success = server.rawInput(input);
                } while ((!input.equals("stop") && success));
            }
        }, "ServerInputThread");
        inputThread.setDaemon(true);
        inputThread.start();

        System.out.print("\033[48;2;15;15;15m\033[2J\033[H");

        String output;
        while ((output = server.rawOutput()) != null) {
            System.out.print("\033[48;2;15;15;15m");
            System.out.printf("\033[0G\033[A\033[K\033[38;2;50;168;82m[Server]\033[97m >>> %s\n", output);
            System.out.printf("====SMP Hook v%d.%d=================================================\n", VERSION_MAJOR, VERSION_MINOR);
            System.out.print(">>> ");
        }

        System.out.print("\033[0m\033[2J\033[H");
    }

    public static void main(String[] args) {
        Server server = new Server(new File("smp\\server.jar"), null, null);
        hookTo(server);
    }
}
