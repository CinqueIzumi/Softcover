package nl.rhaydus.softcover.core.designsystem.presentation.util

import io.kotest.matchers.shouldBe
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PickerDatesTest {
    @Nested
    inner class ToPickerMillis {
        @Test
        fun `known date converts to its start-of-day-UTC epoch millis`() {
            // ----- Arrange -----
            val date = LocalDate(2023, 6, 15)

            // ----- Act -----
            val result = date.toPickerMillis()

            // ----- Assert -----
            result shouldBe 1_686_787_200_000L
        }
    }

    @Nested
    inner class RoundTrip {
        @Test
        fun `toPickerLocalDate decodes a value produced by toPickerMillis back to the same date`() {
            // ----- Arrange -----
            val date = LocalDate(2023, 6, 15)
            val millis = date.toPickerMillis()

            // ----- Act -----
            val result = millis.toPickerLocalDate()

            // ----- Assert -----
            result shouldBe date
        }

        @Test
        fun `round trip is stable for the first day of a month`() {
            // ----- Arrange -----
            val date = LocalDate(2024, 3, 1)
            val millis = date.toPickerMillis()

            // ----- Act -----
            val result = millis.toPickerLocalDate()

            // ----- Assert -----
            result shouldBe date
        }

        @Test
        fun `round trip is stable for a leap day`() {
            // ----- Arrange -----
            val date = LocalDate(2024, 2, 29)
            val millis = date.toPickerMillis()

            // ----- Act -----
            val result = millis.toPickerLocalDate()

            // ----- Assert -----
            result shouldBe date
        }
    }

    @Nested
    inner class RegressionGuard {
        @Test
        fun `toPickerLocalDate decodes start-of-day-UTC millis to the fixed UTC date`() {
            // ----- Arrange -----
            val expected = LocalDate(2023, 6, 15)
            val millis = expected.toPickerMillis()

            // ----- Act -----
            val result = millis.toPickerLocalDate()

            // ----- Assert -----
            result shouldBe expected
        }

        @Test
        fun `decoding through a west-of-UTC zone would shift the day, but toPickerLocalDate does not`() {
            // ----- Arrange -----
            val expected = LocalDate(2023, 6, 15)
            val millis = expected.toPickerMillis()
            val newYork = TimeZone.of("America/New_York")

            // ----- Act -----
            val naiveLocalDecode = Instant.fromEpochMilliseconds(millis).toLocalDateTime(newYork).date
            val pickerDecode = millis.toPickerLocalDate()

            // ----- Assert -----
            naiveLocalDecode shouldBe LocalDate(2023, 6, 14)
            pickerDecode shouldBe expected
        }

        @Test
        fun `decoding through an east-of-UTC zone stays on the same day and toPickerLocalDate agrees`() {
            // ----- Arrange -----
            val expected = LocalDate(2023, 6, 15)
            val millis = expected.toPickerMillis()
            val kiritimati = TimeZone.of("Pacific/Kiritimati")

            // ----- Act -----
            val naiveLocalDecode = Instant.fromEpochMilliseconds(millis).toLocalDateTime(kiritimati).date
            val pickerDecode = millis.toPickerLocalDate()

            // ----- Assert -----
            naiveLocalDecode shouldBe expected
            pickerDecode shouldBe expected
        }
    }
}
