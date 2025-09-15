package dev.arsngrobg.smphook.core;

/**
 * <p>The {@code HashFunction} is a function that computes the hash of an object, depending on the generic type {@code T}.</p>
 * <p>This interface defines a set of static methods that have the same signature, and executes as if it were a {@code HashFunction}.</p>
 *
 * @param  <T> the argument type for this {@code HashFunction}
 * @author     Arsngrobg
 * @since      0.0.0-pre-alpha
 */
@FunctionalInterface
public interface HashFunction<T> {
    /**
     * <p>Computes the hash of the variadic argument list of generic objects.</p>
     *
     * @param  objs the variadic list of generic objects
     * @return      the composite hash of the variadic list of generic objects
     * @author      Arsngrobg
     * @since       0.0.0-pre-alpha
     */
    static int hash(Object... objs) {
        throw new UnsupportedOperationException("HashFunction:hash(Object... objs) not implemented."); // TODO
    }

    /**
     * <p>Computes the hash for the provided generic {@code Object}.</p>
     * <p>Where this function differs from {@link HashFunction#hashObject(Object)} is that it infers the given {@code Object}'s type.</p>
     *
     * @param  obj the generic {@code Object} to compute the hash of
     * @return     the hash of the generic {@code Object}
     * @author     Arsngrobg
     * @since      0.0.0-pre-alpha
     */
    static int hash(Object obj) {
        if (obj == null) return 0;

        if (obj instanceof Long      l) return HashFunction.hashLong  (l);
        if (obj instanceof Integer   i) return HashFunction.hashInt   (i);
        if (obj instanceof Short     s) return HashFunction.hashShort (s);
        if (obj instanceof Byte      b) return HashFunction.hashByte  (b);
        if (obj instanceof Float     f) return HashFunction.hashFloat (f);
        if (obj instanceof Double    d) return HashFunction.hashDouble(d);
        if (obj instanceof Character c) return HashFunction.hashChar  (c);

        if (obj.getClass().isArray()) {
            Class<?> type = obj.getClass().getComponentType();
            if (type == Long.class     ) return HashFunction.hashLongArray  ((long[]  ) obj);
            if (type == Integer.class  ) return HashFunction.hashIntArray   ((int[]   ) obj);
            if (type == Short.class    ) return HashFunction.hashShortArray ((short[] ) obj);
            if (type == Byte.class     ) return HashFunction.hashByteArray  ((byte[]  ) obj);
            if (type == Float.class    ) return HashFunction.hashFloatArray ((float[] ) obj);
            if (type == Double.class   ) return HashFunction.hashDoubleArray((double[]) obj);
            if (type == Character.class) return HashFunction.hashCharArray  ((char[]  ) obj);
            else                         return HashFunction.hashObjectArray((Object[]) obj);
        }

        return HashFunction.hashObject(obj);
    }

    /**
     * <p>Computes the hash for the provided {@code Object}.</p>
     *
     * @param  obj the {@code Object} to be hashed
     * @return     the hash of the {@code Object}
     * @author     Arsngrobg
     * @since      0.0.0-pre-alpha
     */
    static int hashObject(Object obj) {
        return (obj != null) ? obj.hashCode() : 0;
    }

    /**
     * <p>Computes the hash for the provided {@code long}.</p>
     *
     * @param  l the {@code long} to be hashed
     * @return   the {@code int} downcast of the provided {@code long}
     * @author   Arsngrobg
     * @since    0.0.0-pre-alpha
     */
    static int hashLong(long l) {
        return (int) l;
    }

    /**
     * <p>Computes the hash for the provided {@code int}.</p>
     *
     * @param  i the {@code int} to be hashed
     * @return   the {@code int}
     * @author   Arsngrobg
     * @since    0.0.0-pre-alpha
     */
    static int hashInt(int i) {
        return i;
    }

    /**
     * <p>Computes the hash for the provided {@code short}</p>
     *
     * @param  s the {@code short} to be hashed
     * @return   the {@code int} upcast of the provided {@code short}
     */
    static int hashShort(short s) {
        return (int) s;
    }

    /**
     * <p>Computes the hash for the provided {@code byte}.</p>
     *
     * @param  b the {@code byte} to be hashed
     * @return   the {@code int} upcast of the provided {@code byte}
     * @author   Arsngrobg
     * @since    0.0.0-pre-alpha
     */
    static int hashByte(byte b) {
        return (int) b;
    }

    /**
     * <p>Computes the hash for the provided {@code float}.</p>
     *
     * @param  f the {@code float} to be hashed
     * @return   the {@code float} converted into its integer bit representation
     * @author   Arsngrobg
     * @since    0.0.0-pre-alpha
     */
    static int hashFloat(float f) {
        return Float.floatToIntBits(f);
    }

    /**
     * <p>Computes the hash for the provided {@code double}.</p>
     *
     * @param  f the {@code double} to be hashed
     * @return   the {@code double} converted into its integer bit representation
     * @author   Arsngrobg
     * @since    0.0.0-pre-alpha
     */
    static int hashDouble(double d) {
        return HashFunction.hashLong(Double.doubleToLongBits(d));
    }

