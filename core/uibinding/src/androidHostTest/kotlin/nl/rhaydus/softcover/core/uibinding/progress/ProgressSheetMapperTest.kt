package nl.rhaydus.softcover.core.uibinding.progress

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.progress.ProgressSheetMedium
import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
import nl.rhaydus.softcover.core.component.progress.ProgressTimeUnit
import nl.rhaydus.softcover.core.component.progress.progressTimeField
import nl.rhaydus.softcover.core.component.sheet.SheetLayout
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ReadingFormat
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProgressSheetMapperTest {
    private fun expectedTimed(
        totalSeconds: Int,
        currentSeconds: Int,
        hours: String = "0",
        minutes: String = "0",
        seconds: String = "0",
    ): ProgressSheetMedium.Timed = ProgressSheetMedium.Timed(
        totalSeconds = totalSeconds,
        currentSeconds = currentSeconds,
        hours = progressTimeField(
            unit = ProgressTimeUnit.HOURS,
            text = hours,
        ),
        minutes = progressTimeField(
            unit = ProgressTimeUnit.MINUTES,
            text = minutes,
        ),
        seconds = progressTimeField(
            unit = ProgressTimeUnit.SECONDS,
            text = seconds,
        ),
    )

    private fun audioBook(currentSeconds: Int?): Book {
        val audioEdition = PreviewData.baseEdition.copy(
            id = 50,
            pages = null,
            audioSeconds = 20_000,
            readingFormat = ReadingFormat.Audio,
        )

        return PreviewData.baseBook.copy(
            editions = listOf(audioEdition),
            userBook = PreviewData.baseBook.userBook?.copy(editionId = audioEdition.id),
            userBookRead = PreviewData.baseBook.userBookRead?.copy(currentSeconds = currentSeconds),
        )
    }

    @Nested
    inner class MediumSelection {
        @Test
        fun `audiobook current edition maps medium to Timed`() {
            // ----- Arrange -----
            val audioEdition = PreviewData.baseEdition.copy(
                id = 2,
                pages = null,
                audioSeconds = 5_400,
                readingFormat = ReadingFormat.Audio,
            )
            val book = PreviewData.baseBook.copy(
                editions = listOf(PreviewData.baseEdition, audioEdition),
                userBook = PreviewData.baseBook.userBook?.copy(editionId = audioEdition.id),
            )

            // ----- Act -----
            val result = book.toProgressSheetUiModel(selectedTab = ProgressSheetTab.TIME)

            // ----- Assert -----
            book.currentEdition shouldBe audioEdition
            result.medium shouldBe expectedTimed(
                totalSeconds = 5_400,
                currentSeconds = 0,
            )
        }

        @Test
        fun `non-audio current edition maps medium to Paged`() {
            // ----- Arrange -----
            val book = PreviewData.baseBook

            // ----- Act -----
            val result = book.toProgressSheetUiModel(selectedTab = ProgressSheetTab.PAGE)

            // ----- Assert -----
            book.currentEdition shouldBe PreviewData.baseEdition
            result.medium shouldBe ProgressSheetMedium.Paged(
                totalPages = 100,
                currentPage = 0,
            )
        }
    }

    /**
     * The mapper deliberately resolves these two totals differently: pages fall back to the book's
     * default edition, seconds do not. These tests exist to pin that asymmetry down, not to flag it
     * as a bug — see the kdoc on [toProgressSheetUiModel].
     */
    @Nested
    inner class TotalFallbackAsymmetry {
        @Test
        fun `totalPages falls back to the book's default edition when the current edition has no page count`() {
            // ----- Arrange -----
            val currentEd = PreviewData.baseEdition.copy(
                id = 10,
                pages = null,
                readingFormat = ReadingFormat.Physical,
            )
            val defaultEd = PreviewData.baseEdition.copy(
                id = 11,
                pages = 250,
                readingFormat = ReadingFormat.Physical,
            )
            val book = PreviewData.baseBook.copy(
                editions = listOf(currentEd),
                defaultEdition = defaultEd,
                userBook = PreviewData.baseBook.userBook?.copy(editionId = currentEd.id),
            )

            // ----- Act -----
            val result = book.toProgressSheetUiModel(selectedTab = ProgressSheetTab.PAGE)

            // ----- Assert -----
            book.currentEdition shouldBe currentEd
            result.medium shouldBe ProgressSheetMedium.Paged(
                totalPages = 250,
                currentPage = 0,
            )
        }

        @Test
        fun `totalSeconds does NOT fall back to the book's default edition when the current edition has no audio seconds`() {
            // ----- Arrange -----
            val currentEd = PreviewData.baseEdition.copy(
                id = 20,
                pages = null,
                audioSeconds = null,
                readingFormat = ReadingFormat.Audio,
            )
            val defaultEd = PreviewData.baseEdition.copy(
                id = 21,
                audioSeconds = 9_000,
                readingFormat = ReadingFormat.Audio,
            )
            val book = PreviewData.baseBook.copy(
                editions = listOf(currentEd),
                defaultEdition = defaultEd,
                userBook = PreviewData.baseBook.userBook?.copy(editionId = currentEd.id),
            )

            // ----- Act -----
            val result = book.toProgressSheetUiModel(selectedTab = ProgressSheetTab.TIME)

            // ----- Assert -----
            book.currentEdition shouldBe currentEd
            result.medium shouldBe expectedTimed(
                totalSeconds = 0,
                currentSeconds = 0,
            )
            (result.medium as ProgressSheetMedium.Timed).hasKnownTotal shouldBe false
        }
    }

    @Nested
    inner class UnknownTotals {
        @Test
        fun `totalPages is 0 and hasKnownTotal is false when neither current nor default edition has a page count`() {
            // ----- Arrange -----
            val currentEd = PreviewData.baseEdition.copy(
                id = 30,
                pages = null,
                readingFormat = ReadingFormat.Physical,
            )
            val book = PreviewData.baseBook.copy(
                editions = listOf(currentEd),
                defaultEdition = null,
                userBook = PreviewData.baseBook.userBook?.copy(editionId = currentEd.id),
            )

            // ----- Act -----
            val result = book.toProgressSheetUiModel(selectedTab = ProgressSheetTab.PAGE)

            // ----- Assert -----
            book.currentEdition shouldBe currentEd
            result.medium shouldBe ProgressSheetMedium.Paged(
                totalPages = 0,
                currentPage = 0,
            )
            (result.medium as ProgressSheetMedium.Paged).hasKnownTotal shouldBe false
        }

        @Test
        fun `totalSeconds is 0 and hasKnownTotal is false when the current edition has no audio seconds`() {
            // ----- Arrange -----
            val currentEd = PreviewData.baseEdition.copy(
                id = 31,
                audioSeconds = null,
                readingFormat = ReadingFormat.Audio,
            )
            val book = PreviewData.baseBook.copy(
                editions = listOf(currentEd),
                defaultEdition = null,
                userBook = PreviewData.baseBook.userBook?.copy(editionId = currentEd.id),
            )

            // ----- Act -----
            val result = book.toProgressSheetUiModel(selectedTab = ProgressSheetTab.TIME)

            // ----- Assert -----
            book.currentEdition shouldBe currentEd
            result.medium shouldBe expectedTimed(
                totalSeconds = 0,
                currentSeconds = 0,
            )
            (result.medium as ProgressSheetMedium.Timed).hasKnownTotal shouldBe false
        }
    }

    @Nested
    inner class CurrentProgress {
        @Test
        fun `currentPage is 0 when userBookRead is null`() {
            // ----- Arrange -----
            val book = PreviewData.baseBook.copy(userBookRead = null)

            // ----- Act -----
            val result = book.toProgressSheetUiModel(selectedTab = ProgressSheetTab.PAGE)

            // ----- Assert -----
            (result.medium as ProgressSheetMedium.Paged).currentPage shouldBe 0
            result.progressPercent shouldBe 0
        }

        @Test
        fun `currentSeconds is 0 when userBookRead is null`() {
            // ----- Arrange -----
            val audioEdition = PreviewData.baseEdition.copy(
                id = 40,
                audioSeconds = 3_600,
                readingFormat = ReadingFormat.Audio,
            )
            val book = PreviewData.baseBook.copy(
                editions = listOf(audioEdition),
                userBook = PreviewData.baseBook.userBook?.copy(editionId = audioEdition.id),
                userBookRead = null,
            )

            // ----- Act -----
            val result = book.toProgressSheetUiModel(selectedTab = ProgressSheetTab.TIME)

            // ----- Assert -----
            book.currentEdition shouldBe audioEdition
            (result.medium as ProgressSheetMedium.Timed).currentSeconds shouldBe 0
            result.progressPercent shouldBe 0
        }
    }

    @Nested
    inner class ProgressPercentRounding {
        @Test
        fun `progressPercent rounds 34,6 up to 35`() {
            // ----- Arrange -----
            val book = PreviewData.baseBook.copy(
                userBookRead = PreviewData.baseBook.userBookRead?.copy(progress = 34.6f),
            )

            // ----- Act -----
            val result = book.toProgressSheetUiModel(selectedTab = ProgressSheetTab.PERCENTAGE)

            // ----- Assert -----
            result.progressPercent shouldBe 35
        }

        @Test
        fun `progressPercent rounds 34,4 down to 34`() {
            // ----- Arrange -----
            val book = PreviewData.baseBook.copy(
                userBookRead = PreviewData.baseBook.userBookRead?.copy(progress = 34.4f),
            )

            // ----- Act -----
            val result = book.toProgressSheetUiModel(selectedTab = ProgressSheetTab.PERCENTAGE)

            // ----- Assert -----
            result.progressPercent shouldBe 34
        }
    }

    @Nested
    inner class Scaffold {
        @Test
        fun `header is the reading-progress eyebrow over the book title, in a scrolling sheet`() {
            // ----- Arrange -----
            val book = PreviewData.baseBook.copy(title = "The Starless Sea")

            // ----- Act -----
            val scaffold = book.toProgressSheetUiModel(selectedTab = ProgressSheetTab.PAGE).scaffold

            // ----- Assert -----
            scaffold.header.section.eyebrow shouldBe "Reading progress"
            scaffold.header.section.headlineText shouldBe "The Starless Sea"
            scaffold.layout shouldBe SheetLayout.Scrolling
        }
    }

    @Nested
    inner class PassThrough {
        @Test
        fun `book title becomes the scaffold headline`() {
            // ----- Arrange -----
            val book = PreviewData.baseBook.copy(title = "The Starless Sea")

            // ----- Act -----
            val result = book.toProgressSheetUiModel(selectedTab = ProgressSheetTab.PAGE)

            // ----- Assert -----
            result.scaffold.header.section.headlineText shouldBe "The Starless Sea"
        }

        @Test
        fun `selectedTab passes through unchanged`() {
            // ----- Arrange -----
            val book = PreviewData.baseBook

            // ----- Act -----
            val result = book.toProgressSheetUiModel(selectedTab = ProgressSheetTab.PERCENTAGE)

            // ----- Assert -----
            result.selectedTab shouldBe ProgressSheetTab.PERCENTAGE
        }
    }

    @Nested
    inner class TimedFields {
        @Test
        fun `seeds the three fields from the book's listening position`() {
            // ----- Arrange -----
            val book = audioBook(currentSeconds = 3_725)

            // ----- Act -----
            val result = book.toProgressSheetUiModel(selectedTab = ProgressSheetTab.TIME)

            // ----- Assert -----
            result.medium shouldBe expectedTimed(
                totalSeconds = 20_000,
                currentSeconds = 3_725,
                hours = "1",
                minutes = "2",
                seconds = "5",
            )
        }

        @Test
        fun `uses a draft that was seeded from the current position`() {
            // ----- Arrange -----
            val book = audioBook(currentSeconds = 600)
            val draft = ProgressTimeDraft(
                forSeconds = 600,
                hours = "7",
                minutes = "8",
                seconds = "9",
            )

            // ----- Act -----
            val result = book.toProgressSheetUiModel(
                selectedTab = ProgressSheetTab.TIME,
                timeDraft = draft,
            )

            // ----- Assert -----
            result.medium shouldBe expectedTimed(
                totalSeconds = 20_000,
                currentSeconds = 600,
                hours = "7",
                minutes = "8",
                seconds = "9",
            )
        }

        @Test
        fun `discards a draft seeded from a position the book has moved past`() {
            // ----- Arrange -----
            val book = audioBook(currentSeconds = 3_600)
            val draft = ProgressTimeDraft(
                forSeconds = 600,
                hours = "7",
                minutes = "8",
                seconds = "9",
            )

            // ----- Act -----
            val result = book.toProgressSheetUiModel(
                selectedTab = ProgressSheetTab.TIME,
                timeDraft = draft,
            )

            // ----- Assert -----
            result.medium shouldBe expectedTimed(
                totalSeconds = 20_000,
                currentSeconds = 3_600,
                hours = "1",
            )
        }

        @Test
        fun `ignores a draft on a paged book`() {
            // ----- Arrange -----
            val draft = ProgressTimeDraft(
                forSeconds = 0,
                hours = "7",
                minutes = "8",
                seconds = "9",
            )

            // ----- Act -----
            val result = PreviewData.baseBook.toProgressSheetUiModel(
                selectedTab = ProgressSheetTab.PAGE,
                timeDraft = draft,
            )

            // ----- Assert -----
            result.medium shouldBe ProgressSheetMedium.Paged(
                totalPages = 100,
                currentPage = 0,
            )
        }
    }

    @Nested
    inner class ProgressCurrentSecondsProperty {
        @Test
        fun `is zero without a read`() {
            // ----- Arrange -----
            val book = PreviewData.baseBook.copy(userBookRead = null)

            // ----- Act -----
            val result = book.progressCurrentSeconds

            // ----- Assert -----
            result shouldBe 0
        }

        @Test
        fun `is zero when the read has no seconds`() {
            // ----- Arrange -----
            val book = audioBook(currentSeconds = null)

            // ----- Act -----
            val result = book.progressCurrentSeconds

            // ----- Assert -----
            result shouldBe 0
        }

        @Test
        fun `is the read's seconds`() {
            // ----- Arrange -----
            val book = audioBook(currentSeconds = 90)

            // ----- Act -----
            val result = book.progressCurrentSeconds

            // ----- Assert -----
            result shouldBe 90
        }
    }
}
