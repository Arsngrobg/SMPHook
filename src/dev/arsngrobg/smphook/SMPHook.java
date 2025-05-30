package dev.arsngrobg.smphook;

/**
 * <p>The entry point for the program.</p>
 * 
 * @author Arsngrobg
 * @since  1.0
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

    public static void main(String[] args) throws SMPHookError {
        
    }
}
