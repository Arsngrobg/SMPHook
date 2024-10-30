package arsngrobg.smphook.server;

/** Set of values that describe units that the JVM recognise when processing heap arguements. */
public enum HeapUnit {
    BYTE    ('B'),
    KILOBYTE('K'),
    MEGABYTE('M'),
    GIGABYTE('G');

    private char suffix;

    HeapUnit(char suffix) {
        this.suffix = suffix;
    }

    /**
     * The suffix that this value represents.
     * @return the character suffix that this value represents
     */
    public char getSuffix() {
        return suffix;
    }
}
