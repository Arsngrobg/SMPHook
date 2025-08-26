package dev.arsngrobg.smphook.core;

/**
 * <p>Tagging interface to say that a class `implements` an instance - i.e. allows the instantiation of the class (indirectly/directly).
 *    Correct use of this interface is that <b>all</b> the methods <b>must</b> be implemented - even if not directly enforced by the compiler or linter.
 *    It is an enforced programming standard with the <b>SMPHook</b> project, no implicit super definitions are allowed.
 * </p>
 *
 * <p>This groups up the {@link dev.arsngrobg.smphook.core.Hashable}, {@link dev.arsngrobg.smphook.core.Comparable}, and {@link dev.arsngrobg.smphook.core.Representable} interfaces.</p>
 *
 * @author Arsngrobg
 * @since  0.0.1
 */
public interface Instance extends Hashable, Comparable, Representable {
    @Override
    int hashCode();

    @Override
    boolean equals(Object o);

    @Override
    String toString();
}
