package nl.rhaydus.softcover.core.domain.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookStatusTest {
    @Nested
    inner class GetFromCode {
        @Test
        fun `maps every status code back to its own status`() {
            // ----- Arrange -----
            val statuses = BookStatus.entries

            // ----- Act -----
            val result = statuses.map { BookStatus.getFromCode(code = it.code) }

            // ----- Assert -----
            result shouldBe statuses
        }

        @Test
        fun `code 4 maps to Paused`() {
            // ----- Arrange -----
            val code = 4

            // ----- Act -----
            val result = BookStatus.getFromCode(code = code)

            // ----- Assert -----
            result shouldBe BookStatus.Paused
        }

        @Test
        fun `an unknown code maps to None`() {
            // ----- Arrange -----
            val code = 99

            // ----- Act -----
            val result = BookStatus.getFromCode(code = code)

            // ----- Assert -----
            result shouldBe BookStatus.None
        }
    }
}
