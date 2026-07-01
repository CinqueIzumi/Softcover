package nl.rhaydus.softcover.feature.app_update.data.version

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class VersionComparatorTest {
    @Nested
    inner class IsNewer {
        @Test
        fun `returns true when candidate is strictly newer than current`() {
            // ----- Arrange -----
            val candidate = "3.1.0"
            val current = "3.0.2"

            // ----- Act -----
            val result = VersionComparator.isNewer(
                candidate = candidate,
                current = current,
            )

            // ----- Assert -----
            assertTrue(result)
        }

        @Test
        fun `returns false when candidate equals current`() {
            // ----- Arrange -----
            val candidate = "3.0.2"
            val current = "3.0.2"

            // ----- Act -----
            val result = VersionComparator.isNewer(
                candidate = candidate,
                current = current,
            )

            // ----- Assert -----
            assertFalse(result)
        }

        @Test
        fun `returns false when candidate is older than current`() {
            // ----- Arrange -----
            val candidate = "2.9.9"
            val current = "3.0.0"

            // ----- Act -----
            val result = VersionComparator.isNewer(
                candidate = candidate,
                current = current,
            )

            // ----- Assert -----
            assertFalse(result)
        }

        @Test
        fun `tolerates leading lowercase v on candidate`() {
            // ----- Arrange -----
            val candidate = "v4.0.0"
            val current = "3.9.9"

            // ----- Act -----
            val result = VersionComparator.isNewer(
                candidate = candidate,
                current = current,
            )

            // ----- Assert -----
            assertTrue(result)
        }

        @Test
        fun `tolerates leading uppercase V on current`() {
            // ----- Arrange -----
            val candidate = "4.0.0"
            val current = "V3.9.9"

            // ----- Act -----
            val result = VersionComparator.isNewer(
                candidate = candidate,
                current = current,
            )

            // ----- Assert -----
            assertTrue(result)
        }

        @Test
        fun `returns true when candidate has fewer segments but higher minor`() {
            // ----- Arrange -----
            val candidate = "3.1"
            val current = "3.0.5"

            // ----- Act -----
            val result = VersionComparator.isNewer(
                candidate = candidate,
                current = current,
            )

            // ----- Assert -----
            assertTrue(result)
        }

        @Test
        fun `returns false when candidate has fewer segments but is numerically equal`() {
            // ----- Arrange -----
            val candidate = "3.0"
            val current = "3.0.0"

            // ----- Act -----
            val result = VersionComparator.isNewer(
                candidate = candidate,
                current = current,
            )

            // ----- Assert -----
            assertFalse(result)
        }

        @Test
        fun `ignores pre-release suffix and returns true when base version is newer`() {
            // ----- Arrange -----
            val candidate = "3.0.2-rc1"
            val current = "3.0.1"

            // ----- Act -----
            val result = VersionComparator.isNewer(
                candidate = candidate,
                current = current,
            )

            // ----- Assert -----
            assertTrue(result)
        }

        @Test
        fun `ignores pre-release suffix and returns false when base version is equal`() {
            // ----- Arrange -----
            val candidate = "3.0.1-rc1"
            val current = "3.0.1"

            // ----- Act -----
            val result = VersionComparator.isNewer(
                candidate = candidate,
                current = current,
            )

            // ----- Assert -----
            assertFalse(result)
        }

        @Test
        fun `ignores build metadata and returns true when base version is newer`() {
            // ----- Arrange -----
            val candidate = "3.1.0+build.100"
            val current = "3.0.9"

            // ----- Act -----
            val result = VersionComparator.isNewer(
                candidate = candidate,
                current = current,
            )

            // ----- Assert -----
            assertTrue(result)
        }

        @Test
        fun `treats non-numeric segment as zero`() {
            // ----- Arrange -----
            val candidate = "3.alpha.0"
            val current = "3.0.0"

            // ----- Act -----
            val result = VersionComparator.isNewer(
                candidate = candidate,
                current = current,
            )

            // ----- Assert -----
            assertFalse(result)
        }
    }
}
