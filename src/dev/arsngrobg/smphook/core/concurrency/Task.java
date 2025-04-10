package dev.arsngrobg.smphook.core.concurrency;

/**
 * <p>The {@code Task} interface is a functional interface which a {@link TaskExecutor} executes.</p>
 * 
 * @author Arsngrobg
 * @since  1.0
 * @see    TaskExecutor
 */
@FunctionalInterface
public interface Task {
    /**
     * <p>Executes the block of code wrapped by this {@code Task} object.</p>
     * 
     * <p>This method may throw an {@link java.lang.Exception} and is always consumed and not handled.</p>
     * 
     * @throws Exception any exception that may be thrown by this block of code
     */
    void execute() throws Exception;
}
