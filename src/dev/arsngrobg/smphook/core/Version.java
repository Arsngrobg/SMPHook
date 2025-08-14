package dev.arsngrobg.smphook.core;

// TODO: Grab the latest available STABLE version of SMPHook using GitHub API
//       (https://api.github.com/repos/arsngrobg/SMPHook/releases/latest)

/**
 * <p>The {@code Version} class acts as metadata for a version of the SMPHook client.</p>
 *
 * @author Arsngrobg
 * @since  0.0.1
 */
public final class Version implements Instance {
    /**
     * <p>Semantic versioning is used to define each update for the SMPHook client.
     *    These components should match the <b>current</b> version of this SMPHook client and should be positive.
     * </p>
     *
     * <p><b>NOTICE: Ensure that the values are correct before pushing any future changes!</b></p>
     */
    public static final int
        VERSION_MAJOR = 0,
        VERSION_MINOR = 0,
        VERSION_PATCH = 2;

    /**
     * <p>The current build state of this SMPHook client version.</p>
     *
     * <p><b>NOTICE: Ensure that this value is correct before pushing any future changes!</b></p>
     */
    public static final BuildState VERSION_BUILD = BuildState.ALPHA;

    /**
     * <p>The set of states that the SMPHook client can be versioned as.
     *    It is metadata attached to the version number and is purely a marker of stability.
     * </p>
     *
     * @author Arsngrobg
     * @since  0.0.1
     */
    public enum BuildState {
        /** <p>This build is in active development and subject to changes.</p> */
        ALPHA,
        /** <p>This build is feature-complete and under public testing.</p> */
        BETA,
        /** <p>This build is stable; is typically the default state.</p> */
        STABLE
    }

    /**
     * <p>This is the current version of SMPHook currently running on the user's device.</p>
     *
     * @return the client version
     *
     * @since 0.0.1
     */
    public static Version getClient() {
        return CLIENT_VERSION;
    }

    private static final Version CLIENT_VERSION = new Version(VERSION_MAJOR, VERSION_MINOR, VERSION_PATCH, VERSION_BUILD);

    private final int major, minor, patch;
    private final BuildState build;

    private Version(int major, int minor, int patch, BuildState build) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.build = build;
    }

    /**
     * <p>Compares this version with the supplied {@code ver}.</p>
     *
     * <p>The build state is ignored as it is only an identifier.</p>
     *
     * @param ver - the version to compare with
     * @return {@code true} if this version is newer than {@code ver}; {@code false} if otherwise
     *
     * @since 0.0.1
     * @see   #isNewerThan(int, int, int)
     */
    public boolean isNewerThan(Version ver) {
        if (ver == null) return false;
        return isNewerThan(ver.major, ver.minor, ver.patch);
    }

    /**
     * <p>Compares this version with the supplied {@code major}, {@code minor}, and {@code patch} version components.</p>
     *
     * @param major - the major version component
     * @param minor - the minor version component
     * @param patch - the patch version component
     * @return {@code true} if this version is newer than {@code ver}; {@code false} if otherwise
     *
     * @since 0.0.1
     * @see   #isNewerThan(Version)
     */
    public boolean isNewerThan(int major, int minor, int patch) {
        if (this.major > major) return true;
        if (this.major < major) return false;

        if (this.minor > minor) return true;
        if (this.minor < minor) return false;

        return this.patch > patch;
    }

    /**
     * <p>Returns the major version component.</p>
     *
     * @return the major version number as an integer
     *
     * @since 0.0.1
     */
    public int getMajorComponent() {
        return major;
    }

    /**
     * <p>Returns the minor version component.</p>
     *
     * @return the minor version number as an integer
     *
     * @since 0.0.1
     */
    public int getMinorComponent() {
        return minor;
    }

    /**
     * </p>Returns the patch version component.</p>
     *
     * @return the patch version number as an integer
     *
     * @since 0.0.1
     */
    public int getPatchComponent() {
        return patch;
    }

    /**
     * <p>Checks if this version of SMPHook is in {@code ALPHA}.</p>
     *
     * @return {@code true} if the build state is {@code ALPHA}; {@code false} otherwise
     *
     * @since 0.0.1
     */
    public boolean inAlpha() {
        return build == BuildState.ALPHA;
    }

    /**
     * <p>Checks if this version of SMPHook is in {@code BETA}.</p>
     *
     * @return {@code true} if the build state is {@code BETA}; {@code false} otherwise
     *
     * @since 0.0.1
     */
    public boolean inBeta() {
        return build == BuildState.BETA;
    }

    /**
     * <p>Checks if this version of SMPHook is {@code STABLE}.</p>
     *
     * @return {@code true} if the build state is {@code STABLE}; {@code false} otherwise
     *
     * @since 0.0.1
     */
    public boolean isStable() {
        return build == BuildState.STABLE;
    }

    @Override
    public int hashCode() {
        return Hashable.hashOf(major, minor, patch, build);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Version asVer)) return false;
        return hashCode() == asVer.hashCode();
    }

    @Override
    public String toString() {
        if (build != BuildState.STABLE) {
            return String.format("%d.%d.%d-%s", major, minor, patch, build.toString().toLowerCase());
        }

        return String.format("%d.%d.%d", major, minor, patch);
    }
}
