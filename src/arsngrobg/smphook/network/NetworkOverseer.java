package arsngrobg.smphook.network;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Tool for monitoring the state of your network connection by monitoring the IPv4 every {@code n} milliseconds.
 * A callback can be set when the overseer detects a IP change, this is set using the {@link #onNetworkChange(Runnable)} method.
 * The callback can be forcefully invoked when passing {@code true} to the {@link #stop(boolean)} method.
 */
public final class NetworkOverseer {
    private static final long RECHECK_INTERVAL = 5000;

    // state memory
    private IPv4 previous = new IPv4(null);
    private IPv4 current  = previous;

    // task scheduler
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledFuture;

    // callback
    private Runnable networkChangeCallback;

    private void analyserJob() {
        current = IPv4.query();
        if (current.isNull()) return;

        if (!current.equals(previous)) stop(true);

        previous = current;
    }

    /**
     * Starts the overseer and will begin its job after {@code checkInterval} milliseconds have passed,
     * and will indefinitely validate the state of the network until a change has occurred, at an interval of {@code checkInterval} milliseconds.
     * @param checkInterval - the time (in milliseconds) to wait inbetween checking the state of the network connection
     */
    public void start(long checkInterval) {
        if (isAnalysing()) stop(false);

        int checks = 0;
        do {
            checks++;
            if (checks > 1) try { Thread.sleep(RECHECK_INTERVAL); } catch (InterruptedException ignored) {}
            previous = IPv4.query();
        } while (previous.isNull());

        scheduledFuture = scheduler.scheduleAtFixedRate(this::analyserJob, checkInterval, checkInterval, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the overseer and invokes its callback if {@code invokeCallback} is {@code true}.
     * @param invokeCallback - whether the overseer should invoke its callback
     */
    public void stop(boolean invokeCallback) {
        if (!isAnalysing()) return;

        scheduledFuture.cancel(true);

        previous = new IPv4(null);
        current = previous;

        if (invokeCallback && networkChangeCallback != null) {
            networkChangeCallback.run();
        }
    }

    /** @return whether the overseer is analysing */
    public boolean isAnalysing() {
        return scheduledFuture != null;
    }

    /**
     * Sets the new callback and returns the old one.
     * @param callback - the new callback
     * @return the previous {@link Runnable} instance
     */
    public Runnable onNetworkChange(Runnable callback) {
        Runnable previousCallback = networkChangeCallback;
        networkChangeCallback = callback;
        return previousCallback;
    }

    /** @return the previous {@link IPv4} state. */
    public IPv4 getPrevious() {
        return previous;
    }

    /** @return the current {@link IPv4} state. */
    public IPv4 getCurrent() {
        return current;
    }
}
