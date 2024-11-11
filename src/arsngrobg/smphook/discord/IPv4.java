package arsngrobg.smphook.discord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Wrapper class for an IPv4 value.
 * The internal value must strictly match the rough pattern of an IPv4 value.
 * The internal value can be {@code null}.
 */
public final class IPv4 {
    private static final String CURL_COMMAND = "curl -4 ifconfig.me";

    /**
     * Queries your home router's current public IPv4 address using the ifconfig.me web service.
     * @return an {@link IPv4} value with the internal {@link String} value
     */
    public static IPv4 query() {
        String[] commandTokens = CURL_COMMAND.split("\\s");
        ProcessBuilder processBuilder = new ProcessBuilder(commandTokens);
        try {
            Process process = processBuilder.start();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String output = br.readLine();
                IPv4 ipv4 = new IPv4(output);
                return ipv4;
            }
        } catch (IOException ignored) { return new IPv4(null); }
    }

    // the regex pattern that the value must match
    private final String REGEX = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$";

    private final String value;

    /**
     * Intialises an IPv4 value with the specified {@code value}.
     * @param value - a {@link String} that must match the rough pattern of an IPv4 value
     */
    public IPv4(String value) {
        if (value == null || !value.matches(REGEX)) value = null;
        this.value = value;
    }

    /** @return whether the internal value is {@code null}. */
    public boolean isNull() {
        return value == null;
    }

    /** @return the {@link String} representation of this IPv4. */
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
        IPv4 asIPv4 = (IPv4) obj;
        return value.equals(asIPv4.value);
    }

    @Override
    public String toString() {
        return String.format("IPv4[%s]", value);
    }
}
