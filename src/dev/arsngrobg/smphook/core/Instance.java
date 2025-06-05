package dev.arsngrobg.smphook.core;

import dev.arsngrobg.smphook.SMPHook;

/**
 * <p>This interface must be implemented by all instanciable classes in the <b>SMPHook</b> system.</p>
 * 
 * <p>It ensures that every instanciable type will implement its own {@link #hashCode()}, {@link #equals(Object)}, and {@link #toString()} methods.</p>
 * 
 * @author Arsngrobg
 * @since  0.1.0
 */
public interface Instance {
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
     *       (31 + h[n - 1]) * (31 + h[n-2]) * (...) * (31 + h[0])
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
                hash = (arr.equals(objects)) ? objects.hashCode() : Instance.hashOf(arr);
            } else hash = obj.hashCode();

            result += hash;
        }

        return result;
    }

    /**
     * <p>Provides a relatively unique values for this instance type.
     *    Such that, for two given values that are objectively equal to eachother (i.e. {@link #equals(Object)}), the hashcode for both instances are also equal to eachother.
     *    So, in a sense:
     *    <pre><blockquote>
     *       [ o1.equals(o2) ] === [ o1.hashCode() == o2.hashCode() ]
     *    </blockquote></pre>
     * </p>
     * 
     * @return the hashcode of this instance
     * 
     * @since 0.1.0
     */
    @Override
    int hashCode();

    /**
     * <p>Compares this object to the supplied {@code obj}.</p>
     * 
     * <p>For this method to be considered a valid {@link #equals(Object)} implementation:
     *    <ul>
     *       <li>it must be reflective - {@code o1.equals(o2) == o2.equals(o1)}</li>
     *       <li>it must only compare to to objects of the same type as this object</li>
     *       <li>{@code o1.equals(null)} must always evaluate to {@code false}</li>
     *    </ul>
     * </p>
     * 
     * @param  obj - the object to compare this object to
     * @return {@code true} if this object is equal to this object, {@code false} if otherwise
     * 
     * @since 0.1.0
     */
    @Override
    boolean equals(Object obj);

    /**
     * <p>Formats this object into its {@code String} representation.</p>
     * 
     * @return the {@code String} representation of this {@code Instance}
     * 
     * @since 0.1.0
     */
    @Override
    String toString();
}
