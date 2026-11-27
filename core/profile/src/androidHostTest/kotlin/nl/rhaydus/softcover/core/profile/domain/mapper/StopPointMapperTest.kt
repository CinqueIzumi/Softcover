package nl.rhaydus.softcover.core.profile.domain.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.profile.domain.model.StopPoint
import nl.rhaydus.softcover.core.profile.domain.model.StopQuarter
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class StopPointMapperTest {
    @Nested
    inner class ToStopPoint {
        @Test
        fun `is unknown when the newest read is finished`() {
            // ----- Arrange -----
            val book = readBook(
                id = 1,
                pages = 300,
                reads = listOf(
                    unfinishedRead(
                        id = 1,
                        currentPage = 100,
                        progress = 33f,
                    ),
                    finishedRead(
                        id = 2,
                        finishedAt = "2026-01-01",
                    ),
                ),
            )

            // ----- Act -----
            val result = book.toStopPoint()

            // ----- Assert -----
            result shouldBe StopPoint(
                page = null,
                fraction = null,
            )
        }

        @Test
        fun `is unknown when the book has no reads`() {
            // ----- Arrange -----
            val book = dnfBook(
                id = 1,
                pages = 300,
                read = null,
            )

            // ----- Act -----
            val result = book.toStopPoint()

            // ----- Assert -----
            result shouldBe StopPoint(
                page = null,
                fraction = null,
            )
        }

        @Test
        fun `reads the newest read, not an older unfinished one`() {
            // ----- Arrange -----
            val book = readBook(
                id = 1,
                pages = 300,
                reads = listOf(
                    unfinishedRead(
                        id = 1,
                        currentPage = 10,
                    ),
                    unfinishedRead(
                        id = 2,
                        currentPage = 200,
                    ),
                ),
            )

            // ----- Act -----
            val result = book.toStopPoint()

            // ----- Assert -----
            result.page shouldBe 200
        }

        @Test
        fun `takes the page from the current page and the fraction from the page over the book pages`() {
            // ----- Arrange -----
            val book = dnfBook(
                id = 1,
                pages = 400,
                read = unfinishedRead(
                    id = 1,
                    currentPage = 160,
                ),
            )

            // ----- Act -----
            val result = book.toStopPoint()

            // ----- Assert -----
            result shouldBe StopPoint(
                page = 160,
                fraction = 0.4,
            )
        }

        @Test
        fun `derives the page from progress and the edition pages of the read`() {
            // ----- Arrange -----
            val book = dnfBook(
                id = 1,
                pages = 999,
                read = unfinishedRead(
                    id = 1,
                    progress = 50f,
                    editionPages = 200,
                ),
            )

            // ----- Act -----
            val result = book.toStopPoint()

            // ----- Assert -----
            result shouldBe StopPoint(
                page = 100,
                fraction = 0.5,
            )
        }

        @Test
        fun `falls back to the book pages when the read has no edition pages`() {
            // ----- Arrange -----
            val book = dnfBook(
                id = 1,
                pages = 300,
                read = unfinishedRead(
                    id = 1,
                    progress = 50f,
                ),
            )

            // ----- Act -----
            val result = book.toStopPoint()

            // ----- Assert -----
            result.page shouldBe 150
        }

        @Test
        fun `rounds a derived page to the nearest page`() {
            // ----- Arrange -----
            val book = dnfBook(
                id = 1,
                read = unfinishedRead(
                    id = 1,
                    progress = 33.3f,
                    editionPages = 200,
                ),
            )

            // ----- Act -----
            val result = book.toStopPoint()

            // ----- Assert -----
            result.page shouldBe 67
        }

        @Test
        fun `prefers the current page for the page and progress for the fraction`() {
            // ----- Arrange -----
            val book = dnfBook(
                id = 1,
                pages = 200,
                read = unfinishedRead(
                    id = 1,
                    currentPage = 40,
                    progress = 60f,
                ),
            )

            // ----- Act -----
            val result = book.toStopPoint()

            // ----- Assert -----
            result shouldBe StopPoint(
                page = 40,
                fraction = 0.6,
            )
        }

        @Test
        fun `places the quarter by progress with the boundaries opening the next quarter`() {
            // ----- Arrange -----
            val progresses = listOf(
                10f,
                24.9f,
                25f,
                49.9f,
                50f,
                74.9f,
                75f,
                100f,
                130f,
            )

            // ----- Act -----
            val result = progresses.map { progress ->
                dnfBook(
                    id = 1,
                    pages = 100,
                    read = unfinishedRead(
                        id = 1,
                        progress = progress,
                    ),
                ).toStopPoint().quarter
            }

            // ----- Assert -----
            result shouldBe listOf(
                StopQuarter.FIRST,
                StopQuarter.FIRST,
                StopQuarter.SECOND,
                StopQuarter.SECOND,
                StopQuarter.THIRD,
                StopQuarter.THIRD,
                StopQuarter.LAST,
                StopQuarter.LAST,
                StopQuarter.LAST,
            )
        }

        @Test
        fun `keeps the page but has no fraction when progress is zero and the page count is unknown`() {
            // ----- Arrange -----
            val book = dnfBook(
                id = 1,
                read = unfinishedRead(
                    id = 1,
                    currentPage = 150,
                ),
            )

            // ----- Act -----
            val result = book.toStopPoint()

            // ----- Assert -----
            result shouldBe StopPoint(
                page = 150,
                fraction = null,
            )
        }

        @Test
        fun `keeps the fraction but has no page when progress is set and the page count is unknown`() {
            // ----- Arrange -----
            val book = dnfBook(
                id = 1,
                read = unfinishedRead(
                    id = 1,
                    progress = 80f,
                ),
            )

            // ----- Act -----
            val result = book.toStopPoint()

            // ----- Assert -----
            result shouldBe StopPoint(
                page = null,
                fraction = 0.8,
            )
        }

        @Test
        fun `is unknown when there is neither progress nor a current page`() {
            // ----- Arrange -----
            val book = dnfBook(
                id = 1,
                pages = 300,
                read = unfinishedRead(id = 1),
            )

            // ----- Act -----
            val result = book.toStopPoint()

            // ----- Assert -----
            result shouldBe StopPoint(
                page = null,
                fraction = null,
            )
        }

        @Test
        fun `clamps the fraction to the whole book`() {
            // ----- Arrange -----
            val overProgress = dnfBook(
                id = 1,
                pages = 100,
                read = unfinishedRead(
                    id = 1,
                    progress = 130f,
                ),
            )

            val pastLastPage = dnfBook(
                id = 2,
                pages = 400,
                read = unfinishedRead(
                    id = 2,
                    currentPage = 500,
                ),
            )

            // ----- Act -----
            val result = listOf(overProgress, pastLastPage).map { it.toStopPoint().fraction }

            // ----- Assert -----
            result shouldBe listOf(1.0, 1.0)
        }
    }
}
