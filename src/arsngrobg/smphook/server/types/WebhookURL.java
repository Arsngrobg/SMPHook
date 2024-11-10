package arsngrobg.smphook.server.types;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Wrapper class for a Discord webhook URL.
 * The internal value must strictly match the rough pattern of a Discord webhook URL.
 */
public final class WebhookURL {
    private static final String REQUEST_METHOD   = "POST";
    private static final String REQUEST_PROPERTY = "application/json";

    // the regex pattern that the value must match
    private static final String REGEX = "^https://discord.com/api/webhooks/[\\d]{19}/[\\w\\d_-]{68}$";

    private final String value;

    /**
     * Initialises a WebhookURL with the specified {@code value}.
     * @param value - a {@link String} that must match the rough pattern of a Discord webhook URL
     */
    public WebhookURL(String value) {
        if (!value.matches(REGEX)) throw new Error("Invalid Discord Webhook URL.");
        this.value = value;
    }

    /**
     * Posts to the given webhook URL the data in the specified {@code json} string.
     * @param json - webhook body represented as a JSON string
     * @return {@code true} if the body was sent, {@code false} if otherwise
     */
    public boolean post(String json) {
        try {
            URL httpURL = new URI(value).toURL();
            HttpURLConnection connection = (HttpURLConnection) httpURL.openConnection();

            connection.setRequestMethod(REQUEST_METHOD);
            connection.setRequestProperty("Content-Type", REQUEST_PROPERTY);
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
                os.write(bytes);
            }

            int response = connection.getResponseCode();
            return response == HttpURLConnection.HTTP_NO_CONTENT;
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** @return the {@link String} representation of this URL. */
    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) return false;
        if (obj == this) return true;
        WebhookURL asWebhookURL = (WebhookURL) obj;
        return value.equals(asWebhookURL.value);
    }

    @Override
    public String toString() {
        return String.format("WebhookURL[%s]", value);
    }
}
