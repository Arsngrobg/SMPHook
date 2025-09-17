package dev.arsngrobg.smphook.core;

/**
 * <p>The {@code HashFunction} interface defines a function that computes the hash of a particular object of type {@code T}.</p>
 * <p>This is used primarily as a meaningful utility class for a set of hash implementations for certain types - mainly primitive types.</p>
 * <p>All static helper methods are implicit definitions of {@code HashFunction}s and can be represented as such:
 *    <pre>
 *       HashFunction<Integer> hashFn = HashFunction::hashInt;<br>hashFn.compute(3); // just returns 3
 *    </pre>
 * </p>
 *
 * @param  <T> the argument type of this {@code HashFunction}
 * @author     Arsngrobg
 * @since      0.0.0-pre-alpha
 */
@FunctionalInterface
public interface HashFunction<T> {
    /** <p>The prime number used in compositing a hash for a sequence of values.</p> */
    static int HASH_PRIME = 31;

    /**
     * <p>Computes the composite hash of the provided variadic list of generic values.</p>
     * <p>This is a more ergonomic method of hashing over:
     *    <pre>
     *       int hash = HashFunction.hash(new Object[] {val1, val2, val3});
     *    </pre>
     * </p>
     *
     * @param  values the variadic list of generic values to be hashed
     * @return        the composite hash of the variadic list of generic values
     * @author        Arsngrobg
     * @since         0.0.0-pre-alpha
     */
    static int hashOf(Object... values) {
        return HashFunction.hash(values);
    }

    /**
     * <p>Computes the composite hash of the provided generic {@code values} array.</p>
     * <p>This function will infer the type of each element of the array.</p>
     *
     * @param  values the generic array of elements to be hashed
     * @return        the composite hash of the individual elements of the {@code values} array
     * @author        Arsngrobg
     * @since         0.0.0-pre-alpha
     */
    static int hash(Object[] values) {
        if (values == null) return 0;

        int hash = 1;
        for (Object value : values) {
            hash = HashFunction.HASH_PRIME * hash + HashFunction.hash(value);
        }

        return hash;
    }

    /**
     * <p>Computes the hash of the provided generic {@code value}.</p>
     * <p>This function will infer the type of this {@code value}, and appropriately apply the correct hash operation.</p>
     *
     * @param  value the generic value to be hashed
     * @return       the hash of this generic {@code value}
     * @author       Arsngrobg
     * @since        0.0.0-pre-alpha
     */
    static int hash(Object value) {
        if (value == null) return 0;

        return switch (value) {
            case long[]   la -> HashFunction.hashLongArray  (la);
            case int[]    ia -> HashFunction.hashIntArray   (ia);
            case short[]  sa -> HashFunction.hashShortArray (sa);
            case byte[]   ba -> HashFunction.hashByteArray  (ba);
            case double[] da -> HashFunction.hashDoubleArray(da);
            case float[]  fa -> HashFunction.hashFloatArray (fa);
            case char[]   ca -> HashFunction.hashCharArray  (ca);
            case Object[] oa -> HashFunction.hashObjectArray(oa);
            case Long      l -> HashFunction.hashLong       (l);
            case Integer   i -> HashFunction.hashInt        (i);
            case Short     s -> HashFunction.hashShort      (s);
            case Byte      b -> HashFunction.hashByte       (b);
            case Double    d -> HashFunction.hashDouble     (d);
            case Float     f -> HashFunction.hashFloat      (f);
            case Character c -> HashFunction.hashChar       (c);
            default          -> HashFunction.hashObject     (value);
        };
    }

    /**
     * <p>Computes the hash of the provided {@code Object}.</p>
     * <p>It invokes its default {@linkplain Object#hashCode()} method.</p>
     *
     * @param  o the {@code Object} to be hashed
     * @return   the hash of this {@code Object}
     * @author   Arsngrobg
     * @since    0.0.0-pre-alpha
     */
    static int hashObject(Object o) {
        return (o != null) ? HashFunction.hash(o) : 0;
    }

