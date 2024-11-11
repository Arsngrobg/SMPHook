package arsngrobg.smphook;

import arsngrobg.smphook.discord.IPv4;
import arsngrobg.smphook.server.HeapArg;

/** Utility class for ease-of-use functionality for the SMPHook. */
public final class SMPHook {

    private SMPHook() { throw new UnsupportedOperationException("Utility Class."); }

    public static void main(String[] args) throws Exception {
        System.out.println(HeapArg.fromString("3G"));
        System.out.println(IPv4.query());
        // server("smp\\server.jar", heapArg("2G"), heapArg("16G"));
        // webhook("https://discord.com/api/webhooks/1240452167006158878/HsAYwcN7fv3Zhyxs2FtFiR-nZnltmOlYAiH0_pa5IevwuiF54OuUgQJ_nRbsoR_KKVlU");

        // webhook().post(String.format("{\"content\":\"*IP: %s*\"}", ipv4().getValue()));

        // String line;
        // while ( (line = server().rawOutput()) != null ) {
        //     System.out.println(String.format("\033[92m[ Server@%s]\033[90m %s\033[0m", time(), line));
        //     System.out.println(String.format("\033[35m[Webhook@%s]\033[1;37m \033[90msent message: \"%s\"\033[0m", time(), line));
        //     webhook().post(String.format("{\"username\":\"Toma Sheby Sigmah\",\"content\":\"```%s```\"}", line));
        // }
        // System.out.println("\033[92m[Server terminated successfully]\033[0m");
    }
}
