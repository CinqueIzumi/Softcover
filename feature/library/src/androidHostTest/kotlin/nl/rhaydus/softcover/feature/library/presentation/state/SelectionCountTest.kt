package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.statistic.StatNumberSuffix
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SelectionCountTest {
    @Nested
    inner class SelectionCountOf {
        @Test
        fun `carries the selected count as its value`() {
            // ----- Arrange -----
            val count = 7

            // ----- Act -----
            val result = selectionCountOf(count = count)

            // ----- Assert -----
            result.value shouldBe 7.0
        }

        @Test
        fun `labels the number with a fixed selected suffix`() {
            // ----- Arrange -----
            val count = 1

            // ----- Act -----
            val result = selectionCountOf(count = count)

            // ----- Assert -----
            result.suffix shouldBe StatNumberSuffix.Fixed(" selected")
        }

        @Test
        fun `zero selected still produces a zero value`() {
            // ----- Arrange -----
            val count = 0

            // ----- Act -----
            val result = selectionCountOf(count = count)

            // ----- Assert -----
            result.value shouldBe 0.0
        }
    }
}
