package nl.rhaydus.softcover.core.uibinding.progress

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.component.progress.ProgressSheetMedium
import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
import nl.rhaydus.softcover.core.domain.model.ReadingFormat
import nl.rhaydus.softcover.core.domain.preview.PreviewData

class ProgressSheetMapperTest {
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
            result.medium shouldBe ProgressSheetMedium.Timed(
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
            result.medium shouldBe ProgressSheetMedium.Timed(
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
            result.medium shouldBe ProgressSheetMedium.Timed(
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
    inner class PassThrough {
        @Test
        fun `bookTitle passes through unchanged`() {
            // ----- Arrange -----
            val book = PreviewData.baseBook.copy(title = "The Starless Sea")

            // ----- Act -----
            val result = book.toProgressSheetUiModel(selectedTab = ProgressSheetTab.PAGE)

            // ----- Assert -----
            result.bookTitle shouldBe "The Starless Sea"
        }

        @Test
        fun `selectedTab passes through unchanged`() {
            // ----- Arrange -----
            val book = PreviewData.baseBook

            // ----- Act -----
            val result = book.toProgressSheetUiModel(selectedTab = ProgressSheetTab.TIME)

            // ----- Assert -----
            result.selectedTab shouldBe ProgressSheetTab.TIME
        }
    }
}
