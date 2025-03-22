package dev.arsngrobg.smphook.concurrency;

import dev.arsngrobg.smphook.SMPHook;
import dev.arsngrobg.smphook.SMPHookError;
import dev.arsngrobg.smphook.SMPHookError.ErrorType;
import static dev.arsngrobg.smphook.SMPHookError.condition;

/**
 * <p>The {@code TaskExecutor} class provides a mechanism for managing and executing tasks
 *    asynchronously using a pool of reusable executors.
 * </p>
 * 
 * <p>Each {@code TaskExecutor} runs tasks in a separate thread, and the same executor can
 *    be reused after the task has finished execution. Executors are pooled, and when all executors
 *    are in use, the pool dynamically grows to accommodate new tasks.
 * </p>
 * 
 * <p>This class ensures efficient management of threads, reducing the overhead of creating
 *    new threads for every task and promoting reusability of existing threads.
 * </p>
 * 
 * @author Arsngrobg
 * @since  1.0
 * @see    #waiting(Task)
 * @see    #execute(Task)
 */
public final class TaskExecutor {
    private static final int BASE_EXECUTOR_POOL_SIZE = 10;
    private static TaskExecutor[] executorPool = new TaskExecutor[BASE_EXECUTOR_POOL_SIZE];

    /**
     * <p>Gets a {@code TaskExecutor} object, supply the given task {@code t} to it, and execute it.</p>
     * 
     * @param t - the task for the {@code TaskExecutor} to execute
     * @return a {@code TaskExecutor} instance executing the given task
     * @throws SMPHookError if the supplied task {@code t} is {@code null}
     */
    public static TaskExecutor execute(Task t) throws SMPHookError {
        TaskExecutor executor = TaskExecutor.waiting(t);
        executor.begin();
        return executor;
    }

    /**
     * <p>Gets a {@code TaskExecutor} object and supply the given task {@code t} to it.</p>
     * 
     * @param t - the task for the {@code TaskExecutor} to execute
     * @return a {@code TaskExecutor} instance waiting to execute its task
     * @throws SMPHookError if the supplied task {@code t} is {@code null}
     */
    public static TaskExecutor waiting(Task t) throws SMPHookError {
        TaskExecutor executor = getNextAvailableExecutor();
        executor.newTask(t);
        return executor;
    }

    private static TaskExecutor getNextAvailableExecutor() {
        for (int idx = 0; idx < executorPool.length; idx++) {
            if (executorPool[idx] == null) {
                executorPool[idx] = new TaskExecutor(idx);
                return executorPool[idx];
            }

            if (executorPool[idx].hasExecuted()) {
                return executorPool[idx];
            }
        }

        TaskExecutor[] copy = new TaskExecutor[executorPool.length + 1];
        TaskExecutor executor = new TaskExecutor(copy.length - 1);
        copy[copy.length - 1] = executor;
        System.arraycopy(copy, 0, executorPool, 0, executorPool.length);

        return executor;
    }

    private final int id;

    private Thread thread;

    /** <p>The Task to complete on execution of its base {@link Task}.</p> */
    public Task onFinish;

    private TaskExecutor(int id) {
        this.id = id;
    }

    void newTask(Task t) throws SMPHookError {
        SMPHookError.strictlyRequireNonNull(t, "t");

        Runnable wrapper = () -> {
            SMPHookError.ifFail(
                t::execute,
                () -> System.out.println("TaskExecutor task failed.")
            );

            if (onFinish != null) {
                SMPHookError.ifFail(
                    onFinish::execute,
                    () -> System.out.println("TaskExecutor onFinish callback failed.")
                );
            }
        };

        thread = Thread.ofVirtual().name("TaskExecutorThread").unstarted(wrapper);
    }

    /**
     * <p>Begins execution of this {@code TaskExecutor}s thread.</p>
     * 
     * @throws SMPHookError if this {@code TaskExecutor} is executing, has executed, or the thread could not be started
     */
    public void begin() throws SMPHookError {
        SMPHookError.caseThrow(
            condition(this::isExecuting, SMPHookError.with(ErrorType.CONCURRENCY, "TaskExecutor is executing.")),
            condition(this::hasExecuted, SMPHookError.with(ErrorType.CONCURRENCY, "TaskExecutor has executed."))
        );

        SMPHookError.throwIfFail(thread::start);
    }

    /**
     * <p>Interrupts the execution of this {@code TaskExecutor}s thread.</p>
     * 
     * @throws SMPHookError if this {@code TaskExecutor} is waiting, has executed, or the thread could not be interrupted
     */
    public void interrupt() throws SMPHookError {
        SMPHookError.caseThrow(
            condition(this::isWaiting,   SMPHookError.with(ErrorType.CONCURRENCY, "TaskExecutor is waiting.")),
            condition(this::hasExecuted, SMPHookError.with(ErrorType.CONCURRENCY, "TaskExecutor has executed."))
        );

        SMPHookError.throwIfFail(thread::interrupt);
    }

    /** @return whether this thread is in the <b>NEW</b> state */
    public boolean isWaiting() {
        return thread.getState() == Thread.State.NEW;
    }

    
    /** @return whether this thread is neither in the <b>NEW</b> state or the <b>TERMINATED</b> state */
    public boolean isExecuting() {
        return !isWaiting() && !hasExecuted();
    }

    /** @return whether this thread is in the <b>TERMINATED</b> state */
    public boolean hasExecuted() {
        return thread.getState() == Thread.State.TERMINATED;
    }

    /** @return the unique ID of this {@code TaskExewcutor} */
    public int getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return SMPHook.hashOf(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (getClass() != obj.getClass()) return false;
        TaskExecutor asExecutor = (TaskExecutor) obj;
        return id == asExecutor.id;
    }

    @Override
    public String toString() {
        return String.format("TaskExecutor(%d)", id);
    }
}
