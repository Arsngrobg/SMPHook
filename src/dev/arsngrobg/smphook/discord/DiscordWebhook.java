package dev.arsngrobg.smphook.discord;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import dev.arsngrobg.smphook.SMPHook;
import dev.arsngrobg.smphook.SMPHookError;
import static dev.arsngrobg.smphook.SMPHookError.condition;
import static dev.arsngrobg.smphook.SMPHookError.nullCondition;

/**
 * <p>The {@code DiscordWebhook} class wraps a string type that is a valid Discord Webhook URL.</p>
 * 
 * @author Arsngrobg
 * @since  1.0
 */
public final class DiscordWebhook {
    public static final String REGEX = "https://discord.com/api/webhooks/\\d{19}/[0-9A-Za-z_-]{68}";

    /**
     * <p>Parses the supplied {@code url} as if it was a valid Discord Webhook URL.</p>
     * 
     * <p>If the supplied {@code url} is not a valid Discord Webhook then this method throws an {@link SMPHookError}.</p>
     * 
     * @param url - a valid Discord Webhook URL
     * @return a new {@code DiscordWebhook} object
     * @throws SMPHookError if the supplied {@code url} is not a valid Discord Webhook URL
     */
    public static DiscordWebhook fromURL(String url) throws SMPHookError {
        SMPHookError.caseThrow(
            nullCondition(url, "url"),
            condition(() -> !url.matches(DiscordWebhook.REGEX), SMPHookError.withMessage("Supplied URL: '%s' is not a valid Discord Webhook URL."))
        );

        URL asUrlObj = SMPHookError.throwIfFail(() -> new URI(url).toURL());

        return new DiscordWebhook(asUrlObj);
    }

    /**
     * <p>HTTP response codes that a Discord webhook can return.</p>
     * 
     * <p><a href="https://discord.com/developers/docs/topics/opcodes-and-status-codes">Reference</a></p>
     */
    public static final int
        RESPONSE_OK                  = 200,
        RESPONSE_CREATED             = 201,
        RESPONSE_NO_CONTENT          = 204,
        RESPONSE_NOT_MODIFIED        = 304,
        RESPONSE_BAD_REQUEST         = 400,
        RESPONSE_UNAUTHORIZED        = 401,
        RESPONSE_FORBIDDEN           = 403,
        RESPONSE_NOT_FOUND           = 404,
        RESPONSE_METHOD_NOT_ALLOWED  = 405,
        RESPONSE_TOO_MANY_REQUESTS   = 429,
        RESPONSE_GATEWAY_UNAVAILABLE = 502;

    // constants for network requests
    private static final String
        REQUEST_TYPE         = "POST",
        REQUEST_PROPERTY     = "Content-Type",
        REQUEST_CONTENT_TYPE = "application/json";

    // constants for the bounds for subtrings of the URL
    private static final int
        ID_START    = 33,
        ID_END      = 51,
        TOKEN_START = ID_END + 2,
        TOKEN_END   = TOKEN_START + 128;

    private final URL url;
    private int lastResponse = RESPONSE_OK;

    private DiscordWebhook(URL url) {
        this.url = url;
    }

    /**
     * <p>Sends the payload via a HTTP POST request with the supplied {@code payload} JSON string.</p>
     * 
     * <p>This method will, majority of the time, return a zero'd value.
     *    However, if the server receives the {@value #RESPONSE_TOO_MANY_REQUESTS} response code from Discord then it will return a non-zero value.
     *    This value should be taken into consideration when attempting to invoke another POST request again.
     * </p>
     * 
     * @param payload - the JSON string containing the Discord webhook POST data
     * @return the amount of time to wait (in milliseconds) before you can post again
     * @throws SMPHookError if the {@code payload} is {@code null} or an empty string
     */
    public long post(String payload) throws SMPHookError {
        String _payload = SMPHookError.strictlyRequireNonNull(payload, "payload").replaceAll("[\\n\\s]+", "");
        if (_payload.isEmpty()) {
            throw SMPHookError.withMessage("JSON payload is empty.");
        }

        return SMPHookError.throwIfFail(() -> {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(REQUEST_TYPE);
            connection.setRequestProperty(REQUEST_PROPERTY, REQUEST_CONTENT_TYPE);
            connection.setDoOutput(true);
            
            try (OutputStream ostream = connection.getOutputStream()) {
                byte[] jsonBytes = _payload.getBytes(StandardCharsets.UTF_8);
                ostream.write(jsonBytes);
            }

            lastResponse = connection.getResponseCode();
            return switch (lastResponse) {
                case RESPONSE_TOO_MANY_REQUESTS -> Long.parseLong(connection.getHeaderField("Retry-After")) * 1000;
                default -> 0L;
            };
        });
    }

    /** @return the unique ID of this Discord Webhook */
    public String getID() {
        return getURL().substring(ID_START, ID_END);
    }

    /** @return the unique instance token for the given Discord Webhook */
    public String getToken() {
        return getURL().substring(TOKEN_START, TOKEN_END);
    }

    /** @return the fully-qualified Discord Webhook URL */
    public String getURL() {
        return url.toString();
    }

    /**
     * <p>The return value of this method is determined by the state after invoking the {@link #post(String)} method.</p>
     * 
     * @return the last response code of this Discord Webhook
     */
    public int getLastResponse() {
        return lastResponse;
    }

    @Override
    public int hashCode() {
        return SMPHook.hashOf(url);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof DiscordWebhook asWebhook)) return false;
        return url.equals(asWebhook.url);
    }

    @Override
    public String toString() {
        return String.format("DiscordWebhook[ID: %s, TOKEN: %s]", getID(), getToken());
    }
}