    /**
     * <p>Computes the hash for the provided {@code char}.</p>
     *
     * @param  c the {@code char} to be hashed
     * @return   the {@code int} upcast of the provided {@code char}
     * @author   Arsngrobg
     * @since    0.0.0-pre-alpha
     */
    static int hashChar(char c) {
        return (int) c;
    }

    /**
     * <p>Computes the hash for the provided {@code Object[]}.</p>
     *
     * @param  longArr the {@code Object[]} to be hashed
     * @return         the hash of each {@code Object} in the {@code Object[]}
     * @author         Arsngrobg
     * @since          0.0.0-pre-alpha
     */
    static int hashObjectArray(Object[] objArr) {
        if (objArr == null || objArr.length == 0) return 0;

        int hash = 1;
        for (Object o : objArr) {
            hash = 31 * hash + HashFunction.hashObject(o);
        }

        return hash;
    }

    /**
     * <p>Computes the hash for the provided {@code long[]}.</p>
     *
     * @param  longArr the {@code long[]} to be hashed
     * @return         the hash of each {@code long} in the {@code long[]}
     * @author         Arsngrobg
     * @since          0.0.0-pre-alpha
     */
    static int hashLongArray(long[] longArr) {
        if (longArr == null || longArr.length == 0) return 0;

        int hash = 1;
        for (long l : longArr) {
            hash = 31 * hash + HashFunction.hashLong(l);
        }

        return hash;
    }

    /**
     * <p>Computes the hash for the provided {@code int[]}.</p>
     *
     * @param  intArr the {@code int[]} to be hashed
     * @return        the hash of each {@code int} in the {@code int[]}
     * @author        Arsngrobg
     * @since         0.0.0-pre-alpha
     */
    static int hashIntArray(int[] intArr) {
        if (intArr == null || intArr.length == 0) return 0;

        int hash = 1;
        for (int i : intArr) {
            hash = 31 * hash + HashFunction.hashInt(i);
        }

        return hash;
    }

    /**
     * <p>Computes the hash for the provided {@code short[]}.</p>
     *
     * @param  shortArr the {@code short[]} to be hashed
     * @return          the hash of each {@code short} in the {@code short[]}
     * @author          Arsngrobg
     * @since           0.0.0-pre-alpha
     */
    static int hashShortArray(short[] shortArr) {
        if (shortArr == null || shortArr.length == 0) return 0;

        int hash = 1;
        for (short s : shortArr) {
            hash = 31 * hash + HashFunction.hashShort(s);
        }

        return hash;
    }

    /**
     * <p>Computes the hash for the provided {@code byte[]}.</p>
     *
     * @param  byteArr the {@code byte[]} to be hashed
     * @return         the hash of each {@code byte} in the {@code byte[]}
     * @author         Arsngrobg
     * @since          0.0.0-pre-alpha
     */
    static int hashByteArray(byte[] byteArr) {
        if (byteArr == null || byteArr.length == 0) return 0;

        int hash = 1;
        for (byte b : byteArr) {
            hash = 31 * hash + HashFunction.hashByte(b);
        }

        return hash;
    }

    /**
     * <p>Computes the hash for the provided {@code float[]}.</p>
     *
     * @param  floatArr the {@code float[]} to be hashed
     * @return          the hash of each {@code float} in the {@code float[]}
     * @author          Arsngrobg
     * @since           0.0.0-pre-alpha
     */
    static int hashFloatArray(float[] floatArr) {
        if (floatArr == null || floatArr.length == 0) return 0;

        int hash = 1;
        for (float f : floatArr) {
            hash = 31 * hash + HashFunction.hashFloat(f);
        }

        return hash;
    }

    /**
     * <p>Computes the hash for the provided {@code double[]}.</p>
     *
     * @param  doubleArr the {@code double[]} to be hashed
     * @return          the hash of each {@code double} in the {@code double[]}
     * @author          Arsngrobg
     * @since           0.0.0-pre-alpha
     */
    static int hashDoubleArray(double[] doubleArr) {
        if (doubleArr == null || doubleArr.length == 0) return 0;

        int hash = 1;
        for (double d : doubleArr) {
            hash = 31 * hash + HashFunction.hashDouble(d);
        }

        return hash;
    }

    /**
     * <p>Computes the hash for the provided {@code char[]}.</p>
     *
     * @param  charArr the {@code char[]} to be hashed
     * @return         the hash of each {@code char} in the {@code char[]}
     * @author         Arsngrobg
     * @since          0.0.0-pre-alpha
     */
    static int hashCharArray(char[] charArr) {
        if (charArr == null || charArr.length == 0) return 0;

        int hash = 1;
        for (char c : charArr) {
            hash = 31 * hash + HashFunction.hashChar(c);
        }

        return hash;
    }

    /**
     * <p>Computes the hash for the provided {@code value} object.</p>
     *
     * @param  value the object to be hashed
     * @return       the hash of this object
     * @author       Arsngrobg
     * @since        0.0.0-pre-alpha
     */
    int compute(T value);
}
