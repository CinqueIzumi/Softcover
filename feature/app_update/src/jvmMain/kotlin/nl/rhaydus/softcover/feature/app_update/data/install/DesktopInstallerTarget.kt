package nl.rhaydus.softcover.feature.app_update.data.install

/**
 * The installer flavour for the host desktop OS, matched against the release assets the CI publishes
 * as `Softcover-vX.Y.Z-{dmg,msi,deb}.{ext}` (see `.github/workflows/release.yml`). [assetSuffix] is
 * the trailing part of the asset file name that uniquely identifies the file for this OS.
 */
internal enum class DesktopInstallerTarget(val assetSuffix: String) {
    MACOS("-dmg.dmg"),
    WINDOWS("-msi.msi"),
    LINUX("-deb.deb"),
    ;

    companion object {
        /** Resolves the target for the running OS, or `null` on an unrecognised platform. */
        fun current(osName: String = System.getProperty("os.name").orEmpty()): DesktopInstallerTarget? {
            val normalized = osName.lowercase()
            return when {
                normalized.contains("mac") -> MACOS
                normalized.contains("win") -> WINDOWS
                normalized.contains("nux") || normalized.contains("nix") -> LINUX
                else -> null
            }
        }
    }
}
