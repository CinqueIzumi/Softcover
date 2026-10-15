package nl.rhaydus.softcover.core.domain.util

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.util.TimeZone
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Instant

class UtcJournalTimeTest {
    private lateinit var originalZone: TimeZone

    @BeforeEach
    fun setUp() {
        originalZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Amsterdam"))
    }

    @AfterEach
    fun tearDown() {
        TimeZone.setDefault(originalZone)
    }

    @Nested
    inner class ToUtcJournalTimestamp {
        @Test
        fun `zero seconds and zero fraction are padded to a fixed width`() {
            // ----- Arrange -----
            val instant = Instant.parse("2026-07-01T10:05:00Z")

            // ----- Act -----
            val result = instant.toUtcJournalTimestamp()

            // ----- Assert -----
            result shouldBe "2026-07-01T10:05:00.000000"
        }

        @Test
        fun `a sub-second fraction is cut to six digits and left-padded`() {
            // ----- Arrange -----
            val instant = Instant.parse("2026-07-01T10:05:03.000123456Z")

            // ----- Act -----
            val result = instant.toUtcJournalTimestamp()

            // ----- Assert -----
            result shouldBe "2026-07-01T10:05:03.000123"
        }

        @Test
        fun `the device zone does not shift the stamped time`() {
            // ----- Arrange -----
            TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Kiritimati"))
            val instant = Instant.parse("2026-07-01T22:30:00Z")

            // ----- Act -----
            val result = instant.toUtcJournalTimestamp()

            // ----- Assert -----
            result shouldBe "2026-07-01T22:30:00.000000"
        }

        @Test
        fun `the output round-trips back to the same instant`() {
            // ----- Arrange -----
            val instant = Instant.parse("2026-12-31T23:59:59.123456Z")

            // ----- Act -----
            val timestamp = instant.toUtcJournalTimestamp()

            // ----- Assert -----
            Instant.parse("${timestamp}Z") shouldBe instant
        }

        @Test
        fun `the output reads back as the same instant in the local zone`() {
            // ----- Arrange -----
            val instant = Instant.parse("2026-07-01T22:30:00Z")

            // ----- Act -----
            val result = utcJournalTimeToLocalDateTime(instant.toUtcJournalTimestamp())

            // ----- Assert -----
            result.shouldNotBeNull().toString() shouldBe "2026-07-02T00:30"
        }
    }

    @Nested
    inner class UtcJournalTimeToLocalDateTime {
        @Test
        fun `a late UTC evening maps to the next local day in a zone ahead of UTC`() {
            // ----- Arrange -----
            val value = "2026-07-01T22:30:00"

            // ----- Act -----
            val result = utcJournalTimeToLocalDateTime(value)

            // ----- Assert -----
            result.shouldNotBeNull().toString() shouldBe "2026-07-02T00:30"
        }

        @Test
        fun `a zone behind UTC maps an early UTC morning to the previous day`() {
            // ----- Arrange -----
            TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Pago_Pago"))
            val value = "2026-07-01T05:00:00"

            // ----- Act -----
            val result = utcJournalTimeToLocalDateTime(value)

            // ----- Assert -----
            result.shouldNotBeNull().toString() shouldBe "2026-06-30T18:00"
        }

        @Test
        fun `a fractional-second timestamp as Hardcover sends it is parsed`() {
            // ----- Arrange -----
            val value = "2026-07-01T22:30:00.123456"

            // ----- Act -----
            val result = utcJournalTimeToLocalDateTime(value)

            // ----- Assert -----
            result.shouldNotBeNull().date.toString() shouldBe "2026-07-02"
        }

        @Test
        fun `text that is not a timestamp returns null`() {
            // ----- Arrange -----
            val value = "not-a-date"

            // ----- Act -----
            val result = utcJournalTimeToLocalDateTime(value)

            // ----- Assert -----
            result.shouldBeNull()
        }
    }

    @Nested
    inner class UtcJournalTimeToLocalDate {
        @Test
        fun `UTC T22 30 maps to the next local day in a zone ahead of UTC`() {
            // ----- Arrange -----
            TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Kiritimati"))
            val value = "2026-07-01T22:30:00.000000"

            // ----- Act -----
            val result = utcJournalTimeToLocalDate(value)

            // ----- Assert -----
            result.shouldNotBeNull().toString() shouldBe "2026-07-02"
        }

        @Test
        fun `a mid-day UTC time stays on the same local day`() {
            // ----- Arrange -----
            val value = "2026-07-01T10:00:00"

            // ----- Act -----
            val result = utcJournalTimeToLocalDate(value)

            // ----- Assert -----
            result.shouldNotBeNull().toString() shouldBe "2026-07-01"
        }

        @Test
        fun `a year-end UTC evening rolls into the next local year`() {
            // ----- Arrange -----
            val value = "2025-12-31T23:30:00"

            // ----- Act -----
            val result = utcJournalTimeToLocalDate(value)

            // ----- Assert -----
            result.shouldNotBeNull().year shouldBe 2026
        }

        @Test
        fun `text that is not a timestamp returns null`() {
            // ----- Arrange -----
            val value = ""

            // ----- Act -----
            val result = utcJournalTimeToLocalDate(value)

            // ----- Assert -----
            result.shouldBeNull()
        }
    }
}
