package dev.arsngrobg.smphook;

/**
 * <p>The entry point for the program.</p>
 * 
 * @author Arsngrobg
 * @since  0.1.0
 */
public final class SMPHook {
    /** <p>Enables certain features used for debugging purposes.</p> */
    public static final boolean DEBUG = true;

    /** <p>The current <b>MAJOR</b> version of SMPHook. It is incremented when an incompatable API change occurs.</p> */
    public static final int VERSION_MAJOR = 0;
    /** <p>The current <b>MINOR</b> version of SMPHook. It is incremented when backward compatible functionality is added.</p> */
    public static final int VERSION_MINOR = 1;
    /** <p>The current <b>PATCH</b> version of SMPHook. It is incremented when backward compatible bug fixes are made.</p> */
    public static final int VERSION_PATCH = 0;

    /**
     * <p>Formats the {@link #VERSION_MAJOR}, {@link #VERSION_MINOR}, and {@link #VERSION_PATCH} into the format {@code MAJOR}.{@code MINOR}.{@code PATCH}.</p>
     * 
     * @return the string representation of {@link #VERSION_MAJOR}, {@link #VERSION_MINOR}, and {@link #VERSION_PATCH} into one string
     */
    public static String getVersion() {
        return String.format("%d.%d.%d", VERSION_MAJOR, VERSION_MINOR, VERSION_PATCH);
    }

    public static void main(String[] args) {

    }
}
