package dev.arsngrobg.smphook.concurrent;

import dev.arsngrobg.smphook.SMPHookError;
import dev.arsngrobg.smphook.SMPHookError.ErrorType;
import static dev.arsngrobg.smphook.SMPHookError.condition;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>A {@code Worker} is a wrapper type for a {@link java.lang.Thread} instance (which is virtual).</p>
 * 
 * <p>It executes a {@link Task} and can be interrupted using the {@link #interrupt()} method.</p>
 * 
 * <p>A {@code Worker} instance can be allocated using its factory methods.</p>
 * 
 * @author Arsngrobg
 * @since  1.0
 * @see Task
 */
public final class Worker {
    private static int nextWorkerID = 0;

    /**
     * <p>Allocates a {@code Worker} to the task {@code t} that begins executing after {@code millis}ms.</p>
     * 
     * @param t - the task to supply to the worker
     * @param millis - the amount of milliseconds to wait before executing the task
     * @return a {@code Waiting} instance that is waiting
     * @throws SMPHookError if {@code t} is {@code null}, or {@code millis} is negative
     */
    public static Worker executeLater(Task t, long millis) throws SMPHookError {
        SMPHookError.requireNonNull(t);

        if (millis < 0) {
            throw SMPHookError.withMessage("'millis' must be an unsigned integer.");
        }

        Task wrapper = () -> {
            SMPHookError.consumeException(() -> Thread.sleep(millis));
            SMPHookError.consumeException(t::execute);
        };

        return Worker.started(wrapper);
    }

    /**
     * <p>Allocates a {@code Worker} to the task {@code t} that begins executing after instantiation.</p>
     * 
     * @param t - the task to supply to the worker
     * @return a {@code Worker} instance that is executing
     * @throws SMPHookError if {@code t} is {@code null}
     */
    public static Worker started(Task t) throws SMPHookError {
        Worker worker = Worker.unstarted(t);
        worker.start();
        return worker;
    }

    /**
     * <p>Allocates a {@code Worker} to the task {@code t} that is waiting to work.</p>
     * 
     * @param t - the task to supply to the worker
     * @return a {@code Worker} instance ready to execute
     * @throws SMPHookError if {@code t} is {@code null}
     */
    public static Worker unstarted(Task t) throws SMPHookError {
        SMPHookError.requireNonNull(t);

        Worker worker = null;

        @SuppressWarnings("null") // this is never executed until atleast after a worker has been supplied with it
        Runnable wrapper = () -> {
            SMPHookError.consumeException(t::execute);
            for (Worker child : worker.children) {
                child.start();
            }
        };

        int workerID = nextWorkerID++;

        String workerThreadName = String.format("Worker#%d", workerID);
        Thread workerThread = Thread.ofVirtual().name(workerThreadName).unstarted(wrapper);

        return new Worker(workerThread, workerID);
    }

    private final Thread thread;
    private final int ID;

    private final List<Worker> children = new ArrayList<>();

    private Worker(Thread thread, int ID) throws SMPHookError {
        if (!SMPHookError.requireNonNull(thread).isVirtual()) { // more of a dev assertion just to make sure I'm not being stupid
            throw SMPHookError.with(ErrorType.CONCURRENCY, "Worker thread supplied is not a virtual thread.");
        }

        this.thread = thread;
        this.ID = ID;
    }

    /**
     * <p>Creates a child worker ready to execute the supplied task {@code t} when its parent has finished executing.</p>
     * 
     * @param t - the task for a child {@code Worker} to execute
     * @return a child {@code Worker}
     * @throws SMPHookError if {@code t} is {@code null}
     */
    public Worker after(Task t) throws SMPHookError {
        Worker child = Worker.unstarted(t);
        children.add(child);
        return child;
    }

    /**
     * <p>Notifies the worker to execute the task at the next available moment.</p>
     * 
     * @throws SMPHookError if the worker is already executing, or has been let go
     */
    public void start() throws SMPHookError {
        SMPHookError.caseThrow(
            condition(this::isStarted, SMPHookError.with(ErrorType.CONCURRENCY, "Worker thread has already started.")),
            condition(this::isLetGo,   SMPHookError.with(ErrorType.CONCURRENCY, "Worker thread has finished."))
        );

        thread.start();
    }

    /**
     * <p>Forcefully stops the worker's execution of its thread.</p>
     * 
     * @throws SMPHookError if the worker has not started its execution, or has been let go
     */
    public void interrupt() throws SMPHookError {
        SMPHookError.caseThrow(
            condition(this::isUnstarted, SMPHookError.with(ErrorType.CONCURRENCY, "Worker thread has not started.")),
            condition(this::isLetGo,     SMPHookError.with(ErrorType.CONCURRENCY, "Worker thread has finished."))
        );

        thread.interrupt();
    }

    /** @return if the underlying thread is awaiting execution */
    public boolean isUnstarted() {
        return thread.getState() == Thread.State.NEW;
    }

    /** @return if the underlying thread is currently being executed */
    public boolean isStarted() {
        return !isUnstarted() && !isLetGo();
    }

    /** @return if the underlying thread has finished */
    public boolean isLetGo() {
        return thread.getState() == Thread.State.TERMINATED;
    }

    /**
     * <i>The ID of a worker is unique to its {@link WorkAllocator}.</i>
     * 
     * @return the unique ID of this worker
     */
    public int getID() {
        return ID;
    }

    @Override
    public int hashCode() {
        return ID;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)                  return false;
        if (obj == this)                  return true;
        if (getClass() != obj.getClass()) return false;
        Worker asWorker = (Worker) obj;
        return ID == asWorker.ID;
    }

    @Override
    public String toString() {
        String stateString;
        if (isUnstarted())  stateString = "UNSTARTED";
        else if (isLetGo()) stateString = "LET_GO";
        else                stateString = "STARTED";
        return String.format("Worker#%d (%s)", ID, stateString);
    }
}
