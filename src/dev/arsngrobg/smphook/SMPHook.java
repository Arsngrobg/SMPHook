package dev.arsngrobg.smphook;

import java.io.File;
import java.util.Scanner;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import dev.arsngrobg.smphook.core.concurrency.TaskExecutor;
import dev.arsngrobg.smphook.core.server.HeapArg;
import dev.arsngrobg.smphook.core.server.JVMOption;
import dev.arsngrobg.smphook.core.server.ServerProcess;
import dev.arsngrobg.smphook.core.server.HeapArg.Unit;

/**
 * <p>The entry point for the program.</p>
 * 
 * @author Arsngrobg
 * @since  1.0
 */
public final class SMPHook {
    /** <p>The local path for the config file - local to the JAR.</p> */
    public static final String CONFIG_FILE_PATH = "hook.json";

    /** <p>The current major version of SMPHook. It is incremented when a <i>major</i> feature is introduced.</p> */
    public static final int VERSION_MAJOR = 1;

    /** <p>The current minor version of SMPHook. It is incremented when <i>minor</i> fixes or features are introduced.</p> */
    public static final int VERSION_MINOR = 0;

    /**
     * <p>Formats the current {@link #VERSION_MAJOR} and {@link #VERSION_MINOR} into the string {@code MAJOR.MINOR}.</p>
     * 
     * @return the formatted version as a string
     */
    public static String getVersion() {
        return String.format("%d.%d", VERSION_MAJOR, VERSION_MINOR);
    }

    /**
     * <p><i>This method is more preferable over Java's methods for its greater compatability.</i></p>
     * 
     * <p>Hashes each element in the supplied argument list of {@code objects}, in which the order of the objects matter.</p>
     * 
     * <p>Each element will rely on its inherint {@link #hashCode()}.
     *    However, if the an element is an array it will apply another invokation to this method to get the hash of each independent element of the sub array - and so on.
     *    If an element is the same as the whole argument list it will use the {@link #hashCode()} of the array reference itself.
     *    If the number of elements supplied is zero - this method returns 0.
     *    If the number of elements supplied is one - this method returns the {@link #hashCode()} for the first element in the argument list.
     * </p>
     * 
     * <p>An illustration of how this hashing algorithm works for {@code n} elements, where {@code n} is greater than 1:
     *    <blockquote><pre>
     *       31 + h[n - 1] * 31 + h[n-2] * ... * 31 + h[0]
     *    </pre></blockquote>
     *    <i>where {@code h} is the individual hash for that element, and {@code n} is the number of elements in the list.</i>
     * </p>
     * 
     * @param objects - the objects to make a combined hash
     * @return a unique hash of those objects
     */
    public static int hashOf(Object... objects) {
        if (objects.length == 0) return 0;
        if (objects.length == 1) return objects[0].hashCode();

        int result = 1;

        for (Object obj : objects) {
            result *= 31;

            if (obj == null) {
                continue;
            }

            int hash;
            if (obj.getClass().isArray()) {
                Object[] arr = (Object[]) obj;
                hash = (arr == objects) ? objects.hashCode() : SMPHook.hashOf(arr);
            } else hash = obj.hashCode();

            result += hash;
        }

        return result;
    }

    public static void runTUI() throws SMPHookError {
        ServerProcess proc = ServerProcess.spawn("smp\\server.jar", null, null);
        System.out.println(proc.getInitCommand());

        TaskExecutor io = TaskExecutor.waiting(() -> {
            Scanner input = new Scanner(System.in);
            while (proc.isRunning()) {
                String command = input.nextLine();
                proc.rawInput(command);
            }
            input.close();
        });

        proc.init(true);
        io.begin();

        TaskExecutor.execute(() -> {
            while (true) {
                SMPHookError.consumeException(() -> Thread.sleep(3000));
                proc.rawInput("say Hello, World!");
            }
        });

        String line;
        while (!(line = proc.rawOutput()).equals(ServerProcess.EOF)) {
            System.out.println(line);
        }
    }

    public static void main(String[] args) throws SMPHookError {
        //runTUI();

        var config = SMPHookConfig.load(SMPHook.CONFIG_FILE_PATH);
    }
}
