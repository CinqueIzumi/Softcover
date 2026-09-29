package nl.rhaydus.softcover.feature.settings.presentation.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.UiScale
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UiScaleRowsMapperTest {
    @Nested
    inner class Rows {
        @Test
        fun `returns one row per UiScale entry in entries order`() {
            // ----- Act -----
            val result = uiScaleRowsFor(selected = UiScale.DEFAULT)

            // ----- Assert -----
            result.map { it.key } shouldBe UiScale.entries
        }
    }

    @Nested
    inner class Selection {
        @Test
        fun `marks exactly one row selected, the one whose key matches selected`() {
            UiScale.entries.forEach { selectedScale ->
                // ----- Act -----
                val result = uiScaleRowsFor(selected = selectedScale)

                // ----- Assert -----
                result.count { it.selected } shouldBe 1
                result.first { it.selected }.key shouldBe selectedScale
            }
        }
    }

    @Nested
    inner class Example {
        @Test
        fun `sets example to null for every row`() {
            // ----- Act -----
            val result = uiScaleRowsFor(selected = UiScale.PERCENT_150)

            // ----- Assert -----
            result.forEach { it.example shouldBe null }
        }
    }
}
