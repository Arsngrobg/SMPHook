package dev.arsngrobg.smphook.core.server;

import dev.arsngrobg.smphook.SMPHook;
import dev.arsngrobg.smphook.SMPHookError;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>A {@code ServerMessage} class represents the output from a Minecraft: Java Edition server process.</p>
 * 
 * <p>It takes output from the server:
 *    <blockquote><pre>
 *       [22:15:19] [Server thread/INFO]: Done (6.420s)! For help, type "help"
 *    </pre></blockquote>
 *    Splits it up into 3 parts:
 *    <ul>
 *        <li>The timestamp - a long value (between {@code 0} & {@code 86400}</li>
 *        <li>The source    - the thread/area of the code that this message originated</li>
 *        <li>The content   - the actual message</li>
 *    </ul>
 * </p>
 * 
 * <p>If you want this object, represented as its original string, use the {@link #toFullyQualifiedString()} method.</p>
 * 
 * <p>This class is immutable and <b>NOT</b> thread-safe - for speed concerns.</p>
 * 
 * @author Arsngrobg
 * @since  1.0
 */
public final class ServerMessage {
    /** <p>The singleton instance for an End-of-File message from the server.</p> */
    public static final ServerMessage EOF = new ServerMessage(0, null, ServerProcess.EOF);

    /** <p>A negative value denoting that the timestamp of the server message is unknown.</p> */
    public static final long TIMESTAMP_UNKNOWN = -1;

    // for constant reuse since many invokations of the toFullyQualifiedString() method - THIS MAKES THIS CLASS NOT THREAD SAFE (synchronized IS SLOW)
    private static final StringBuilder REPR_BUILDER = new StringBuilder(256);
    private static final Pattern PATTERN = Pattern.compile("\\[(\\d{2}:\\d{2}:\\d{2})\\] \\[(.+)\\]: (.+)");

    // time conversion units
    private static final long FROM_HOURS   = 60 * 60;
    private static final long FROM_MINUTES = 60;

    /**
     * <p>Constructs a new {@code ServerMessage} from the supplied {@code serverOutput} string.</p>
     * 
     * <p>If the output does not match the expected pattern, then a {@code ServerMessage} is returned where the message content is {@code serverOutput}.</p>
     * 
     * @param serverOutput - the raw output from the Minecraft: Java Edition server
     * @return the output, interpreted as a {@code ServerMessage}
     * @throws SMPHookError if the supplied {@code serverOutput} is {@code null}
     */
    public static ServerMessage fromServerOutput(String serverOutput) throws SMPHookError {
        if (SMPHookError.strictlyRequireNonNull(serverOutput, "serverOutput").equals(ServerProcess.EOF)) {
            return ServerMessage.EOF;
        }

        Matcher matcher = ServerMessage.PATTERN.matcher(SMPHookError.strictlyRequireNonNull(serverOutput, "serverOutput"));
        if (!matcher.matches()) {
            return new ServerMessage(TIMESTAMP_UNKNOWN, null, serverOutput);
        }

        String[] formatTime = matcher.group(1).split(":");
        String   source     = matcher.group(2);
        String   content    = matcher.group(3);

        long timestamp = Long.parseLong(formatTime[0]) * ServerMessage.FROM_HOURS
                       + Long.parseLong(formatTime[1]) * ServerMessage.FROM_MINUTES
                       + Long.parseLong(formatTime[2]);
        
        return new ServerMessage(timestamp, source, content);
    }

    private final long   timestamp;
    private final String source;
    private final String content;

    private ServerMessage(long timestamp, String source, String content) {
        this.timestamp = timestamp;
        this.source    = source;
        this.content   = content;
    }

    /**
     * <p>Returns the fully-qualified string representation of this {@code ServerMessage} object.</p>
     * 
     * <p>This method is the reason why this class is not considered <b>thread-safe</b> as every object uses a global {@link StringBuilder} instance.
     *    Speed concerns arise due to the use of the {@code synchronized} keyword - hence this class is not <b>thread-safe</b>.
     * </p>
     * 
     * @return the fully qualified string representation of this object
     */
    public String toFullyQualifiedString() {
        REPR_BUILDER.setLength(0);

        if (timestamp > 0) {
            Duration duration = Duration.ofSeconds(timestamp);
            long hours = duration.toHoursPart();
            long mins  = duration.toMinutesPart();
            long secs  = duration.toSecondsPart();

            REPR_BUILDER.append(String.format("[%02d:%02d:%02d]", hours, mins, secs))
                        .append("\s");
        }

        if (source != null) {
            REPR_BUILDER.append("[")
                        .append(source)
                        .append("]")
                        .append("\s");
        }

        REPR_BUILDER.append(content);

        return REPR_BUILDER.toString();
    }

    /** @return the time - in seconds, since midnight */
    public long getTimestamp() {
        return timestamp;
    }

    /** @return the thread/area of code that this message originated */
    public String getSource() {
        return source;
    }

    /** @return the actual message content */
    public String getContent() {
        return content;
    }

    @Override
    public int hashCode() {
        return SMPHook.hashOf(timestamp, source, content);
    }

    @Override
    public String toString() {
        return String.format("ServerMessage[T:%d S:%s C:%s]", timestamp, source, content);
    }
}
