package nl.rhaydus.softcover.core.uibinding.progress

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import nl.rhaydus.softcover.core.component.progress.ProgressSheetMedium
import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
import nl.rhaydus.softcover.core.component.progress.ProgressTimeUnit
import nl.rhaydus.softcover.core.component.progress.progressTimeField
import nl.rhaydus.softcover.core.component.sheet.SheetFooterEmphasis
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.ReadingFormat
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProgressSheetMapperTest {
    private fun expectedTimed(
        totalSeconds: Int,
        hours: String = "0",
        minutes: String = "0",
        seconds: String = "0",
        suffix: String?,
        fraction: Float = 0f,
    ): ProgressSheetMedium.Timed = ProgressSheetMedium.Timed(
        totalSeconds = totalSeconds,
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
        suffix = suffix,
        fraction = fraction,
        canDecreaseMinutes = minutes != "0",
        canIncreaseMinutes = minutes != "59",
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
            userBook = PreviewData.baseBook.userBook?.copy(
                status = BookStatus.Reading,
                editionId = audioEdition.id,
                reads = PreviewData.baseBook.userBook?.reads.orEmpty().map { it.copy(currentSeconds = currentSeconds) },
            ),
        )
    }

    @Nested
    inner class MediumSelection {
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
                suffix = "of 100 pages",
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
                suffix = "of 250 pages",
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
                suffix = null,
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
                suffix = null,
            )
            (result.medium as ProgressSheetMedium.Paged).hasKnownTotal shouldBe false
        }
    }

    @Nested
    inner class CurrentProgress {
        @Test
        fun `currentPage is 0 when there is no current read`() {
            // ----- Arrange -----
            val book = PreviewData.baseBook.copy(userBook = PreviewData.baseBook.userBook?.copy(reads = emptyList()))

            // ----- Act -----
            val result = book.toProgressSheetUiModel(selectedTab = ProgressSheetTab.PAGE)

            // ----- Assert -----
            (result.medium as ProgressSheetMedium.Paged).currentPage shouldBe 0
            result.progressPercent shouldBe 0
        }

        @Test
        fun `currentSeconds is 0 when there is no current read`() {
            // ----- Arrange -----
            val audioEdition = PreviewData.baseEdition.copy(
                id = 40,
                audioSeconds = 3_600,
                readingFormat = ReadingFormat.Audio,
            )
            val book = PreviewData.baseBook.copy(
                editions = listOf(audioEdition),
                userBook = PreviewData.baseBook.userBook?.copy(
                    status = BookStatus.Reading,
                    editionId = audioEdition.id,
                    reads = emptyList(),
                ),
            )

            // ----- Act -----
            val result = book.toProgressSheetUiModel(selectedTab = ProgressSheetTab.TIME)

            // ----- Assert -----
            book.currentEdition shouldBe audioEdition
            result.progressPercent shouldBe 0
        }
    }

    @Nested
    inner class ProgressPercentRounding {
        @Test
        fun `progressPercent rounds 34,6 up to 35`() {
            // ----- Arrange -----
            val book = PreviewData.baseBook.copy(
                userBook = PreviewData.baseBook.userBook?.copy(
                    status = BookStatus.Reading,
                    reads = PreviewData.baseBook.userBook?.reads.orEmpty().map { it.copy(progress = 34.6f) },
                ),
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
                userBook = PreviewData.baseBook.userBook?.copy(
                    status = BookStatus.Reading,
                    reads = PreviewData.baseBook.userBook?.reads.orEmpty().map { it.copy(progress = 34.4f) },
                ),
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
        fun `header headline is the book title`() {
            // ----- Arrange -----
            val book = PreviewData.baseBook.copy(title = "The Starless Sea")

            // ----- Act -----
            val scaffold = book.toProgressSheetUiModel(selectedTab = ProgressSheetTab.PAGE).scaffold

            // ----- Assert -----
            scaffold.header.shouldNotBeNull().section.headlineText shouldBe "The Starless Sea"
        }

        @Test
        fun `secondary footer button is an outlined Mark as read with the check icon that dismisses the sheet`() {
            // ----- Arrange -----
            val book = PreviewData.baseBook

            // ----- Act -----
            val footer = book.toProgressSheetUiModel(selectedTab = ProgressSheetTab.PAGE).scaffold.footer

            // ----- Assert -----
            val secondary = footer.shouldBeInstanceOf<SheetFooterUiModel.Stacked>().secondary.shouldNotBeNull()
            secondary.label shouldBe "Mark as read"
            secondary.dismissesSheet shouldBe true
            secondary.emphasis shouldBe SheetFooterEmphasis.Outlined
            secondary.icon shouldBe SoftcoverIcon.Check
        }

        @Test
        fun `a paused book gets the on hold eyebrow and the resume button`() {
            // ----- Arrange -----
            val userBook = PreviewData.baseBook.userBook.shouldNotBeNull().copy(status = BookStatus.Paused)
            val book = PreviewData.baseBook.copy(userBook = userBook)

            // ----- Act -----
            val scaffold = book.toProgressSheetUiModel(selectedTab = ProgressSheetTab.PAGE).scaffold

            // ----- Assert -----
            val eyebrow = scaffold.header.shouldNotBeNull().section.eyebrow
            val primary = scaffold.footer.shouldBeInstanceOf<SheetFooterUiModel.Stacked>().primary.shouldNotBeNull()
            eyebrow shouldBe "On hold"
            primary.label shouldBe "Update and resume"
        }

        @Test
        fun `a reading book keeps the reading eyebrow and the update button`() {
            // ----- Arrange -----
            val userBook = PreviewData.baseBook.userBook.shouldNotBeNull().copy(status = BookStatus.Reading)
            val book = PreviewData.baseBook.copy(userBook = userBook)

            // ----- Act -----
            val scaffold = book.toProgressSheetUiModel(selectedTab = ProgressSheetTab.PAGE).scaffold

            // ----- Assert -----
            val eyebrow = scaffold.header.shouldNotBeNull().section.eyebrow
            val primary = scaffold.footer.shouldBeInstanceOf<SheetFooterUiModel.Stacked>().primary.shouldNotBeNull()
            eyebrow shouldBe "Reading progress"
            primary.label shouldBe "Update progress"
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
                hours = "1",
                minutes = "2",
                seconds = "5",
                suffix = "of 05:33:20",
                fraction = 3_725f / 20_000,
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
                hours = "7",
                minutes = "8",
                seconds = "9",
                suffix = "of 05:33:20",
                fraction = 25_689f / 20_000,
            )
        }

        @Test
        fun `keeps cleared draft text so the field can be emptied`() {
            // ----- Arrange -----
            val book = audioBook(currentSeconds = 600)
            val draft = ProgressTimeDraft(
                forSeconds = 600,
                hours = "",
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
                hours = "",
                minutes = "8",
                seconds = "9",
                suffix = "of 05:33:20",
                fraction = 489f / 20_000,
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
                hours = "1",
                suffix = "of 05:33:20",
                fraction = 3_600f / 20_000,
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
                suffix = "of 100 pages",
            )
        }
    }

    @Nested
    inner class MinuteStepperBounds {
        @Test
        fun `minutes at zero can only increase`() {
            // ----- Arrange -----
            val book = audioBook(currentSeconds = 600)
            val draft = ProgressTimeDraft(
                forSeconds = 600,
                hours = "0",
                minutes = "0",
                seconds = "0",
            )

            // ----- Act -----
            val result = book.toProgressSheetUiModel(
                selectedTab = ProgressSheetTab.TIME,
                timeDraft = draft,
            )

            // ----- Assert -----
            val timed = (result.medium as? ProgressSheetMedium.Timed).shouldNotBeNull()
            timed.canDecreaseMinutes shouldBe false
            timed.canIncreaseMinutes shouldBe true
        }

        @Test
        fun `minutes at the maximum can only decrease`() {
            // ----- Arrange -----
            val book = audioBook(currentSeconds = 600)
            val draft = ProgressTimeDraft(
                forSeconds = 600,
                hours = "0",
                minutes = "59",
                seconds = "0",
            )

            // ----- Act -----
            val result = book.toProgressSheetUiModel(
                selectedTab = ProgressSheetTab.TIME,
                timeDraft = draft,
            )

            // ----- Assert -----
            val timed = (result.medium as? ProgressSheetMedium.Timed).shouldNotBeNull()
            timed.canDecreaseMinutes shouldBe true
            timed.canIncreaseMinutes shouldBe false
        }

        @Test
        fun `minutes between the bounds can step both ways`() {
            // ----- Arrange -----
            val book = audioBook(currentSeconds = 600)
            val draft = ProgressTimeDraft(
                forSeconds = 600,
                hours = "0",
                minutes = "30",
                seconds = "0",
            )

            // ----- Act -----
            val result = book.toProgressSheetUiModel(
                selectedTab = ProgressSheetTab.TIME,
                timeDraft = draft,
            )

            // ----- Assert -----
            val timed = (result.medium as? ProgressSheetMedium.Timed).shouldNotBeNull()
            timed.canDecreaseMinutes shouldBe true
            timed.canIncreaseMinutes shouldBe true
        }

        @Test
        fun `a blank minutes draft is treated as zero`() {
            // ----- Arrange -----
            val book = audioBook(currentSeconds = 600)
            val draft = ProgressTimeDraft(
                forSeconds = 600,
                hours = "0",
                minutes = "",
                seconds = "0",
            )

            // ----- Act -----
            val result = book.toProgressSheetUiModel(
                selectedTab = ProgressSheetTab.TIME,
                timeDraft = draft,
            )

            // ----- Assert -----
            val timed = (result.medium as? ProgressSheetMedium.Timed).shouldNotBeNull()
            timed.canDecreaseMinutes shouldBe false
            timed.canIncreaseMinutes shouldBe true
        }
    }

    @Nested
    inner class ProgressCurrentSecondsProperty {
        @Test
        fun `is zero without a read`() {
            // ----- Arrange -----
            val book = PreviewData.baseBook.copy(userBook = PreviewData.baseBook.userBook?.copy(reads = emptyList()))

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
