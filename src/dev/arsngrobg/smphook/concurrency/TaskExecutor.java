package dev.arsngrobg.smphook.concurrency;

import dev.arsngrobg.smphook.SMPHook;
import dev.arsngrobg.smphook.SMPHookError;
import dev.arsngrobg.smphook.SMPHookError.ErrorType;
import static dev.arsngrobg.smphook.SMPHookError.condition;

public final class TaskExecutor {
    private final    int id;
    private final Thread thread;

    public Task onFinish;

    private TaskExecutor(int id, Thread thread) {
        this.id     = id;
        this.thread = thread;
    }

    public void begin() throws SMPHookError {
        SMPHookError.caseThrow(
            condition(this::isExecuting, SMPHookError.with(ErrorType.CONCURRENCY, "TaskExecutor is executing.")),
            condition(this::hasExecuted, SMPHookError.with(ErrorType.CONCURRENCY, "TaskExecutor has executed."))
        );

        thread.start();
        if (!thread.isAlive()) {
            throw SMPHookError.with(ErrorType.CONCURRENCY, "TaskExecutorThread could not be started.");
        }
    }

    public void interrupt() throws SMPHookError {
        SMPHookError.caseThrow(
            condition(this::isWaiting,   SMPHookError.with(ErrorType.CONCURRENCY, "TaskExecutor is waiting.")),
            condition(this::hasExecuted, SMPHookError.with(ErrorType.CONCURRENCY, "TaskExecutor has executed."))
        );

        try { thread.interrupt(); } catch (SecurityException e) { SMPHookError.withCause(e); }
        if (!thread.isInterrupted()) {
            throw SMPHookError.with(ErrorType.CONCURRENCY, "TaskExecutorThread could not be interrupted.");
        }
    }

    public boolean isWaiting() {
        return thread.getState() == Thread.State.NEW;
    }

    public boolean isExecuting() {
        return !isWaiting() && !hasExecuted();
    }

    public boolean hasExecuted() {
        return thread.getState() == Thread.State.TERMINATED;
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
        return String.format("TaskExecutor[%d]", id);
    }
}
