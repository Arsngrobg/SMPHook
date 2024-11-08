package arsngrobg.smphook.discord;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import arsngrobg.smphook.server.types.WebhookURL;

public final class DiscordWebhook {
    private static final String REQUEST_METHOD   = "POST";
    private static final String REQUEST_PROPERTY = "application/json";

    private static boolean send(String json, WebhookURL url) {
        if (url == null) throw new Error("nullptr");

        try {
            URL httpURL = new URI(url.getValue()).toURL();
            HttpURLConnection connection = (HttpURLConnection) httpURL.openConnection();

            connection.setRequestMethod(REQUEST_METHOD);
            connection.setRequestProperty("Content-Type", REQUEST_PROPERTY);
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
                os.write(bytes);
            }

            int response = connection.getResponseCode();
            if (response == HttpURLConnection.HTTP_NO_CONTENT) {
                return true;
            } else return false;
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean sendMessage(String message, WebhookURL url) {
        String json = String.format("{\"content\":\"%s\"}", message);
        return send(json, url);
    }

    private DiscordWebhook() { throw new UnsupportedOperationException("Utility Class."); }
}
