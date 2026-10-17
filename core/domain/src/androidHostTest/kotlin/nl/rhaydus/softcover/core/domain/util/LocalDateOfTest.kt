package nl.rhaydus.softcover.core.domain.util

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import java.util.TimeZone
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LocalDateOfTest {
    private lateinit var originalZone: TimeZone

    @BeforeEach
    fun setUp() {
        originalZone = TimeZone.getDefault()
    }

    @AfterEach
    fun tearDown() {
        TimeZone.setDefault(originalZone)
    }

    @Nested
    inner class LocalDate {
        @Test
        fun `a zone ahead of UTC rolls a late UTC evening into the next day`() {
            // ----- Arrange -----
            TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Kiritimati"))

            // ----- Act -----
            val result = localDateOf(actionAt = "2026-05-04T12:34:56Z")

            // ----- Assert -----
            result shouldBe "2026-05-05"
        }

        @Test
        fun `a zone behind UTC rolls an early UTC morning into the previous day`() {
            // ----- Arrange -----
            TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Pago_Pago"))

            // ----- Act -----
            val result = localDateOf(actionAt = "2026-05-04T03:00:00Z")

            // ----- Assert -----
            result shouldBe "2026-05-03"
        }

        @Test
        fun `UTC keeps the date of the timestamp`() {
            // ----- Arrange -----
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

            // ----- Act -----
            val result = localDateOf(actionAt = "2026-05-04T23:59:59Z")

            // ----- Assert -----
            result shouldBe "2026-05-04"
        }
    }

    @Nested
    inner class LocalDateTime {
        @Test
        fun `maps an instant to the wall-clock date and time of the default zone`() {
            // ----- Arrange -----
            TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Kiritimati"))

            // ----- Act -----
            val result = localDateTimeOf(actionAt = "2026-05-04T12:34:56Z")

            // ----- Assert -----
            result.toString() shouldBe "2026-05-05T02:34:56"
        }

        @Test
        fun `throws IllegalArgumentException for a malformed timestamp`() {
            // ----- Arrange -----
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

            // ----- Act -----
            val act = { localDateTimeOf(actionAt = "not-a-timestamp") }

            // ----- Assert -----
            shouldThrow<IllegalArgumentException>(act)
        }
    }
}
