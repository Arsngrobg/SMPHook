package arsngrobg.smphook.discord;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import arsngrobg.smphook.annotations.NonNull;
import arsngrobg.smphook.annotations.Signed;

/**
 * <h1>Discord Webhook</h1>
 * <p>A class representing a discord webhook URL, which can send messages through POST requests using JSON payloads.</p>
 * <p>Payloads can be sent using {@link #post(String)} and returns whether the POST request was a success or not.
 * 
 * <blockqoute><pre>
 *     DiscordWebhook webhook = new DiscordWebhook("YOUR_URL_HERE");
 *     String payload = "{\"content\", \"Hello, World!\"}";
 *     boolean success = webhook.post(payload); // sends "Hello, World!" to the assigned channel on Discord
 *     System.out.println(success);             // outputs if that POST request was a success
 * </pre></blockqoute></p>
 * 
 * <p>This class is immutable and thread-safe.</p>
 * 
 * @since  1.0
 * @author Arnsgrobg
 */
public final class DiscordWebhook {
    private static final    int HTTP_TOO_MANY_REQUESTS = 429;
    private static final String REQUEST_METHOD         = "POST";
    private static final String CONTENT_TYPE           = "application/json";

    private static final String REGEX                  = "^https://discord.com/api/webhooks/\\d{19}/[a-zA-Z0-9_-]{68}$";

    private final String url;

    /**
     * <p>Initialises a DiscordWebhook instance with the {@code url}.
     *    The {@code url} specified must match the general pattern of a Discord webhook URL - or else it will throw an error.
     * </p>
     * @param url - the fully-qualified URL of the Discord webhook
     * @throws Error if the {@code url} is {@code null} OR if the {@code url} doesn't match the regular webhook pattern
     */
    public DiscordWebhook(@NonNull String url) throws Error {
        if (url == null)         throw new Error("SMPHookError: Webhook URL cannot be null.");
        if (!url.matches(REGEX)) throw new Error("SMPHookError: Invalid Webhook URL.");
        this.url = url;
    }

    /**
     * <p>Initialises a DiscordWebhook instance using the {@code id} & {@code token} to generate the URL string.</p>
     * @param    id - numeric value relating to the unique ID of the webhook (as string)
     * @param token - unique string value for authentication with Discord - should not be {@code null}
     * @throws Error if the Discord webhook URL generated from this constructor is invalid
     */
    public DiscordWebhook(@NonNull String id, @NonNull String token) throws Error {
        this(String.format("https://discord.com/api/webhooks/%s/%s", id, token));
    }

    /**
     * <i>This is the delayed version of {@link #post(String)} - essentially pausing before sending the data (helps get around rate-limiting).</i>
     * <p>Attempts to initiate a HTTPS connection with Discord's webhook service using the webhook URL bound by this DiscordWebhook instance.</p>
     * <p>This method returns {@code false} if:
     *     <ul>
     *         <li>a HTTPS connection could not be established</li>
     *         <li>if the service is unable to send the {@code payload} - this can be from <a href="https://discord.com/developers/docs/topics/rate-limits">rate-limiting</a></li>
     *         <li>if the HTTPS connection has returned any content (typically means error)</li>
     *         <li>if the {@code payload} is {@code null}</li>
     *     </ul>
     * </p>
     * 
     * <p>It is important that before sending the payload, make sure that literal strings within the JSON should be escaped.
     * 
     * <blockqoute><pre>
     *     DiscordWebhook webhook = new DiscordWebhook("YOUR_URL_HERE");
     *     final String payload = "{\"content\": \"Hello, World!\", \"username\": \"foo\"}";
     *     boolean success = webhook.post(payload);
     *     if (success) System.out.println("Payload Success!");
     *     else         System.out.println("Payload Failure!");
     * </pre></blockquote></p>
     * 
     * <p><b>Discord webhook API guide: https://birdie0.github.io/discord-webhooks-guide</b></p>
     * @param payload - a non-null JSON string containing the necessary data
     * @param delay   - the number of milliseconds the webhook will wait before sending the message
     * @return {@code true} if the payload was sent successfully, {@code false} if otherwise
     */
    public boolean post(@NonNull String payload, @Signed long delay) {
        if (delay < 0) return false;

        try { Thread.sleep(delay); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return post(payload);
    }

    /**
     * <p>Attempts to initiate a HTTPS connection with Discord's webhook service using the webhook URL bound by this DiscordWebhook instance.</p>
     * <p>This method returns {@code false} if:
     *     <ul>
     *         <li>a HTTPS connection could not be established</li>
     *         <li>if the service is unable to send the {@code payload} - this can be from <a href="https://discord.com/developers/docs/topics/rate-limits">rate-limiting</a></li>
     *         <li>if the HTTPS connection has returned any content (typically means error)</li>
     *         <li>if the {@code payload} is {@code null}</li>
     *     </ul>
     * </p>
     * 
     * <p>It is important that before sending the payload, make sure that literal strings within the JSON should be escaped.
     * 
     * <blockqoute><pre>
     *     DiscordWebhook webhook = new DiscordWebhook("YOUR_URL_HERE");
     *     final String payload = "{\"content\": \"Hello, World!\", \"username\": \"foo\"}";
     *     boolean success = webhook.post(payload);
     *     if (success) System.out.println("Payload Success!");
     *     else         System.out.println("Payload Failure!");
     * </pre></blockquote></p>
     * 
     * <p><b>Discord webhook API guide: https://birdie0.github.io/discord-webhooks-guide</b></p>
     * 
     * <p><b>This method naturally handles rate-limiting.</b></p>
     * @param payload - a non-null JSON string containing the necessary data
     * @return {@code true} if the payload was sent successfully, {@code false} if otherwise
     */
    public boolean post(@NonNull String payload) {
        if (payload == null) return false;

        payload = payload.replaceAll("[\\s|\\n]+", "");
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
            if (response == HTTP_TOO_MANY_REQUESTS) {
                String retryAfterField = connection.getHeaderField("Retry-After");
                long newDelay = Long.parseLong(retryAfterField);
                return post(payload, newDelay);
            }

            return response == HttpURLConnection.HTTP_NO_CONTENT;
        } catch (IOException | URISyntaxException ignored) { return false; } // URISyntaxException shouldn't happen
    }

    /** @return the Discord webhook URL that this DiscordWebhook instance is bound by */
    public String getUrl() {
        return url;
    }

    /** @return the numeric value relating to the unique ID of the webhook (as string) */
    public String getId() {
        return url.substring(33, 51);
    }

    /** @return the unique string value for the authentication with Discord */
    public String getToken() {
        return url.substring(53, url.length() - 1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getToken());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) return false;
        if (obj == this) return true;
        DiscordWebhook asDiscordWebhook = (DiscordWebhook) obj;
        return getToken().equals(asDiscordWebhook.getToken()) && getId().equals(asDiscordWebhook.getId());
    }

    @Override
    public String toString() {
        return String.format("DiscordWebhook[ID: %s, Token: %s]", getId(), getToken());
    }
}
