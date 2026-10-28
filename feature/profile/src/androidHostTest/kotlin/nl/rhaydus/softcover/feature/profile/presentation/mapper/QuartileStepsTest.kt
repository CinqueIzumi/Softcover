package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.chart.YearGridStep
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class QuartileStepsTest {
    @Nested
    inner class QuartileCutPointsOf {
        @Test
        fun `picks the nearest rank of eight values regardless of their order`() {
            // ----- Arrange -----
            val values = listOf(
                80,
                10,
                50,
                30,
                70,
                20,
                60,
                40,
            )

            // ----- Act -----
            val result = quartileCutPoints(values).shouldNotBeNull()

            // ----- Assert -----
            result shouldBe QuartileCutPoints(
                first = 20,
                second = 40,
                third = 60,
            )
        }

        @Test
        fun `has cut points from exactly four distinct values`() {
            // ----- Arrange -----
            val values = listOf(
                1,
                2,
                3,
                4,
            )

            // ----- Act -----
            val result = quartileCutPoints(values)

            // ----- Assert -----
            result shouldBe QuartileCutPoints(
                first = 1,
                second = 2,
                third = 3,
            )
        }

        @Test
        fun `has none with fewer than four values`() {
            // ----- Arrange -----
            val values = listOf(
                1,
                50,
                400,
            )

            // ----- Act -----
            val result = quartileCutPoints(values)

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `has none when every value is equal`() {
            // ----- Arrange -----
            val values = List(6) { 30 }

            // ----- Act -----
            val result = quartileCutPoints(values)

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `has none for no values`() {
            // ----- Arrange -----
            val values = emptyList<Int>()

            // ----- Act -----
            val result = quartileCutPoints(values)

            // ----- Assert -----
            result.shouldBeNull()
        }
    }

    @Nested
    inner class StepFor {
        private val cutPoints = QuartileCutPoints(
            first = 20,
            second = 40,
            third = 60,
        )

        @Test
        fun `is the darkest step without cut points`() {
            // ----- Arrange -----
            val pages = 1

            // ----- Act -----
            val result = stepFor(
                pages = pages,
                cutPoints = null,
            )

            // ----- Assert -----
            result shouldBe YearGridStep.Four
        }

        @Test
        fun `a value on a cut point stays in the lighter step`() {
            // ----- Arrange -----
            val onCutPoints = listOf(
                20,
                40,
                60,
            )

            // ----- Act -----
            val result = onCutPoints.map {
                stepFor(
                    pages = it,
                    cutPoints = cutPoints,
                )
            }

            // ----- Assert -----
            result shouldBe listOf(
                YearGridStep.One,
                YearGridStep.Two,
                YearGridStep.Three,
            )
        }

        @Test
        fun `a value just past a cut point moves to the next step`() {
            // ----- Arrange -----
            val pastCutPoints = listOf(
                21,
                41,
                61,
            )

            // ----- Act -----
            val result = pastCutPoints.map {
                stepFor(
                    pages = it,
                    cutPoints = cutPoints,
                )
            }

            // ----- Assert -----
            result shouldBe listOf(
                YearGridStep.Two,
                YearGridStep.Three,
                YearGridStep.Four,
            )
        }

        @Test
        fun `gives equal page counts the same step`() {
            // ----- Arrange -----
            val equalPages = listOf(
                30,
                30,
                30,
            )

            // ----- Act -----
            val result = equalPages.map {
                stepFor(
                    pages = it,
                    cutPoints = cutPoints,
                )
            }

            // ----- Assert -----
            result shouldBe listOf(
                YearGridStep.Two,
                YearGridStep.Two,
                YearGridStep.Two,
            )
        }
    }
}
