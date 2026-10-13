package nl.rhaydus.softcover.core.domain.model

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
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
        fun `every other status is not always visible in library`() {
            // ----- Arrange -----
            val others = UserBookStatus.entries - UserBookStatus.CURRENTLY_READING

            // ----- Act -----
            val result = others.map { it.isAlwaysVisibleInLibrary }

            // ----- Assert -----
            result.none { it } shouldBe true
        }
    }

    @Nested
    inner class TogglableInLibrary {
        @Test
        fun `togglableInLibrary is every status except CURRENTLY_READING`() {
            // ----- Arrange -----
            val expected = UserBookStatus.entries - UserBookStatus.CURRENTLY_READING

            // ----- Act -----
            val result = UserBookStatus.togglableInLibrary

            // ----- Assert -----
            result shouldContainExactlyInAnyOrder expected
        }
    }

    @Nested
    inner class ActiveLibraryCodes {
        @Test
        fun `returns just the CURRENTLY_READING code when enabledCodes is empty`() {
            // ----- Arrange -----
            val enabledCodes = emptySet<Int>()

            // ----- Act -----
            val result = UserBookStatus.activeLibraryCodes(enabledCodes = enabledCodes)

            // ----- Assert -----
            result shouldBe setOf(UserBookStatus.CURRENTLY_READING.code)
        }

        @Test
        fun `adds the CURRENTLY_READING code to the enabled codes`() {
            // ----- Arrange -----
            val enabledCodes = setOf(1, 3, 5)

            // ----- Act -----
            val result = UserBookStatus.activeLibraryCodes(enabledCodes = enabledCodes)

            // ----- Assert -----
            result shouldBe enabledCodes + UserBookStatus.CURRENTLY_READING.code
        }
    }
}
