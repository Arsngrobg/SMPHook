package dev.arsngrobg.smphook.core;

import java.util.Objects;

/**
 * <p>The {@code Version} class is metadata for a version of the SMPHook client.</p>
 * <p>The <b>client</b> version of <b>SMPHook</b> is </p>
 *
 * @author  Arsngrobg
 * @since   0.0.0-pre-alpha
 */
public final class Version {
    /**
     * <p>This is the release type of this current version of the SMPHook client.</p>
     * <p><b>DEVNOTE: MAKE SURE THIS IS CORRECT BEFORE PUSHING CHANGES!</b></p>
     */
    public static final ReleaseType VERSION_RELEASE = ReleaseType.PRE_ALPHA;
    /**
     * <p>The {@code major}, {@code minor}, and {@code patch} components of this current version of the SMPHook client.</p>
     * <p><b>DEVNOTE: MAKE SURE THIS IS CORRECT BEFORE PUSHING CHANGES!</b></p>
     */
    public static final int
        VERSION_MAJOR = 0,
        VERSION_MINOR = 0,
        VERSION_PATCH = 0;

    private static final Version CLIENT_VERSION = new Version(VERSION_MAJOR, VERSION_MINOR, VERSION_PATCH, VERSION_RELEASE);

    /**
     * <p>Gets the version of the SMPHook client currently running on this device.</p>
     *
     * @return the client version
     * @author Arsngrobg
     * @since  0.0.0-pre-alpha
     */
    public static Version getClientVersion() {
        return CLIENT_VERSION;
    }

    /**
     * <p>Gets the current version of the SMPHook client available.</p>
     *
     * @return the latest version available
     * @author Arsngrobg
     * @since  NOT_YET_IMPLEMENTED
     */
    public static Version getLatestVersion() {
        // TODO: Get latest SMPHook client version using the GitHub API
        throw new UnsupportedOperationException("Version:getLatestVersion not implemented.");
    }

    /**
     * <p>The {@code ReleaseType} labels the current version.
     *    The release types have precedence over eachother; in the order that they are defined in.
     * </p>
     *
     * @author Arsngrobg
     * @since  0.0.0-pre-alpha
     */
    public enum ReleaseType {
        /** <p>Internal development - not intended for public use.</p> */
        PRE_ALPHA,
        /** <p>Internal testing - intended for select group of testers.</p> */
        ALPHA,
        /** <p>Public testing - intended for everybody.</p> */
        BETA,
        /** <p>Feature complete - bug fixes only.</p> */
        RELEASE_CANDIDATE,
        /** <p>Feature complete & all known bugs fixed.</p> */
        STABLE;

        @Override
        public String toString() {
            return switch (this) {
                case RELEASE_CANDIDATE -> "rc";
                default -> name().toLowerCase();
            };
        }
    }

    private final int major, minor, patch;
    private final ReleaseType release;

    private Version(int major, int minor, int patch, ReleaseType release) {
        this.major   = major;
        this.minor   = minor;
        this.patch   = patch;
        this.release = release;
    }

    /**
     * <p>Checks whether this {@code Version} is newer than the {@code Version} provided.</p>
     *
     * @param  ver the {@code Version} to compare with this {@code Version}, cannot be {@code null}
     * @return     {@code true} if this {@code Version} is newer than the {@code Version} object
     * @see        #isNewerThan(int, int, int, ReleaseType)
     * @author     Arsngrobg
     * @since      0.0.0-pre-alpha
     */
    public boolean isNewerThan(Version ver) {
        // TODO: Null reference validation
        return isNewerThan(ver.getMajor(), ver.getMinor(), ver.getPatch(), ver.getRelease());
    }

    /**
     * <p>Checks if whether this {@code Version} is newer than the unpacked {@code Version} object provided as arguments.</p>
     *
     * @param major   the major component of the unpacked {@code Version}
     * @param minor   the minor component of the unpacked {@code Version}
     * @param patch   the patch component of the unpacked {@code Version}
     * @param release the release type of the unpacked {@code Version}
     * @return        {@code true} if this {@code Version} is newer than the unpacked {@code Version}; {@code false} if otherwise
     * @see           #isNewerThan(Version)
     * @author        Arsngrobg
     * @since         0.0.0-pre-alpha
     */
    public boolean isNewerThan(int major, int minor, int patch, ReleaseType release) {
        if (this.major > major) return true;
        if (this.major < major) return false;

        if (this.minor > minor) return true;
        if (this.minor < minor) return false;

        if (this.patch > patch) return true;
        if (this.patch < patch) return false;

        // TODO: Null reference validation
        return this.release.ordinal() > release.ordinal();
    }

    /**
     * <p>Gets the major component of this {@code Version} object.</p>
     *
     * @return the major component
     * @author Arsngrobg
     * @since  0.0.0-pre-alpha
     */
    public int getMajor() {
        return major;
    }

    /**
     * <p>Gets the minor component of this {@code Version} object.</p>
     *
     * @return the minor component
     * @author Arsngrobg
     * @since  0.0.0-pre-alpha
     */
    public int getMinor() {
        return minor;
    }

    /**
     * <p>Gets the patch component of this {@code Version} object.</p>
     *
     * @return the patch component
     * @author Arsngrobg
     * @since  0.0.0-pre-alpha
     */
    public int getPatch() {
        return patch;
    }

    /**
     * <p>Gets the release type of this {@code Version} object.</p>
     *
     * @return the release type
     * @author Arsngrobg
     * @since  0.0.0-pre-alpha
     */
    public ReleaseType getRelease() {
        return release;
    }

    @Override
    public int hashCode() {
        return HashFunction.hash(major, minor, patch, release);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Version asVer)) return false;
        return (
            (major   == asVer.getMajor()  ) &&
            (minor   == asVer.getMinor()  ) &&
            (patch   == asVer.getPatch()  ) &&
            (release == asVer.getRelease())
        );
    }

    @Override
    public String toString() {
        return switch (release) {
            case STABLE -> String.format("%d.%d.%d", major, minor, patch);
            default     -> String.format("%d.%d.%d-%s", major, minor, patch, release);
        };
    }
}
