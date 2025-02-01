package dev.arsngrobg.smphook.concurrent;

/**
 * <p>A {@code Task} is defined as a unit of work which a {@link Worker} {@link Thread} will execute.</p>
 * 
 * <p>It has the same signature as the {@link Runnable} interface but delegates the exception handling to the caller.</p>
 * 
 * @author Arsngrobg
 * @since  1.0
 * @see    Worker
 */
@FunctionalInterface
public interface Task {
    /**
     * <p>Executes the code block within this method body.</p>
     * 
     * @throws Exception
     */
    void execute() throws Exception;
}
