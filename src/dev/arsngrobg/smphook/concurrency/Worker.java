package dev.arsngrobg.smphook.concurrency;

import dev.arsngrobg.smphook.SMPHookError;
import dev.arsngrobg.smphook.SMPHookError.ErrorType;
import static dev.arsngrobg.smphook.SMPHookError.condition;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * <p>A {@code Worker} is a wrapper type for a {@link java.lang.Thread}.
 *    The {@code Worker} class is designed to handle small jobs that do not require constant use of resource and CPU time, hence all worker threads are <i>virtual</i>.
 *    Due to the fact that the threads executed by {@code Worker}'s are <i>virtual</i>, they do not hang after JVM shutdown.
 *    On instantiation, the {@code Worker} is assigned a unique ID for the current SMPHook instance, and this is based on instance order.
 * </p>
 * 
 * <p>Workers do not hang after the Main thread has finished executing (as the threads are inherintly virtual).</p>
 * 
 * <p>It is repsonsible for executing a given {@link Task}, which can be started and stopped using {@link #start()} and {@link #stop()} respectively.
 *    A worker can be in one of three states: <b>WAITING</b>, <b>WORKING</b>, and <b>FINISHED</b>.
 *    These states can be queried through the {@code Worker}'s predicate methods.
 * </p>
 * 
 * <p>A {@code Worker} can be instantiated with its factory methods.</p>
 * 
 * @author Arsngrobg
 * @since  1.0
 * @see    Worker#ofWorking(Task)
 * @see    Worker#ofWaiting(Task)
 * @see    Worker#ofFuture(Task, long)
 * @see    Task
 */
public final class Worker {
    /**
     * <p>Assigns a {@link Task} to a {@code Worker} that will wait {@code millis}ms before executing its task.</p>
     * 
     * @param t - the task to be executed by a {@code Worker} {@code millis}ms later
     * @param millis - the amount of time (in ms) to wait before the {@code Worker} should execute its task
     * @return a {@code Worker} that will execute its task later
     * @throws SMPHookError if the supplied task {@code t}
     */
    public static Worker ofFuture(Task t, long millis) throws SMPHookError {
        SMPHookError.requireNonNull(t);

        Task wrapper = () -> {
            SMPHookError.consumeException(() -> Thread.sleep(millis));
            SMPHookError.consumeException(t::execute);
        };

        return Worker.ofWorking(wrapper);
    }

    /**
     * <p>Assigns a {@link Task} to a {@code Worker} that is instantly notified to execute its task.</p>
     * 
     * @param t - the task to be executed by a {@code Worker} as soon as it receives it
     * @return a {@code Worker} working on the task {@code t}
     * @throws SMPHookError if the supplied task {@code t} is {@code null}
     */
    public static Worker ofWorking(Task t) throws SMPHookError {
        Worker worker = Worker.ofWaiting(t);
        worker.start();
        return worker;
    }

    /**
     * <p>Assigns a task to a {@code Worker} that is waiting to be executed (in the <b>WAITING</b> state).</p>
     * 
     * @param t - the task to be executed by a {@code Worker} when notified
     * @return a {@code Worker} ready to execute the task {@code t}
     * @throws SMPHookError if the supplied task {@code t} is {@code null}
     */
    public static Worker ofWaiting(Task t) throws SMPHookError {
        SMPHookError.requireNonNull(t);

        Worker worker = null;
        AtomicReference<Worker> workerRef = new AtomicReference<>();

        Runnable wrapper = () -> {
            SMPHookError.consumeException(t::execute);
            workerRef.get().initChild();
        };

        StackTraceElement[] stacktrace = Stream.of(Thread.currentThread().getStackTrace())
                                               .filter(e -> !e.getFileName().equals("Worker.java"))
                                               .toArray(StackTraceElement[]::new);
        String callerClassName = stacktrace[1].getClassName(); // element 0 is Thread.currentThread().getStackTrace()
        Class<?> caller = SMPHookError.throwIfFail(() -> Class.forName(callerClassName));

        String workerThreadName = String.format("VIRTUAL_THREAD | Worker#%d", nextWorkerID);
        Thread workerThread = Thread.ofVirtual().name(workerThreadName).unstarted(wrapper);

        worker = new Worker(workerThread, caller);
        workerRef.set(worker);
        return worker;
    }

    // each ID of a worker is the subsequent integer after the last one - simple but there is a very narrow case where they are equal
    private static int nextWorkerID = 0;

    private final int ID = nextWorkerID++;
    private final Thread thread;
    private final Class<?> caller;

    private Optional<Worker> child = Optional.empty();

    private Worker(Thread thread, Class<?> caller) throws SMPHookError {
        if (!SMPHookError.requireNonNull(thread).isVirtual()) { // dev assertion
            throw SMPHookError.with(ErrorType.CONCURRENCY, "The supplied worker thread is not a virtual thread.");
        }

        this.thread = thread;
        this.caller = SMPHookError.requireNonNull(caller);
    }

    /**
     * <p>Notifies the worker to execute the task at the next available moment.</p>
     * 
     * @throws SMPHookError if the worker is already executing, or has finished executing
     */
    public void start() throws SMPHookError {
        SMPHookError.caseThrow(
            condition(this::isWorking,  SMPHookError.with(ErrorType.CONCURRENCY, "Unable to start: Worker is already began working")),
            condition(this::isFinished, SMPHookError.with(ErrorType.CONCURRENCY, "Unable to start: Worker has finished execution."))
        );

        thread.start();
    }

    /**
     * <p>Forcefully stops the worker's execution of its thread.</p>
     * 
     * @throws SMPHookError if the worker has not started its execution, or has finished executing
     */
    public void interrupt() throws SMPHookError {
        SMPHookError.caseThrow(
            condition(this::isWaiting,  SMPHookError.with(ErrorType.CONCURRENCY, "Unable to interrupt: Worker is waiting for execution.")),
            condition(this::isFinished, SMPHookError.with(ErrorType.CONCURRENCY, "Unable to interrupt: Worker has finished execution."))
        );

        thread.interrupt();
        if (!thread.isInterrupted()) {
            SMPHookError.with(ErrorType.CONCURRENCY, "Worker thread could not be interrupted for an unkown reason.");
        }

        initChild();
    }

    /**
     * <p>Attaches a child {@code Worker} to this parent worker.</p>
     * 
     * <p>The child worker will be invoked after the parent has finished executing.</p>
     * 
     * @param t - the task that is executed after the execution of this worker's current task
     * @return a new {@code Worker} instance (can be used for chaining)
     * @throws SMPHookError if {@code t} is {@code null}
     */
    public Worker then(Task t) throws SMPHookError {
        child = Optional.of(Worker.ofWaiting(t));
        if (isFinished()) {
            child.get().start();
        }

        return child.get();
    }

    private void initChild() {
        child.ifPresent(c -> {
            if (c.isWaiting()) {
                c.start();
            }
        });
    }

    /**
     * <p>Checks whether the worker thread is neither in the {@link Thread.State#NEW} or the {@link Thread.State#TERMINATED} state.</p>
     * 
     * <p>This is the transitional state between <b>WAITING</b> and <b>FINISHED</b>.</p>
     * 
     * @return {@code true} if so, {@code false} if otherwise
     */
    public boolean isWorking() {
        return !isWaiting() && !isFinished();
    }

    /**
     * <p>Checks whether the worker thread is in the {@link Thread.State#NEW} state.</p>
     * 
     * <p>This is the inital state of a {@code Worker}.</p>
     * 
     * @return {@code true} if so, {@code false} if otherwise
     */
    public boolean isWaiting() {
        return thread.getState() == Thread.State.NEW;
    }

    /**
     * <p>Checks whether the worker thread is in the {@link Thread.State#TERMINATED} state.</p>
     * 
     * <p>this is the final state of a {@code Worker}.</p>
     * 
     * @return {@code true} if so, {@code false} if otherwise
     */
    public boolean isFinished() {
        return thread.getState() == Thread.State.TERMINATED;
    }

    /**
     * @return the class that invoked this {@code Worker} instance
     */
    public Class<?> getCaller() {
        return caller;
    }

    /**
     * <p>{@code Worker} IDs are not related to the task they are given, but determined by the order they are instanced.</p>
     * 
     * @return the unique ID for this worker in this instance of the SMPHook system.
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
        String callerName = caller.getSimpleName();
        if (!callerName.equals("SMPHook")) {
            callerName = "SMPHook.".concat(callerName);
        }
        return String.format("%s <- Worker#%d", callerName, ID);
    }
}
