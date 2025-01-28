package dev.arsngrobg.smphook;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import dev.arsngrobg.smphook.server.HeapArg;
import dev.arsngrobg.smphook.server.JVMOption;
import dev.arsngrobg.smphook.server.ServerProcess;

/**
 * <p>The main program.</p>
 * 
 * @author Arsngrobg
 * @since  1.0
 */
public final class SMPHook {
    /** <p>The major version of <i>SMPHook</i>. This is incremented whenever major features are introduced.</p> */
    public static final int VERSION_MAJOR = 1;

    /** <p>The minor version of <i>SMPHook</i>. This is incremented whenever minor (e.g. bug fixes, QOL) features are introduced.</p> */
    public static final int VERSION_MINOR = 0;

    private static final List<Thread> workers = new ArrayList<>();

    // functional interface that delegates exception handling to the worker thread and aborts if an exception occurs
    @FunctionalInterface
    private static interface Task {
        void perform() throws Exception;
    }

    /**
     * <p>Assigns a worker {@link Thread} to the given {@code task} function to be performed concurrently.</p>
     * 
     * <p>You can chain callback functions using the returned {@link CompletableFuture}.</p>
     * 
     * <p>The cleanup of workers are automatically handled by SMPHook.</p>
     * 
     * @param task - a task to perform concurrently
     * @return a {@link CompletableFuture}
     */
    public static CompletableFuture<Thread> assignWorkerTo(Task task) {
        int workerIdx = workers.size();
        String workerID = String.format("Worker#%d", workerIdx);

        CompletableFuture<Thread> future = new CompletableFuture<>();

        Runnable wrapper = () -> {
            try { task.perform(); } catch (Exception e) { SMPHookError.withCause(e); }
            future.complete(Thread.currentThread());
            workers.set(workerIdx, null);
        };

        Thread workerThread = Thread.ofVirtual().name(workerID).unstarted(wrapper);
        workers.add(workerThread);
        workerThread.start();

        return future;
    }

    /**
     * <p>Retrieves the string representation of the version as a string, consisting of the major and minor version numbers.</p>
     * 
     * <p>The version is returned in the format of [<i>major</i>].[<i>minor</i>].</p>
     * 
     * @return the version of this SMPHook instance as [<i>major</i>].[<i>minor</i>]
     */
    public static String getVersion() {
        return String.format("%d.%d", VERSION_MAJOR, VERSION_MINOR);
    }

    public static void main(String[] args) throws Exception {
        var arg = HeapArg.fromString("300G");
        System.out.println(arg);

        System.out.println(JVMOption.enabled("UnlockExperimentalVMOptions"));
        System.out.println(JVMOption.enabled("UseG1GC"));
        System.out.println(JVMOption.disabled("UseG1GC"));
        System.out.println(JVMOption.assigned("G1NewSizePercent", "20"));

        ServerProcess proc = new ServerProcess("smp\\server.jar", HeapArg.fromString("2G"), HeapArg.fromString("8G"));
        System.out.println(proc.getInitCommand());
        System.out.println();

        proc.init(false);

        SMPHook.assignWorkerTo(() -> proc.rawInput("say Hello, World!")).thenAccept(t -> proc.rawInput("say FINISHED!"));

        String line;
        while (!(line = proc.rawOutput()).equals(ServerProcess.EOF)) {
            System.out.printf("[Server] :: %s\n", line);
        }

        throw SMPHookError.nullReference("TEST ERROR CASE");
    }

    private SMPHook() {}
}
