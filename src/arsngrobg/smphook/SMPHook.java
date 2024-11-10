package arsngrobg.smphook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import arsngrobg.smphook.discord.IPv4;
import arsngrobg.smphook.server.HeapArg;
import arsngrobg.smphook.server.Server;
import arsngrobg.smphook.server.HeapArg.Unit;
import arsngrobg.smphook.server.types.WebhookURL;

/** Utility class for ease-of-use functionality for the SMPHook. */
public final class SMPHook {
    private static Server     instance;
    private static WebhookURL webhookURL;

    public static String time() {
        LocalDateTime time = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return time.format(formatter);
    }

    /**
     * Takes in the {@code argStr} parameter and parse it as if it was a JVM heap argument (e.g. 3G or 112M).
     * The HeapArg object returned is never {@code null}, if the {@code argStr} is invalid, the program will throw an error.
     * @param argStr - the string that is to be parsed
     * @return a {@link HeapArg} object if the {@code argStr} parameter is a valid JVM heap argument
     */
    public static HeapArg heapArg(String argStr) {
        if (argStr == null) throw new Error("nullptr");

        char lastChar = argStr.charAt(argStr.length() - 1);
        Unit unit = null;
        for (Unit u : Unit.values()) {
            unit = u.suffix == lastChar ? u : null;
        }

        if (unit == null) throw new Error("Invalid unit suffix.");

        try {
            String sizePortion = argStr.substring(0, argStr.length() - 1);
            long size = Long.parseLong(sizePortion);
    
            return new HeapArg(size, unit);
        } catch (NumberFormatException ignored) { throw new Error("Invalid heap argument size."); }
    }

    /**
     * Grabs the IPv4 address of the device using a curl command.
     * @return an {@link IPv4} value.
     */
    public static IPv4 ipv4() {
        ProcessBuilder processBuilder = new ProcessBuilder("curl", "-4", "ifconfig.me");
        try {
            Process process = processBuilder.start();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String output = br.readLine();
                return new IPv4(output);
            }
        } catch (IOException ignored) { return new IPv4(null); }
    }

    /**
     * Initialises a server instance.
     * @param jarPath - the path to the server jar file
     * @param minHeap - the minimum amount of memory to allocate to the JVM
     * @param maxHeap - the maximum amount of memory to allocate to the JVM
     * @return a {@link Server} instance
     */
    public static Server server(String jarPath, HeapArg minHeap, HeapArg maxHeap) {
        if (SMPHook.instance != null) {
            throw new Error("Server instance already available.");
        }
        SMPHook.instance = new Server(jarPath, minHeap, maxHeap);
        return SMPHook.instance;
    }

    /**
     * Gets the currently active instance of the Minecraft server.
     * If no such exists, then it will throw an error.
     * @return the active {@link Server} instance
     */
    public static Server server() {
        if (SMPHook.instance == null) {
            throw new Error("No server instance currently available.");
        }
        return SMPHook.instance;
    }

    /**
     * Initialises a new instance of a {@link WebhookURL}, the {@code url} string specified must be a valid Discord webhook URL.
     * @param url - the raw string consisting of the Discord webhook URL
     * @return the new instance of {@link WebhookURL}
     */
    public static WebhookURL webhook(String url) {
        SMPHook.webhookURL = new WebhookURL(url);
        return SMPHook.webhookURL;
    }

    /**
     * Returns the most recent {@link WebhookURL} instance.
     * If no instance was created, an error is thrown
     * @return the most recent {@link WebhookURL} instance.
     */
    public static WebhookURL webhook() {
        if (SMPHook.webhookURL == null) {
            throw new Error("No instance of webhook has been recently initialised.");
        }
        return SMPHook.webhookURL;
    }

    private SMPHook() { throw new UnsupportedOperationException("Utility Class."); }

    public static void main(String[] args) throws Exception {
        server("smp\\server.jar", heapArg("2G"), heapArg("16G"));
        webhook("https://discord.com/api/webhooks/1240452167006158878/HsAYwcN7fv3Zhyxs2FtFiR-nZnltmOlYAiH0_pa5IevwuiF54OuUgQJ_nRbsoR_KKVlU");

        webhook().post(String.format("{\"content\":\"*IP: %s*\"}",   ipv4().getValue()));

        String line;
        while ( (line = server().rawOutput()) != null ) {
            System.out.println(String.format("\033[92m[Server@%s] \033[90m %s\033[0m", time(), line));
            System.out.println(String.format("\033[35m[Webhook@%s]\033[1;37m \033[90msent message: \"%s\"\033[0m", time(), line));
            webhook().post(String.format("{\"content\":\"```%s```\"}", line));
        }
        System.out.println("\033[92m[Server terminated successfully]\033[0m");
    }
}