    /**
     * <p>Computes the hash of the provided {@code long} value.</p>
     * <p>It unsigned-shifts the high 32-bits to low 32-bits and applies the XOR operation.</p>
     *
     * @param  l the {@code long} to be hashed
     * @return   the hash of this {@code l}
     * @author   Arsngrobg
     * @since    0.0.0-pre-alpha
     */
    static int hashLong(long l) {
        return (int) (l ^ (l >>> 32));
    }

    /**
     * <p>Computes the hash of the provided {@code int} value.</p>
     * <p>It returns the original value passed into this method.</p>
     *
     * @param  i the {@code int} to be hashed
     * @return   the hash of this {@code int}
     * @author   Arsngrobg
     * @since    0.0.0-pre-alpha
     */
    static int hashInt(int i) {
        return i;
    }

    /**
     * <p>Computes the hash of the provided {@code short}.</p>
     * <p>It upcasts the {@code short} as an {@code int}.</p>
     *
     * @param  s the {@code short} to be hashed
     * @return   the hash of this {@code short}
     * @author   Arsngrobg
     * @since    0.0.0-pre-alpha
     */
    static int hashShort(short s) {
        return (int) s;
    }

    /**
     * <p>Computes the hash of the provided {@code byte}.</p>
     * <p>It upcasts the {@code byte} as an {@code int}.</p>
     *
     * @param  b the {@code byte} to be hashed
     * @return   the hash of this {@code byte}
     * @author   Arsngrobg
     * @since    0.0.0-pre-alpha
     */
    static int hashByte(byte b) {
        return (int) b;
    }

    /**
     * <p>Computes the hash of the provided {@code double}.</p>
     * <p>It converts the {@code double} to its 64-bit representation and uses the 32-bit compressed representation.</p>
     *
     * @param  d the {@code double} to be hashed
     * @return   the hash of this {@code double}
     * @author   Arsngrobg
     * @since    0.0.0-pre-alpha
     */
    static int hashDouble(double d) {
        long bits = Double.doubleToLongBits(d);
        return (int) (bits ^ (bits >>> 32));
    }

    /**
     * <p>Computes the hash of the provided {@code float}.</p>
     * <p>It converts the {@code float} to its 32-bit representation.</p>
     *
     * @param  f the {@code float} to be hashed
     * @return   the hash of this {@code float}
     * @author   Arsngrobg
     * @since    0.0.0-pre-alpha
     */
    static int hashFloat(float f) {
        return (int) Float.floatToIntBits(f);
    }

    /**
     * <p>Computes the hash of the provided {@code char}.</p>
     * <p>It converts the character into its codepoint.</p>
     *
     * @param  c the {@code char} to be hashed
     * @return   the hash of this {@code char}
     * @author   Arsngrobg
     * @since    0.0.0-pre-alpha
     */
    static int hashChar(char c) {
        return (int) c;
    }

    /**
     * <p>Computes the hash of the provided {@code Object[]} by recursively iterating through all its elements.</p>
     *
     * @param  oa the {@code Object[]} to be hashed
     * @return    the hash of this {@code Object[]}
     * @author    Arsngrobg
     * @since     0.0.0-pre-alpha
     */
    static int hashObjectArray(Object[] oa) {
        if (oa == null || oa.length == 0) return 0;
        if (oa.length == 1) return HashFunction.hashObject(oa[0]);

        int hash = 1;
        for (Object o : oa) {
            if (o == oa) throw new IllegalStateException("Cannot recursively hash the supplied array.");
            hash = HashFunction.HASH_PRIME * hash + HashFunction.hashObject(o);
        }

        return hash;
    }

    /**
     * <p>Computes the composite hash of the provided {@code long[]} by iterating through all its elements.</p>
     *
     * @param  la the {@code long[]} to be hashed
     * @return    the composite hash of the {@code long[]}
     * @author    Arsngrobg
     * @since     0.0.0-pre-alpha
     */
    static int hashLongArray(long[] la) {
        if (la == null || la.length == 0) return 0;
        if (la.length == 1) return HashFunction.hashLong(la[0]);

        int hash = 1;
        for (long l : la) {
            hash = HashFunction.HASH_PRIME * hash + HashFunction.hashLong(l);
        }

        return hash;
    }

