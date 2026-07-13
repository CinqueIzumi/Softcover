package nl.rhaydus.softcover.core.designsystem.presentation.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.ProgressUnit

class ProgressSheetTabMappingTest {
    @Nested
    inner class ToProgressUnit {
        @Test
        fun `PAGE tab maps to PAGE unit`() {
            // ----- Arrange -----
            val tab = ProgressSheetTab.PAGE

            // ----- Act -----
            val result = tab.toProgressUnit()

            // ----- Assert -----
            result shouldBe ProgressUnit.PAGE
        }

        @Test
        fun `TIME tab maps to TIME unit`() {
            // ----- Arrange -----
            val tab = ProgressSheetTab.TIME

            // ----- Act -----
            val result = tab.toProgressUnit()

            // ----- Assert -----
            result shouldBe ProgressUnit.TIME
        }

        @Test
        fun `PERCENTAGE tab maps to PERCENTAGE unit`() {
            // ----- Arrange -----
            val tab = ProgressSheetTab.PERCENTAGE

            // ----- Act -----
            val result = tab.toProgressUnit()

            // ----- Assert -----
            result shouldBe ProgressUnit.PERCENTAGE
        }
    }

    @Nested
    inner class ToProgressSheetTab {
        @Test
        fun `PAGE unit maps to PAGE tab`() {
            // ----- Arrange -----
            val unit = ProgressUnit.PAGE

            // ----- Act -----
            val result = unit.toProgressSheetTab()

            // ----- Assert -----
            result shouldBe ProgressSheetTab.PAGE
        }

        @Test
        fun `TIME unit maps to TIME tab`() {
            // ----- Arrange -----
            val unit = ProgressUnit.TIME

            // ----- Act -----
            val result = unit.toProgressSheetTab()

            // ----- Assert -----
            result shouldBe ProgressSheetTab.TIME
        }

        @Test
        fun `PERCENTAGE unit maps to PERCENTAGE tab`() {
            // ----- Arrange -----
            val unit = ProgressUnit.PERCENTAGE

            // ----- Act -----
            val result = unit.toProgressSheetTab()

            // ----- Assert -----
            result shouldBe ProgressSheetTab.PERCENTAGE
        }
    }

    @Nested
    inner class RoundTrip {
        @Test
        fun `toProgressUnit then toProgressSheetTab round-trips for PAGE`() {
            // ----- Arrange -----
            val tab = ProgressSheetTab.PAGE

            // ----- Act -----
            val result = tab.toProgressUnit().toProgressSheetTab()

            // ----- Assert -----
            result shouldBe tab
        }

        @Test
        fun `toProgressUnit then toProgressSheetTab round-trips for TIME`() {
            // ----- Arrange -----
            val tab = ProgressSheetTab.TIME

            // ----- Act -----
            val result = tab.toProgressUnit().toProgressSheetTab()

            // ----- Assert -----
            result shouldBe tab
        }

        @Test
        fun `toProgressUnit then toProgressSheetTab round-trips for PERCENTAGE`() {
            // ----- Arrange -----
            val tab = ProgressSheetTab.PERCENTAGE

            // ----- Act -----
            val result = tab.toProgressUnit().toProgressSheetTab()

            // ----- Assert -----
            result shouldBe tab
        }
    }
}
