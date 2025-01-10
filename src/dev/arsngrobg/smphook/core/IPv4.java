package dev.arsngrobg.smphook.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import dev.arsngrobg.smphook.SMPHookError;
import dev.arsngrobg.smphook.SMPHookError.Type;

/**
 * <p>A wrapper class for a String that represents an IPv4 address.</p>
 * 
 * @author Arsngrobg
 * @since  1.0
 */
public final class IPv4 {
    private static final String CURL_CMD = "curl -4 ifconfig.me";

    /**
     * <p>Queries the public IPv4 address using the <a href="https://github.com/curl/curl">curl</a> library.</p>
     * <p>This returns an empty {@link Optional} if the request failed.</p>
     * @return an {@link Optional} containing the {@code IPv4} object
     */
    public static Optional<IPv4> queryPublic() {
        String[] cmdTokens = CURL_CMD.split("\\s+");
        ProcessBuilder pb = new ProcessBuilder(cmdTokens);
        try (InputStream istream = pb.start().getInputStream()) {
            byte[] bytes = istream.readAllBytes();
            return Optional.of(new IPv4(new String(bytes)));
        } catch (IOException | SMPHookError ignored) { return Optional.empty(); } // this is the only case where we can overlook the error being thrown
    }

    // https://stackoverflow.com/questions/5284147/validating-ipv4-addresses-with-regexp
    private static final String REGEX = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$";

    private final String address;

    /**
     * Initialises an {@code IPv4} value with the given {@code address}.
     * @param address - the {@code String} that is of the pattern x.x.x.x (x = 0..255)
     * @throws SMPHookError if the {@code address} is {@code null} or is invalid
     */
    public IPv4(String address) throws SMPHookError {
        if (address == null) SMPHookError.throwNullPointer("address");
        if (!address.matches(REGEX)) {
            throw SMPHookError.getErr(Type.INVALID_IPV4_ADDRESS);
        }

        this.address = address;
    }

    /** @return the wrapped {@code String} IPv4 address */
    public String getAddress() {
        return address;
    };

    @Override
    public int hashCode() {
        return address.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) return false;
        if (obj == this) return true;
        IPv4 asIPv4 = (IPv4) obj;
        return address.equals(asIPv4.address);
    }

    @Override
    public String toString() {
        return String.format("IPv4[%s]", address);
    }
}
