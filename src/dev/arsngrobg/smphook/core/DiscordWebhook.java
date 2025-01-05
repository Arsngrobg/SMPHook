package dev.arsngrobg.smphook.core;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import dev.arsngrobg.smphook.SMPHookError;
import dev.arsngrobg.smphook.SMPHookError.Type;

/**
 * <p>Wrapper class for a Discord webhook URL.</p>
 * <p>This class also provides functionality for sending JSON data through the URL.</p>
 * <p>It is important to note that a valid Discord webhook URL must be in the format: https://discord.com/api/webhooks/{webhook_id}/{webhook_token}</p>
 * 
 * @author Arsngrobg
 * @since  1.0
 */
public final class DiscordWebhook {
    // HTTP request constants
    private static final int    TOO_MANY_REQUESTS = 429;
    private static final String REQUEST_METHOD    = "POST";
    private static final String CONTENT_TYPE      = "application/json";

    private static final String REGEX = "^https://discord.com/api/webhooks/\\d{19}/[a-zA-Z0-9_-]{68}$";

    private final String url;

    /**
     * <p>Initialises a new instance of {@code DiscordWebhook} through a valid Discord webhook URL.</p>
     * @param url - the valid Discord URL
     * @throws SMPHookError if the URL is {@code null}, or does not match the expected Discord webhook URL format
     */
    public DiscordWebhook(String url) throws SMPHookError {
        if (url == null) SMPHookError.throwNullPointer("url");
        if (!url.matches(REGEX)) {
            throw SMPHookError.getErr(Type.INVALID_DISCORD_WEBHOOK_URL);
        }

        this.url = url;
    }

    /**
     * <p>Initialises a new instance of {@code DiscordWebhook} through a {@code id} and {@code token} string.</p>
     * @param id - the webhook ID
     * @param token - the webhook token
     * @throws SMPHookError if the resulting URL generated is invalid
     */
    public DiscordWebhook(String id, String token) throws SMPHookError {
        this(String.format("https://discord.com/api/webhooks/%s/%s", id, token));
    }

    /**
     * <p>Sends the JSON format {@code payload} to the Discord webhook URL through a HTTP POST request.</p>
     * <p>This method returns {@code false} if the {@code payload} is {@code null} or empty.</p>
     * <p>In the case of a rate-limit, the {@code Retry-After} header will be used to determine the next best moment to resend the request.</p>
     * @param payload - the JSON formatted string
     * @return if the POST was successful
     */
    public boolean post(String payload) {
        if (payload == null) return false;

        payload = payload.replaceAll("\\n+", "");
        if (payload.isEmpty()) return false;

        try {
            URL httpURL = new URI(url).toURL();
            HttpURLConnection connection = (HttpURLConnection) httpURL.openConnection();

            connection.setRequestMethod(REQUEST_METHOD);
            connection.setRequestProperty("Content-Type", CONTENT_TYPE);
            connection.setDoOutput(true);

            try (OutputStream ostream = connection.getOutputStream()) {
                byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
                ostream.write(bytes);
            }

            int response = connection.getResponseCode();
            if (response == TOO_MANY_REQUESTS) {
                String retryAfterField = connection.getHeaderField("Retry-After");
                long delay = Long.parseLong(retryAfterField);
                try { Thread.sleep(delay); } catch (InterruptedException e) { e.printStackTrace(); } // this could probably be done with a scheduledexecutor
                return post(payload);
            }
            return response == HttpURLConnection.HTTP_NO_CONTENT;
        } catch (IOException | URISyntaxException ignored) { return false; }
    }

    /** @return this webhook's token */
    public String getToken() {
        return url.substring(53, url.length() - 1);
    }

    /** @return this webhook's ID */
    public String getId() {
        return url.substring(33, 51);
    }

    /** @return the wrapped URL */
    public String getUrl() {
        return url;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getToken());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) return false;
        if (obj == this) return true;
        DiscordWebhook asWebhook = (DiscordWebhook) obj;
        return getToken().equals(asWebhook.getToken()) && getId().equals(asWebhook.getId());
    }

    @Override
    public String toString() {
        return String.format("DiscordWebhook[ID: %s, Token: %s]", getId(), getToken());
    }
}
