package dev.arsngrobg.smphook.core.server;

import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import dev.arsngrobg.smphook.SMPHook;
import dev.arsngrobg.smphook.SMPHookConfig;
import dev.arsngrobg.smphook.SMPHookError;
import dev.arsngrobg.smphook.SMPHookError.ErrorType;
import dev.arsngrobg.smphook.core.concurrency.TaskExecutor;

/**
 * <p>The {@code MinecraftServer} class is a high-level abstraction of the {@link ServerProcess} class.</p>
 * 
 * @author Arsngrobg
 * @since  1.0
 * @see    ServerProcess
 */
public final class MinecraftServer {
    public static MinecraftServer fromConfig(SMPHookConfig.ServerSettings config) throws SMPHookError {
        return MinecraftServer.fromProcess(ServerProcess.spawn(
            config.jarPath(), config.minHeap(), config.maxHeap(), config.jvmOptions()
        ));
    }

    public static MinecraftServer fromProcess(ServerProcess proc) throws SMPHookError {
        return new MinecraftServer(SMPHookError.strictlyRequireNonNull(proc, "proc"));
    }

    /** <p>The maximum capacity of the output buffer.</p> */
    public static final int OUTPUT_BUFFER_SIZE = 64;

    private final BlockingQueue<ServerMessage> buffer = new ArrayBlockingQueue<>(OUTPUT_BUFFER_SIZE);

    private final ServerProcess proc;

    private MinecraftServer(ServerProcess proc) {
        this.proc = proc;
    }

    /**
     * <p>Starts the Minecraft server.</p>
     * 
     * @throws SMPHookError if the underlying process could not be started
     */
    public void start() throws SMPHookError {
        proc.init(true);
        if (!proc.isRunning()) {
            throw SMPHookError.with(ErrorType.IO, "Server could not be started");
        }

        buffer.clear();
        // these will always terminate - either through SIGINT or server close, so no need to be managed
        TaskExecutor.execute(() -> {
            String rawMsg;
            while ( !(rawMsg = proc.rawOutput()).equals(ServerProcess.EOF) ) {
                buffer.put(ServerMessage.fromServerOutput(rawMsg));
            }
            buffer.add(ServerMessage.EOF);
        });
    }

    /**
     * <p>Attempts to stop the server, pushes the {@code "stop"} command to the server.</p>
     * 
     * @return {@code true} if the server was stopped, {@code false} if otherwise
     * @throws SMPHookError if the Minecraft server is not running
     */
    public boolean stop() throws SMPHookError {
        proc.rawInput("stop");
        for (ServerMessage msg : buffer.toArray(ServerMessage[]::new)) {
            if (msg.equals(ServerMessage.EOF)) {
                return true;
            }
        }

        return false;
    }

    /**
     * <p>Pushes the command to the Minecraft server.</p>
     * 
     * <p>This method may or may not return a {@link ServerMessage} object.</p>
     * 
     * @param command - the command to push to the server
     * @return the server response (if any)
     * @throws SMPHookError if the supplied {@code command} is {@code null}
     */
    public Optional<ServerMessage> pushCommand(String command) throws SMPHookError {
        long timestamp = System.currentTimeMillis() % 86400;
        proc.rawInput(command);

        for (ServerMessage msg : buffer.toArray(ServerMessage[]::new)) {
            if (msg.getTimestamp() < timestamp) continue;
            return Optional.of(msg);
        }

        return Optional.ofNullable(null);
    }

    public ServerMessage getMessage() {
        ServerMessage msg = SMPHookError.throwIfFail(buffer::take);
        return msg;
    }

    /**
     * <p>Checks to see if the Minecraft server in running.
     *    If it is, it will also check if the input and output streams are non-{@code null}.
     *    If not an {@link SMPHookError} is thrown.
     * </p>
     * 
     * @return {@code true} if running (+ in typical state), or {@code false} if otherwise
     * @throws SMPHookError if the server is in an unusual state
     */
    public boolean isRunning() throws SMPHookError {
        return proc.isRunning();
    }

    /** @return the minimum heap allocation pool argument wrapped in an optional */
    public Optional<HeapArg> getMinHeap() {
        return proc.getMinHeap();
    }

    /** @return the maximum heap allocation pool argument wrapped in an optional. */
    public Optional<HeapArg> getMaxHeap() {
        return proc.getMaxHeap();
    }

    @Override
    public int hashCode() {
        return SMPHook.hashOf(proc);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof MinecraftServer asServer)) return false;
        return proc.equals(asServer.proc);
    }

    @Override
    public String toString() {
        return proc.toString(); // TODO
    }
}
