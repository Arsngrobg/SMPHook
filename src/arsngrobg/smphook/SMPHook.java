package arsngrobg.smphook;

import java.io.File;
import java.util.Scanner;

import arsngrobg.smphook.annotations.UtilityClass;
import arsngrobg.smphook.server.HeapArg;
import arsngrobg.smphook.server.Server;

@UtilityClass
public final class SMPHook {
    public static void main(String[] args) {
        HeapArg arg1  = new HeapArg(3, HeapArg.Unit.GIGABYTE);
        HeapArg arg2  = new HeapArg(8, HeapArg.Unit.GIGABYTE);
        Server server = new Server(new File("smp\\server.jar"), arg1, arg2);

        server.init(false);

        try (Scanner scanner = new Scanner(System.in)) {
            while (server.isRunning()) {
                System.out.print(">>> ");
                String command = scanner.nextLine();
                boolean success = server.rawInput(command);
                if (command.equals("stop") && success) break;
                System.out.println();
            }
        }

        // String line;
        // while ((line = server.rawOutput()) != null) {
        //     System.out.println(line);
        // }
    }
}
