package dev.arsngrobg.smphook;

import dev.arsngrobg.smphook.core.Version;

/**
 * <p>The entry point for the program.</p>
 *
 * @author Arsngrobg
 * @since  0.0.1
 */
public final class SMPHook {
    /** <p>Enables certain features used for debugging purposes.</p> */
    public static final boolean DEBUG = true;

    public static void main(String[] args) {
        System.out.printf("SMPHook v%s", Version.getClient());
    }
}
