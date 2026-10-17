package nl.rhaydus.softcover.core.component.chart

import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ColumnChartGeometryTest {
    // Three 40-wide columns separated by 10-wide gaps: [0, 40], [50, 90], [100, 140].
    private val rowWidth = 140f
    private val count = 3
    private val gap = 10f

    @Nested
    inner class ColumnIndexAt {
        @Test
        fun `an offset inside the first column resolves to it`() {
            // ----- Arrange -----
            val x = 20f

            // ----- Act -----
            val index = columnIndexAt(
                x,
                rowWidth,
                count,
                gap,
            )

            // ----- Assert -----
            index shouldBe 0
        }

        @Test
        fun `an offset inside a middle column resolves to it`() {
            // ----- Arrange -----
            val x = 70f

            // ----- Act -----
            val index = columnIndexAt(
                x,
                rowWidth,
                count,
                gap,
            )

            // ----- Assert -----
            index shouldBe 1
        }

        @Test
        fun `an offset inside the last column resolves to it`() {
            // ----- Arrange -----
            val x = 120f

            // ----- Act -----
            val index = columnIndexAt(
                x,
                rowWidth,
                count,
                gap,
            )

            // ----- Assert -----
            index shouldBe 2
        }

        @Test
        fun `an offset in a gap snaps to the nearer column on its left`() {
            // ----- Arrange -----
            val x = 42f

            // ----- Act -----
            val index = columnIndexAt(
                x,
                rowWidth,
                count,
                gap,
            )

            // ----- Assert -----
            index shouldBe 0
        }

        @Test
        fun `an offset in a gap snaps to the nearer column on its right`() {
            // ----- Arrange -----
            val x = 48f

            // ----- Act -----
            val index = columnIndexAt(
                x,
                rowWidth,
                count,
                gap,
            )

            // ----- Assert -----
            index shouldBe 1
        }

        @Test
        fun `an offset exactly at the midpoint of a gap goes to the column on its right`() {
            // ----- Arrange -----
            val midpoint = 45f

            // ----- Act -----
            val index = columnIndexAt(
                midpoint,
                rowWidth,
                count,
                gap,
            )

            // ----- Assert -----
            index shouldBe 1
        }

        @Test
        fun `an offset just before the midpoint of a gap goes to the column on its left`() {
            // ----- Arrange -----
            val justBeforeMidpoint = 44.9f

            // ----- Act -----
            val index = columnIndexAt(
                justBeforeMidpoint,
                rowWidth,
                count,
                gap,
            )

            // ----- Assert -----
            index shouldBe 0
        }

        @Test
        fun `a negative offset clamps to the first column`() {
            // ----- Arrange -----
            val x = -30f

            // ----- Act -----
            val index = columnIndexAt(
                x,
                rowWidth,
                count,
                gap,
            )

            // ----- Assert -----
            index shouldBe 0
        }

        @Test
        fun `an offset past the row width clamps to the last column`() {
            // ----- Arrange -----
            val x = rowWidth + 500f

            // ----- Act -----
            val index = columnIndexAt(
                x,
                rowWidth,
                count,
                gap,
            )

            // ----- Assert -----
            index shouldBe count - 1
        }

        @Test
        fun `a zero count returns minus one`() {
            // ----- Arrange -----
            val emptyCount = 0

            // ----- Act -----
            val index = columnIndexAt(
                10f,
                rowWidth,
                emptyCount,
                gap,
            )

            // ----- Assert -----
            index shouldBe -1
        }

        @Test
        fun `a negative count returns minus one`() {
            // ----- Arrange -----
            val negativeCount = -2

            // ----- Act -----
            val index = columnIndexAt(
                10f,
                rowWidth,
                negativeCount,
                gap,
            )

            // ----- Assert -----
            index shouldBe -1
        }

        @Test
        fun `a zero row width returns minus one`() {
            // ----- Arrange -----
            val emptyWidth = 0f

            // ----- Act -----
            val index = columnIndexAt(
                10f,
                emptyWidth,
                count,
                gap,
            )

            // ----- Assert -----
            index shouldBe -1
        }

        @Test
        fun `a single column resolves every offset to it`() {
            // ----- Arrange -----
            val offsets = listOf(-20f, 0f, 50f, 100f, 400f)

            // ----- Act -----
            val indices = offsets.map {
                columnIndexAt(
                    it,
                    100f,
                    1,
                    gap,
                )
            }

            // ----- Assert -----
            indices shouldBe listOf(0, 0, 0, 0, 0)
        }

        @Test
        fun `columns without a gap split the row evenly`() {
            // ----- Arrange -----
            val offsets = listOf(0f, 24.9f, 25f, 74.9f, 99.9f)

            // ----- Act -----
            val indices = offsets.map {
                columnIndexAt(
                    it,
                    100f,
                    4,
                    0f,
                )
            }

            // ----- Assert -----
            indices shouldBe listOf(0, 0, 1, 2, 3)
        }
    }

    @Nested
    inner class ColumnCenterX {
        @Test
        fun `each column centre sits in the middle of its column`() {
            // ----- Arrange -----
            val indices = 0 until count

            // ----- Act -----
            val centers = indices.map {
                columnCenterX(
                    it,
                    rowWidth,
                    count,
                    gap,
                )
            }

            // ----- Assert -----
            centers shouldBe listOf(20f, 70f, 120f)
        }

        @Test
        fun `a single column is centred in the row`() {
            // ----- Arrange -----
            val width = 100f

            // ----- Act -----
            val center = columnCenterX(
                0,
                width,
                1,
                gap,
            )

            // ----- Assert -----
            center shouldBe (50f plusOrMinus 0.001f)
        }

        @Test
        fun `columns without a gap are centred on equal slices`() {
            // ----- Arrange -----
            val indices = 0 until 4

            // ----- Act -----
            val centers = indices.map {
                columnCenterX(
                    it,
                    100f,
                    4,
                    0f,
                )
            }

            // ----- Assert -----
            centers shouldBe listOf(12.5f, 37.5f, 62.5f, 87.5f)
        }

        @Test
        fun `the index at each column centre is that column`() {
            // ----- Arrange -----
            val indices = 0 until count

            // ----- Act -----
            val resolved = indices.map {
                columnIndexAt(
                    columnCenterX(
                        it,
                        rowWidth,
                        count,
                        gap,
                    ),
                    rowWidth,
                    count,
                    gap,
                )
            }

            // ----- Assert -----
            resolved shouldBe indices.toList()
        }
    }
}
