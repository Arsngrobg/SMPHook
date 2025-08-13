package dev.arsngrobg.smphook.core;

/**
 * <p>Tagging interface to define that an {@code Object} or {@code Instance} is {@code Hashable}.</p>
 * 
 * <p><i>Wraps the {@link Object#hashCode()} instance method.</i></p>
 * 
 * @author Arsngrobg
 * @since  0.0.1
 */
public interface Hashable {
    /**
     * <p><i>This method is more preferable over Java's methods for its greater compatibility.</i></p>
     * 
     * <p>Hashes each element in the supplied argument list of {@code objects}, in which the order of the objects matter.</p>
     * 
     * <p>Each element will rely on its inherent {@link #hashCode()} - via {@link System#identityHashCode(Object)}.
     *    However, if the element is an array it will apply another invocation to this method to get the hash of each independent element of the sub array - and so on.
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
     static int hashOf(Object... objects) {
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
                hash = (arr.equals(objects)) ? objects.hashCode() : Hashable.hashOf(arr);
            } else hash = System.identityHashCode(obj);

            result += hash;
        }

        return result;
    }

    /**
     * <p>Returns the hash-code of this specific `permutation` of this object.</p>
     * 
     * <p>For the {@link Hashable#hashCode()} contract to be fulfilled, it must agree to these requirements:
     *    <ul>
     *       <li>It must return a consistent value - it should align with the current state of this instance</li>
     *       <li>If two objects are equal (as described by {@link Comparable#equals(Object) method} - then the hashcode should also be the same</li>
     *       <li>Two non-equal objects that are not equal may produce distinct hashcode in order to increase performance of certain collection types that utilize this hashcode.</li>
     *    </ul>
     * </p> 
     * 
     * @return the hashcode of this {@code Object} or {@code Instance}
     * 
     * @since 0.1.0
     */
    @Override
    int hashCode();
}
