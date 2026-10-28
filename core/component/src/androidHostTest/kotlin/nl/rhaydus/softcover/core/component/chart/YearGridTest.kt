package nl.rhaydus.softcover.core.component.chart

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class YearGridTest {
    // Cells are 10 wide with 2 gaps: pitch 12, three columns across a 34-wide grid.
    private val cellPx = 10f
    private val pitchPx = 12f
    private val gridWidthPx = 34f

    private val read = YearGridCell.Read(
        date = LocalDate(
            year = 2025,
            month = 1,
            day = 1,
        ),
        step = YearGridStep.One,
        contentDescription = "Jan 1",
    )

    private val model = YearGridUiModel(
        year = 2025,
        months = persistentListOf(
            YearGridMonthUiModel(
                label = "Jan",
                days = persistentListOf(read, YearGridCell.Empty, read),
            ),
        ),
        fewerLabel = "Fewer",
        moreLabel = "More",
    )

    private fun hit(
        x: Float,
        y: Float,
        isRtl: Boolean = false,
    ): IntOffset? = readCellAt(
        position = Offset(
            x,
            y,
        ),
        model = model,
        pitchPx = pitchPx,
        gridWidthPx = gridWidthPx,
        isRtl = isRtl,
    )

    @Nested
    inner class ReadCellAt {
        @Test
        fun `a point on a read cell resolves to its column and row`() {
            // ----- Arrange -----
            val x = 30f

            // ----- Act -----
            val cell = hit(
                x,
                5f,
            )

            // ----- Assert -----
            cell shouldBe IntOffset(
                2,
                0,
            )
        }

        @Test
        fun `the gap after a read cell resolves to that cell`() {
            // ----- Arrange -----
            val x = 11f

            // ----- Act -----
            val cell = hit(
                x,
                11f,
            )

            // ----- Assert -----
            cell shouldBe IntOffset(
                0,
                0,
            )
        }

        @Test
        fun `a point on an empty cell resolves to nothing`() {
            // ----- Arrange -----
            val x = 18f

            // ----- Act -----
            val cell = hit(
                x,
                5f,
            )

            // ----- Assert -----
            cell.shouldBeNull()
        }

        @Test
        fun `a point past the last day resolves to nothing`() {
            // ----- Arrange -----
            val x = 40f

            // ----- Act -----
            val cell = hit(
                x,
                5f,
            )

            // ----- Assert -----
            cell.shouldBeNull()
        }

        @Test
        fun `a point below the last month resolves to nothing`() {
            // ----- Arrange -----
            val y = 15f

            // ----- Act -----
            val cell = hit(
                5f,
                y,
            )

            // ----- Assert -----
            cell.shouldBeNull()
        }

        @Test
        fun `a point left of the grid resolves to nothing`() {
            // ----- Arrange -----
            val x = -3f

            // ----- Act -----
            val cell = hit(
                x,
                5f,
            )

            // ----- Assert -----
            cell.shouldBeNull()
        }

        @Test
        fun `a point above the grid resolves to nothing`() {
            // ----- Arrange -----
            val y = -3f

            // ----- Act -----
            val cell = hit(
                5f,
                y,
            )

            // ----- Assert -----
            cell.shouldBeNull()
        }

        @Test
        fun `the read cell that is first in reading order sits at the right edge in right to left`() {
            // ----- Arrange -----
            val x = 30f

            // ----- Act -----
            val cell = hit(
                x,
                5f,
                isRtl = true,
            )

            // ----- Assert -----
            cell shouldBe IntOffset(
                0,
                0,
            )
        }

        @Test
        fun `a point at the left edge in right to left resolves to the last column`() {
            // ----- Arrange -----
            val x = 3f

            // ----- Act -----
            val cell = hit(
                x,
                5f,
                isRtl = true,
            )

            // ----- Assert -----
            cell shouldBe IntOffset(
                2,
                0,
            )
        }

        @Test
        fun `a point beyond the grid on the right in right to left resolves to nothing`() {
            // ----- Arrange -----
            val x = 40f

            // ----- Act -----
            val cell = hit(
                x,
                5f,
                isRtl = true,
            )

            // ----- Assert -----
            cell.shouldBeNull()
        }

        @Test
        fun `the resolved cell carries the read day's date`() {
            // ----- Arrange -----
            val x = 3f

            // ----- Act -----
            val cell = hit(
                x,
                5f,
            ).shouldNotBeNull()
            val day = model.months[cell.y].days[cell.x] as YearGridCell.Read

            // ----- Assert -----
            day.date shouldBe read.date
        }
    }

    @Nested
    inner class CellLeft {
        @Test
        fun `columns advance by one pitch from the left`() {
            // ----- Arrange -----
            val column = 2

            // ----- Act -----
            val left = cellLeft(
                column = column,
                pitchPx = pitchPx,
                cellPx = cellPx,
                gridWidthPx = gridWidthPx,
                isRtl = false,
            )

            // ----- Assert -----
            left shouldBe (24f plusOrMinus 0.001f)
        }

        @Test
        fun `the first column sits flush with the right edge in right to left`() {
            // ----- Arrange -----
            val column = 0

            // ----- Act -----
            val left = cellLeft(
                column = column,
                pitchPx = pitchPx,
                cellPx = cellPx,
                gridWidthPx = gridWidthPx,
                isRtl = true,
            )

            // ----- Assert -----
            left shouldBe (24f plusOrMinus 0.001f)
        }

        @Test
        fun `the last column sits flush with the left edge in right to left`() {
            // ----- Arrange -----
            val column = 2

            // ----- Act -----
            val left = cellLeft(
                column = column,
                pitchPx = pitchPx,
                cellPx = cellPx,
                gridWidthPx = gridWidthPx,
                isRtl = true,
            )

            // ----- Assert -----
            left shouldBe (0f plusOrMinus 0.001f)
        }
    }

    @Nested
    inner class Previews {
        @Test
        fun `includes a loading fixture`() {
            // ----- Arrange -----
            val previews = YearGridUiModel.previews.values

            // ----- Act -----
            val loading = previews.filter { it.isLoading }

            // ----- Assert -----
            loading.shouldNotBeEmpty()
        }

        @Test
        fun `includes a fixture with read days`() {
            // ----- Arrange -----
            val previews = YearGridUiModel.previews.values

            // ----- Act -----
            val populated = previews.filter { fixture ->
                fixture.months.any { month -> month.days.any { it is YearGridCell.Read } }
            }

            // ----- Assert -----
            populated.shouldNotBeEmpty()
        }
    }
}
