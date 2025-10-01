package dev.arsngrobg.smphook.core;

import java.util.Objects;
import java.util.Optional;

/**
 * <p>The {@code Version} class acts as the metadata for particular <b>SMPHook</b> client.</p>
 * <p><b>SMPHook</b> follows the SemVer versioning scheme: <a href='https://semver.org'>semver.org</a></p>
 * <p>This class exposes <i>two</i> main factories for obtaining a {@code Version} of <b>SMPHook</b>.
 *    <pre><code>
 *         var client = Version.getClientVersion(); // the current version running on this system
 *         var latest = Version.getLatestVersion(); // the latest version found on GitHub
 *    </code></pre>
 *    <i>The {@code Version::getLatestVersion} factory uses the GitHub API to retrieve the latest-available version of
 *       <b>SMPHook</b> on GitHub.
 *    </i>
 * </p>
 *
 * @author  Arsngrobg
 * @since   v0.0.0-pre_alpha
 * @version v1.1
 * @see     Version#getClientVersion
 * @see     Version#getLatestVersion
 */
@SuppressWarnings("ClassCanBeRecord")
public final class Version {
    // NOTICE: these are the client version numbers, please make sure these reflect the true version
    private static final int
        VERSION_MAJOR = 0,
        VERSION_MINOR = 0,
        VERSION_PATCH = 2;

    // NOTICE: this is the client version release type, please make sure this reflects the true release type
    private static final ReleaseType VERSION_RELEASE = ReleaseType.PRE_ALPHA;

    private static final Version CLIENT_VERSION = new Version(
            VERSION_MAJOR, VERSION_MINOR, VERSION_PATCH,
            VERSION_RELEASE
    );

    /**
     * <p>Returns the version of <b>SMPHook</b> that is currently running on the user's system / server.</p>
     *
     * @return  the client version of <b>SMPHook</b>
     * @since   v0.0.0-pre_alpha
     */
    public static Version getClientVersion() {
        return CLIENT_VERSION;
    }

    /**
     * <p>Returns the latest-available version of <b>SMPHook</b> that is currently available on GitHub.</p>
     * <p>Of course, this requires the user/server to be connected to the internet in-order for this factory to return
     *    the latest {@code Version}. Hence, this factory returns a {@code Version} wrapped in an {@link Optional} type.
     * </p>
     *
     * @return an {@link Optional} that may contain the latest {@code Version} or not (due to network failure)
     * @since  v0.0.0-pre_alpha
     */
    public static Optional<Version> getLatestVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * <p>The {@code ReleaseType} enum defines the in-order sequence of build stages of a {@code Version} of
     *    <b>SMPHook</b>.
     * </p>
     * <p>When obtaining the string representation of a {@code Version} object, the {@code ReleaseType} is appended to
     *    the end of the version string. For example:
     *    <pre><code>
     *        var version = getClientVersion(); // the current version is 1, 2, 3, ReleaseType.BETA
     *        System.out.println(version); // output: 1.2.3-beta
     *    </code></pre>
     *    However, if the current {@code Version} is a <b>STABLE</b> release, then the version number is returned
     *    plainly.
     * </p>
     *
     * @author  Arsngrobg
     * @since   v0.0.1-pre_alpha
     * @version v1.0
     */
    @SuppressWarnings("unused")
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
            return (this == RELEASE_CANDIDATE) ? "rc" : name().toLowerCase();
        }
    }

    private final int major, minor, patch;
    private final ReleaseType release;

    private Version(final int major, final int minor, final int patch, final ReleaseType release) {
        this.major   = major;
        this.minor   = minor;
        this.patch   = patch;
        this.release = release;
    }

    /**
     * <p>Compares the {@code Version} with this {@code Version}.</p>
     *
     * @param  version the {@code Version} to compare with this {@code Version}
     * @return         {@code true} if this {@code Version} is newer than the supplied {@code Version}; {@code false} if
     *                 otherwise
     * @author         Arsngrobg
     * @since          v0.0.1-pre_alpha
     * @see            Version#isNewerThan(int, int, int, ReleaseType)
     */
    public boolean isNewerThan(final Version version) {
        return isNewerThan(version.getMajor(), version.getMinor(), version.getPatch(), version.getRelease());
    }

    /**
     * <p>Compares the unpacked {@code Version} with this {@code Version}.</p>
     *
     * @param  major   the <b>major</b> component of the unpacked {@code Version}
     * @param  minor   the <b>minor</b> component of the unpacked {@code Version}
     * @param  patch   the <b>patch</b> component of the unpacked {@code Version}
     * @param  release the <b>release</b> component of the unpacked {@code Version}
     * @return         {@code true} if this {@code Version} is newer than the supplied, unpacked {@code Version};
     *                 {@code false} if otherwise
     * @author         Arsngrobg
     * @since          v0.0.1-pre_alpha
     * @see            Version#isNewerThan(Version)
     */
    public boolean isNewerThan(final int major, final int minor, final int patch, final ReleaseType release) {
        if (this.major > major) return true;
        if (this.major < major) return false;

        if (this.minor > minor) return true;
        if (this.minor < minor) return false;

        if (this.patch > patch) return true;
        if (this.patch < patch) return false;

        return this.release.ordinal() > release.ordinal();
    }

    /**
     * <p>This is the <b>major</b> component of this {@code Version}.</p>
     * <p>This is modified when incompatible API changes occur.</p>
     *
     * @return this {@code Version}'s <b>major</b> component
     * @since  v0.0.0-pre_alpha
     */
    public int getMajor() {
        return major;
    }

    /**
     * <p>This is the <b>minor</b> component of this {@code Version}.</p>
     * <p>This is modified when backward-compatible functionality changes occur.</p>
     *
     * @return this {@code Version}'s <b>minor</b> component
     * @since  v0.0.0-pre_alpha
     */
    public int getMinor() {
        return minor;
    }

    /**
     * <p>This is the <b>patch</b> component of this {@code Version}.</p>
     * <p>This is modified when backward-compatible bug fixes occur.</p>
     *
     * @return this {@code Version}'s <b>patch</b> component
     * @since  v0.0.0-pre_alpha
     */
    public int getPatch() {
        return patch;
    }

    /**
     * <p>This is the <b>release</b> metadata component of this {@code Version}.</p>
     *
     * @return this {@code Version}'s release component
     * @since  v0.0.0-pre_alpha
     */
    public ReleaseType getRelease() {
        return release;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch, release);
    }

    @Override
    public boolean equals(final Object obj) {
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
        if (release == ReleaseType.STABLE) {
            return String.format("%d.%d.%d", major, minor, patch);
        } else return String.format("%d.%d.%d-%s", major, minor, patch, release);
    }
}
