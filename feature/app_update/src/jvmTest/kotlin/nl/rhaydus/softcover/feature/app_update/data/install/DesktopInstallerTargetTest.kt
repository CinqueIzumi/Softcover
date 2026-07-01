package nl.rhaydus.softcover.feature.app_update.data.install

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DesktopInstallerTargetTest {
    @Nested
    inner class Current {
        @Test
        fun `returns MACOS for Mac OS X`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = DesktopInstallerTarget.current(osName = "Mac OS X")

            // ----- Assert -----
            assertEquals(
                DesktopInstallerTarget.MACOS,
                result,
            )
        }

        @Test
        fun `returns WINDOWS for Windows 11`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = DesktopInstallerTarget.current(osName = "Windows 11")

            // ----- Assert -----
            assertEquals(
                DesktopInstallerTarget.WINDOWS,
                result,
            )
        }

        @Test
        fun `returns LINUX for Linux`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = DesktopInstallerTarget.current(osName = "Linux")

            // ----- Assert -----
            assertEquals(
                DesktopInstallerTarget.LINUX,
                result,
            )
        }

        @Test
        fun `returns null for unrecognised platform`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = DesktopInstallerTarget.current(osName = "SunOS")

            // ----- Assert -----
            assertNull(result)
        }

        @Test
        fun `returns null for empty os name`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = DesktopInstallerTarget.current(osName = "")

            // ----- Assert -----
            assertNull(result)
        }
    }

    @Nested
    inner class AssetSuffix {
        @Test
        fun `MACOS has dmg asset suffix`() {
            // ----- Arrange -----

            // ----- Act -----
            val suffix = DesktopInstallerTarget.MACOS.assetSuffix

            // ----- Assert -----
            assertEquals(
                "-dmg.dmg",
                suffix,
            )
        }

        @Test
        fun `WINDOWS has msi asset suffix`() {
            // ----- Arrange -----

            // ----- Act -----
            val suffix = DesktopInstallerTarget.WINDOWS.assetSuffix

            // ----- Assert -----
            assertEquals(
                "-msi.msi",
                suffix,
            )
        }

        @Test
        fun `LINUX has deb asset suffix`() {
            // ----- Arrange -----

            // ----- Act -----
            val suffix = DesktopInstallerTarget.LINUX.assetSuffix

            // ----- Assert -----
            assertEquals(
                "-deb.deb",
                suffix,
            )
        }
    }
}
