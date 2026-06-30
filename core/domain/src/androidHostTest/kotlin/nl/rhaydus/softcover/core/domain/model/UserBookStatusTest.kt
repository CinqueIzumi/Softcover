package nl.rhaydus.softcover.core.domain.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserBookStatusTest {
    @Nested
    inner class IsAlwaysVisibleInLibrary {
        @Test
        fun `CURRENTLY_READING is always visible in library`() {
            // ----- Arrange -----
            val status = UserBookStatus.CURRENTLY_READING

            // ----- Act -----
            val result = status.isAlwaysVisibleInLibrary

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `WANT_TO_READ is not always visible in library`() {
            // ----- Arrange -----
            val status = UserBookStatus.WANT_TO_READ

            // ----- Act -----
            val result = status.isAlwaysVisibleInLibrary

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `READ is not always visible in library`() {
            // ----- Arrange -----
            val status = UserBookStatus.READ

            // ----- Act -----
            val result = status.isAlwaysVisibleInLibrary

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `DID_NOT_FINISH is not always visible in library`() {
            // ----- Arrange -----
            val status = UserBookStatus.DID_NOT_FINISH

            // ----- Act -----
            val result = status.isAlwaysVisibleInLibrary

            // ----- Assert -----
            result shouldBe false
        }
    }

    @Nested
    inner class TogglableInLibrary {
        @Test
        fun `togglableInLibrary excludes CURRENTLY_READING`() {
            // ----- Arrange & Act -----
            val result = UserBookStatus.togglableInLibrary

            // ----- Assert -----
            result.contains(UserBookStatus.CURRENTLY_READING) shouldBe false
        }

        @Test
        fun `togglableInLibrary includes WANT_TO_READ`() {
            // ----- Arrange & Act -----
            val result = UserBookStatus.togglableInLibrary

            // ----- Assert -----
            result.contains(UserBookStatus.WANT_TO_READ) shouldBe true
        }

        @Test
        fun `togglableInLibrary includes READ`() {
            // ----- Arrange & Act -----
            val result = UserBookStatus.togglableInLibrary

            // ----- Assert -----
            result.contains(UserBookStatus.READ) shouldBe true
        }

        @Test
        fun `togglableInLibrary includes DID_NOT_FINISH`() {
            // ----- Arrange & Act -----
            val result = UserBookStatus.togglableInLibrary

            // ----- Assert -----
            result.contains(UserBookStatus.DID_NOT_FINISH) shouldBe true
        }

        @Test
        fun `togglableInLibrary contains exactly three entries`() {
            // ----- Arrange & Act -----
            val result = UserBookStatus.togglableInLibrary

            // ----- Assert -----
            result.size shouldBe 3
        }
    }

    @Nested
    inner class ActiveLibraryCodes {
        @Test
        fun `always includes CURRENTLY_READING code even when enabledCodes is empty`() {
            // ----- Arrange -----
            val enabledCodes = emptySet<Int>()

            // ----- Act -----
            val result = UserBookStatus.activeLibraryCodes(enabledCodes = enabledCodes)

            // ----- Assert -----
            result.contains(UserBookStatus.CURRENTLY_READING.code) shouldBe true
        }

        @Test
        fun `includes CURRENTLY_READING code 2 when passed codes do not contain it`() {
            // ----- Arrange -----
            val enabledCodes = setOf(1, 3, 5)

            // ----- Act -----
            val result = UserBookStatus.activeLibraryCodes(enabledCodes = enabledCodes)

            // ----- Assert -----
            result.contains(2) shouldBe true
        }

        @Test
        fun `includes all codes passed in the enabled set`() {
            // ----- Arrange -----
            val enabledCodes = setOf(1, 3, 5)

            // ----- Act -----
            val result = UserBookStatus.activeLibraryCodes(enabledCodes = enabledCodes)

            // ----- Assert -----
            result.containsAll(enabledCodes) shouldBe true
        }

        @Test
        fun `returns just CURRENTLY_READING code when enabledCodes is empty`() {
            // ----- Arrange -----
            val enabledCodes = emptySet<Int>()

            // ----- Act -----
            val result = UserBookStatus.activeLibraryCodes(enabledCodes = enabledCodes)

            // ----- Assert -----
            result shouldBe setOf(UserBookStatus.CURRENTLY_READING.code)
        }

        @Test
        fun `does not duplicate CURRENTLY_READING when it is already in enabledCodes`() {
            // ----- Arrange -----
            val enabledCodes = setOf(UserBookStatus.CURRENTLY_READING.code, 1, 3)

            // ----- Act -----
            val result = UserBookStatus.activeLibraryCodes(enabledCodes = enabledCodes)

            // ----- Assert -----
            result.count { it == UserBookStatus.CURRENTLY_READING.code } shouldBe 1
        }
    }
}