    /**
     * <p>Computes the composite hash of the provided {@code int[]} by iterating through all its elements.</p>
     *
     * @param  ia the {@code int[]} to be hashed
     * @return    the composite hash of the {@code int[]}
     * @author    Arsngrobg
     * @since     0.0.0-pre-alpha
     */
    static int hashIntArray(int[] ia) {
        if (ia == null || ia.length == 0) return 0;
        if (ia.length == 1) return HashFunction.hashInt(ia[0]);

        int hash = 1;
        for (int i : ia) {
            hash = HashFunction.HASH_PRIME * hash + HashFunction.hashInt(i);
        }

        return hash;
    }

    /**
     * <p>Computes the composite hash of the provided {@code short[]} by iterating through all its elements.</p>
     *
     * @param  sa the {@code short[]} to be hashed
     * @return    the composite hash of the {@code short[]}
     * @author    Arsngrobg
     * @since     0.0.0-pre-alpha
     */
    static int hashShortArray(short[] sa) {
        if (sa == null || sa.length == 0) return 0;
        if (sa.length == 1) return HashFunction.hashShort(sa[0]);

        int hash = 1;
        for (short i : sa) {
            hash = HashFunction.HASH_PRIME * hash + HashFunction.hashShort(i);
        }

        return hash;
    }

    /**
     * <p>Computes the composite hash of the provided {@code byte[]} by iterating through all its elements.</p>
     *
     * @param  ba the {@code byte[]} to be hashed
     * @return    the composite hash of the {@code byte[]}
     * @author    Arsngrobg
     * @since     0.0.0-pre-alpha
     */
    static int hashByteArray(byte[] ba) {
        if (ba == null || ba.length == 0) return 0;
        if (ba.length == 1) return HashFunction.hashByte(ba[0]);

        int hash = 1;
        for (byte b : ba) {
            hash = HashFunction.HASH_PRIME * hash + HashFunction.hashByte(b);
        }

        return hash;
    }

    /**
     * <p>Computes the composite hash of the provided {@code double[]} by iterating through all its elements.</p>
     *
     * @param  da the {@code double[]} to be hashed
     * @return    the composite hash of the {@code double[]}
     * @author    Arsngrobg
     * @since     0.0.0-pre-alpha
     */
    static int hashDoubleArray(double[] da) {
        if (da == null || da.length == 0) return 0;
        if (da.length == 1) return HashFunction.hashDouble(da[0]);

        int hash = 1;
        for (double d : da) {
            hash = HashFunction.HASH_PRIME * hash + HashFunction.hashDouble(d);
        }

        return hash;
    }

    /**
     * <p>Computes the composite hash of the provided {@code float[]} by iterating through all its elements.</p>
     *
     * @param  fa the {@code float[]} to be hashed
     * @return    the composite hash of the {@code float[]}
     * @author    Arsngrobg
     * @since     0.0.0-pre-alpha
     */
    static int hashFloatArray(float[] fa) {
        if (fa == null || fa.length == 0) return 0;
        if (fa.length == 1) return HashFunction.hashFloat(fa[0]);

        int hash = 1;
        for (float f : fa) {
            hash = HashFunction.HASH_PRIME * hash + HashFunction.hashFloat(f);
        }

        return hash;
    }

    /**
     * <p>Computes the composite hash of the provided {@code char[]} by iterating through all its elements.</p>
     *
     * @param  ca the {@code char[]} to be hashed
     * @return    the composite hash of the {@code char[]}
     * @author    Arsngrobg
     * @since     0.0.0-pre-alpha
     */
    static int hashCharArray(char[] ca) {
        if (ca == null || ca.length == 0) return 0;
        if (ca.length == 1) return HashFunction.hashChar(ca[0]);

        int hash = 1;
        for (char c : ca) {
            hash = HashFunction.HASH_PRIME * hash + HashFunction.hashChar(c);
        }

        return hash;
    }

    /**
     * <p>Computes the hash of the provided {@code value}.</p>
     * <p>Appropriate {@code null} checks should be written for this {@code value}.</p>
     *
     * @param  value the {@code Object} to be hashed
     * @return       the hash of {@code value}
     * @author       Arsngrobg
     * @since        0.0.0-pre-alpha
     */
    int compute(T value);
}
