package dev.arsngrobg.smphook.core.server;

import dev.arsngrobg.smphook.SMPHook;
import dev.arsngrobg.smphook.SMPHookError;
import dev.arsngrobg.smphook.SMPHookError.ErrorType;

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
 * <p>This class is immutable and thread-safe.</p>
 * 
 * @author Arsngrobg
 * @since  1.0
 */
public final class ServerMessage {
    private static final Pattern PATTERN = Pattern.compile("\\[(\\d{2}:\\d{2}:\\d{2})\\] \\[(.+)\\]: (.+)");

    // time conversion units
    private static final long FROM_HOURS   = 60 * 60;
    private static final long FROM_MINUTES = 60;

    /**
     * <p>Constructs a new {@code ServerMessage} from the supplied {@code serverOutput} string.</p>
     * 
     * @param serverOutput - the raw output from the Minecraft: Java Edition server
     * @return the output, interpreted as a {@code ServerMessage}
     * @throws SMPHookError if the supplied {@code serverOutput} is {@code null} or the {@code serverOutput} does not match the expected pattern
     */
    public static ServerMessage fromServerOutput(String serverOutput) throws SMPHookError {
        Matcher matcher = ServerMessage.PATTERN.matcher(SMPHookError.strictlyRequireNonNull(serverOutput, "serverOutput"));
        if (!matcher.matches()) {
            throw SMPHookError.with(ErrorType.IO, "Incoming server message does not match expected output pattern");
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
        Duration duration = Duration.ofSeconds(timestamp);
        long hours = duration.toHoursPart();
        long mins  = duration.toMinutesPart();
        long secs  = duration.toSecondsPart();
        return String.format("[%02d:%02d:%d] [%s] %s", hours, mins, secs, source, content);
    }
}
