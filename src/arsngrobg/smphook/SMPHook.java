package arsngrobg.smphook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;
import java.util.Scanner;

import arsngrobg.smphook.discord.WebhookURL;
import arsngrobg.smphook.network.IPv4;
import arsngrobg.smphook.server.HeapArg;
import arsngrobg.smphook.server.Server;

/** Main program */
public final class SMPHook {
    private static final String PROPERTIES_FILE_NAME = "hook.properties";
 
    /**
     * The default configuration for SMPHook.
     * @return a {@link Properties} object with the defaults
     */
    public static Properties defaultSettings() {
        Properties defaults = new Properties();
        defaults.setProperty("server.entry-point", "server.jar");
        defaults.setProperty("server.min-heap", "");
        defaults.setProperty("server.max-heap", "");
        defaults.setProperty("discord.webhookurl", "");
        return defaults;
    }

    /**
     * Attempts to load {@code hook.properties} and filter any anomalous values if present in the file.
     * If the {@code hook.properties} file fails to load, then the default values are used.
     * @return a {@link Properties} object that may contain loaded values OR the defaults
     */
    public static Properties settings() {
        Properties defaults = defaultSettings();
        Properties loadedSettings = new Properties(defaults);

        File propertiesFile = new File(PROPERTIES_FILE_NAME);

        try (FileInputStream fis = new FileInputStream(propertiesFile)) {
            loadedSettings.load(fis);
        } catch (IOException ignored) {}

        Properties settings = new Properties(defaults);
        defaults.forEach((key, _value) -> settings.setProperty((String) key, loadedSettings.getProperty((String) key)));

        try (FileOutputStream fos = new FileOutputStream(propertiesFile)) {
            settings.store(fos, "SMPHook Properties");
        } catch (IOException ignored) {}

        return settings;
    }

    public static void hookTo(Server serverInstance, WebhookURL webhook) {
        final String JSON_TEMPLATE = "{\"content\": \"%s\"}";

        // if the environment running the hook fails, the server instance will be left hanging, so kill it
        Runnable onVMShutdown = () -> {
            if (serverInstance.isRunning()) return;
            serverInstance.stop();
            webhook.post(String.format(JSON_TEMPLATE, "```Server has been killed.```"));
        };
        Runtime.getRuntime().addShutdownHook(new Thread(onVMShutdown, "VMShutdownHookThread"));

        // initialise a thread responsible for handling input into the server
        Runnable serverInputJob = () -> {
            try (Scanner scanner = new Scanner(System.in)) {
                String line;
                do {
                    line = scanner.nextLine().trim();
                    serverInstance.rawInput(line);
                } while (!line.equals("stop"));
            }
        };
        Thread serverInputThread = new Thread(serverInputJob, "ServerInputThread");
        serverInputThread.setDaemon(true);
        serverInputThread.start();

        webhook.post(String.format(JSON_TEMPLATE, String.format("```Server is online:\\n\\nIP: %s```", IPv4.query().getValue())));

        // server output takes up main thread
        final int BUFFER_CAPACITY = 40;
        Queue<String> outputBuffer = new LinkedList<>();

        StringBuilder stringBuilder = new StringBuilder();

        String line;
        while ((line = serverInstance.rawOutput()) != null) {
            outputBuffer.add(line);
            if (outputBuffer.size() > BUFFER_CAPACITY) outputBuffer.poll();

            stringBuilder.setLength(0);
            stringBuilder.append("\033[2J\n");
            outputBuffer.forEach(l -> stringBuilder.append("\033[32m[Server] \033[90m")
                                                   .append(l)
                                                   .append("\n"));
            stringBuilder.append("=============================================>")
                         .append("\n")
                         .append("\033[0m>>> ");
            System.out.print(stringBuilder.toString());
        }
    }

    public static void run() {
        Properties settings = settings();
        
        String entryPoint = settings.getProperty("server.entry-point");
        HeapArg minheap = settings.getProperty("server.min-heap").isEmpty() ? null : HeapArg.fromString(settings.getProperty("server.min-heap"));
        HeapArg maxHeap = settings.getProperty("server.max-heap").isEmpty() ? null : HeapArg.fromString(settings.getProperty("server.max-heap"));

        Server server = new Server(entryPoint, minheap, maxHeap);

        WebhookURL webhook = new WebhookURL(settings.getProperty("discord.webhookurl"));

        hookTo(server, webhook);
    }

    public static void main(String[] args) {
        run();
    }

    private static class TerminalUtils {
        private TerminalUtils() { throw new UnsupportedOperationException("Utility Class."); }
    }
}
