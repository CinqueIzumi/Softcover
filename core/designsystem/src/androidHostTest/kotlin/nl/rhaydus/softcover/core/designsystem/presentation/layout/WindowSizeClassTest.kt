package nl.rhaydus.softcover.core.designsystem.presentation.layout

import androidx.compose.ui.unit.dp
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WindowSizeClassTest {
    @Nested
    inner class WidthClassFor {
        @Test
        fun `returns COMPACT for 0dp`() {
            // ----- Arrange -----
            // ----- Act -----
            val result = WindowSizeClass.widthClassFor(0.dp)

            // ----- Assert -----
            result shouldBe WindowWidthClass.COMPACT
        }

        @Test
        fun `returns COMPACT for 599dp — one below COMPACT threshold`() {
            // ----- Arrange -----
            // ----- Act -----
            val result = WindowSizeClass.widthClassFor(599.dp)

            // ----- Assert -----
            result shouldBe WindowWidthClass.COMPACT
        }

        @Test
        fun `returns MEDIUM for 600dp — at COMPACT threshold`() {
            // ----- Arrange -----
            // ----- Act -----
            val result = WindowSizeClass.widthClassFor(600.dp)

            // ----- Assert -----
            result shouldBe WindowWidthClass.MEDIUM
        }

        @Test
        fun `returns MEDIUM for 839dp — one below MEDIUM threshold`() {
            // ----- Arrange -----
            // ----- Act -----
            val result = WindowSizeClass.widthClassFor(839.dp)

            // ----- Assert -----
            result shouldBe WindowWidthClass.MEDIUM
        }

        @Test
        fun `returns EXPANDED for 840dp — at MEDIUM threshold`() {
            // ----- Arrange -----
            // ----- Act -----
            val result = WindowSizeClass.widthClassFor(840.dp)

            // ----- Assert -----
            result shouldBe WindowWidthClass.EXPANDED
        }

        @Test
        fun `returns EXPANDED for 841dp — above MEDIUM threshold`() {
            // ----- Arrange -----
            // ----- Act -----
            val result = WindowSizeClass.widthClassFor(841.dp)

            // ----- Assert -----
            result shouldBe WindowWidthClass.EXPANDED
        }

        @Test
        fun `returns EXPANDED for large value 1600dp`() {
            // ----- Arrange -----
            // ----- Act -----
            val result = WindowSizeClass.widthClassFor(1600.dp)

            // ----- Assert -----
            result shouldBe WindowWidthClass.EXPANDED
        }
    }
}
