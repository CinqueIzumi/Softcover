package nl.rhaydus.softcover.feature.settings.presentation.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.DateStyle
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DateStyleRowsMapperTest {
    @Nested
    inner class Rows {
        @Test
        fun `returns one row per DateStyle entry in entries order`() {
            // ----- Act -----
            val result = dateStyleRowsFor(
                examples = emptyMap(),
                selected = DateStyle.DAY_MONTH_YEAR,
            )

            // ----- Assert -----
            result.map { it.key } shouldBe DateStyle.entries
        }
    }

    @Nested
    inner class Selection {
        @Test
        fun `marks exactly one row selected, the one whose key matches selected`() {
            DateStyle.entries.forEach { selectedStyle ->
                // ----- Act -----
                val result = dateStyleRowsFor(
                    examples = emptyMap(),
                    selected = selectedStyle,
                )

                // ----- Assert -----
                result.count { it.selected } shouldBe 1
                result.first { it.selected }.key shouldBe selectedStyle
            }
        }
    }

    @Nested
    inner class Example {
        @Test
        fun `sets example to null for a style with no entry in examples`() {
            // ----- Act -----
            val result = dateStyleRowsFor(
                examples = emptyMap(),
                selected = DateStyle.DAY_MONTH_YEAR,
            )

            // ----- Assert -----
            result.first { it.key == DateStyle.DAY_MONTH_YEAR }.example shouldBe null
        }

        @Test
        fun `carries the matching examples entry through for a style that has one`() {
            // ----- Arrange -----
            val examples = mapOf(DateStyle.YEAR_MONTH_DAY to "2024/01/31")

            // ----- Act -----
            val result = dateStyleRowsFor(
                examples = examples,
                selected = DateStyle.YEAR_MONTH_DAY,
            )

            // ----- Assert -----
            result.first { it.key == DateStyle.YEAR_MONTH_DAY }.example shouldBe "2024/01/31"
        }

        @Test
        fun `leaves styles absent from a partial examples map at null`() {
            // ----- Arrange -----
            val examples = mapOf(DateStyle.YEAR_MONTH_DAY to "2024/01/31")

            // ----- Act -----
            val result = dateStyleRowsFor(
                examples = examples,
                selected = DateStyle.YEAR_MONTH_DAY,
            )

            // ----- Assert -----
            result.first { it.key == DateStyle.DAY_MONTH_YEAR }.example shouldBe null
            result.first { it.key == DateStyle.MONTH_DAY_YEAR }.example shouldBe null
        }
    }
}
