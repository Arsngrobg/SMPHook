package arsngrobg.smphook.server.types;

// Resource: https://gist.github.com/Birdie0/78ee79402a4301b1faf412ab5f1cdcf9

/**
 * Wrapper class for a Discord webhook URL.
 * The internal value must strictly match the rough pattern of a Discord webhook URL.
 */
public final class WebhookURL {
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
        if (obj == this)                                 return true;
        WebhookURL asWebhookURL = (WebhookURL) obj;
        return value.equals(asWebhookURL.value);
    }

    @Override
    public String toString() {
        return String.format("WebhookURL[%s]", value);
    }
}
