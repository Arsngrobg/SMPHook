package dev.arsngrobg.smphook;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import dev.arsngrobg.smphook.core.concurrency.TaskExecutor;

/**
 * <p>The entry point for the program.</p>
 * 
 * @author Arsngrobg
 * @since  1.0
 */
public final class SMPHook {
    /** <p>Enables certain features used for debugging purposes (i.e. using a terminal emulator).</p> */
    public static final boolean DEBUG = true;

    /** <p>The current major version of SMPHook. It is incremented when a <i>major</i> feature is introduced.</p> */
    public static final int VERSION_MAJOR = 1;

    /** <p>The current minor version of SMPHook. It is incremented when <i>minor</i> fixes or features are introduced.</p> */
    public static final int VERSION_MINOR = 0;

    /** <p>Command-line arguments for determining the runtime use-case.</p> */
    public static final String
        ARG_NOGUI = "-nogui",
        ARG_SETUP = "-setup";

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
                hash = (arr.equals(objects)) ? objects.hashCode() : SMPHook.hashOf(arr);
            } else hash = obj.hashCode();

            result += hash;
        }

        return result;
    }

    public static void tui() throws Exception {
        DefaultTerminalFactory factory = new DefaultTerminalFactory()
        .setPreferTerminalEmulator(SMPHook.DEBUG)
        .setForceTextTerminal(!SMPHook.DEBUG)
        .setTerminalEmulatorTitle(String.format("SMPHook v%s", SMPHook.getVersion()));

        Terminal terminal = factory.createTerminal();
        TextGraphics tgfx = SMPHookError.throwIfFail(terminal::newTextGraphics);

        tgfx.setForegroundColor(TextColor.ANSI.WHITE_BRIGHT);
        tgfx.getActiveModifiers().add(SGR.BOLD);

        StringBuilder buffer = new StringBuilder();

        terminal.addResizeListener((t, s) -> {
            SMPHookError.throwIfFail(t::clearScreen);

            tgfx.setBackgroundColor(TextColor.Indexed.fromRGB(15, 15, 15));
            tgfx.fill('\s');

            tgfx.setBackgroundColor(TextColor.Indexed.fromRGB(50, 168, 82));
            tgfx.drawLine(0, s.getRows() - 2, s.getColumns(), s.getRows() - 2, '\s');
            tgfx.putString(3, s.getRows() - 2, String.format("SMPHook v%s", SMPHook.getVersion()));
            tgfx.setBackgroundColor(TextColor.Indexed.fromRGB(15, 15, 15));
            tgfx.putString(0, s.getRows() - 1, ">>> ");

            tgfx.setBackgroundColor(TextColor.Indexed.fromRGB(15, 15, 15));
            tgfx.putString(4, SMPHookError.throwIfFail(terminal::getTerminalSize).getRows() - 1, buffer.toString());

            SMPHookError.throwIfFail(t::flush);
        });

        TaskExecutor.execute(() -> {
            int cursorPosition = 0;

            while (true) {
                KeyStroke keyStroke = terminal.readInput();
                KeyType   keyType   = keyStroke.getKeyType();

                if (keyType == KeyType.Backspace && buffer.length() != 0) {
                    tgfx.putString(4, terminal.getTerminalSize().getRows() - 1, buffer.toString().replaceAll(".", "\s"));
                    buffer.deleteCharAt(cursorPosition != 0 ? --cursorPosition : 0);
                } else if (keyType == KeyType.ArrowLeft && cursorPosition != 0) {
                    cursorPosition--;
                } else if (keyType == KeyType.ArrowRight && cursorPosition != buffer.length()) {
                    cursorPosition++;
                } else if (keyType == KeyType.Enter) {
                    String command = buffer.toString();
                    tgfx.putString(4, terminal.getTerminalSize().getRows() - 1, command.replaceAll(".", "\s"));
                    buffer.delete(0, buffer.length());
                    cursorPosition = 0;
                } else if (keyType == KeyType.Character) {
                    buffer.insert(cursorPosition, keyStroke.getCharacter());
                    cursorPosition++;
                }

                tgfx.setBackgroundColor(TextColor.Indexed.fromRGB(15, 15, 15));
                tgfx.putString(4, terminal.getTerminalSize().getRows() - 1, buffer.toString());
                terminal.setCursorPosition(4 + cursorPosition, terminal.getTerminalSize().getRows() - 1);

                terminal.flush();
            }
        });
    }

    public static void setup() throws SMPHookError {
        SMPHookConfig config = SMPHookConfig.loadDefaults();
        System.out.printf("Created default hook.json file");
        System.out.println(config);
    }

    public static void main(String[] args) throws Exception {
        // default to use the GUI for high-level usage
        if (args.length == 0) args = new String[] { ARG_NOGUI };

        if (args.length == 1) {
            switch (args[0]) {
                case ARG_NOGUI -> tui();
                case ARG_SETUP -> setup();
            }
        } else System.err.println("Illegal number of arguments supplied");
    }
}
