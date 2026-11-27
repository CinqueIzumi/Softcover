package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.AcquisitionKind
import nl.rhaydus.softcover.core.domain.model.AcquisitionSource
import nl.rhaydus.softcover.core.domain.model.BookCopy
import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItDraft
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WhereYouGotItDraftMapperTest {
    @Nested
    inner class ToWhereYouGotItDraft {
        @Test
        fun `drafts a preset source as that preset's key`() {
            // ----- Arrange -----
            val copy = BookCopy(
                bookId = 1,
                source = AcquisitionSource.Preset(kind = AcquisitionKind.GIFT),
            )

            // ----- Act -----
            val result = copy.toWhereYouGotItDraft()

            // ----- Assert -----
            result shouldBe WhereYouGotItDraft.Preset(key = AcquisitionKind.GIFT.toKey())
        }

        @Test
        fun `drafts another source as custom text`() {
            // ----- Arrange -----
            val copy = BookCopy(
                bookId = 1,
                source = AcquisitionSource.Other(text = "A street stall"),
            )

            // ----- Act -----
            val result = copy.toWhereYouGotItDraft()

            // ----- Assert -----
            result shouldBe WhereYouGotItDraft.Custom(text = "A street stall")
        }

        @Test
        fun `drafts a copy without a source as none`() {
            // ----- Arrange -----
            val copy = BookCopy(
                bookId = 1,
                source = null,
            )

            // ----- Act -----
            val result = copy.toWhereYouGotItDraft()

            // ----- Assert -----
            result shouldBe WhereYouGotItDraft.None
        }
    }

    @Nested
    inner class WithPresetClicked {
        @Test
        fun `picks the tapped preset from none`() {
            // ----- Arrange -----
            val draft = WhereYouGotItDraft.None

            // ----- Act -----
            val result = draft.withPresetClicked(key = AcquisitionKind.LIBRARY.toKey())

            // ----- Assert -----
            result shouldBe WhereYouGotItDraft.Preset(key = AcquisitionKind.LIBRARY.toKey())
        }

        @Test
        fun `replaces the picked preset when a different one is tapped`() {
            // ----- Arrange -----
            val draft = WhereYouGotItDraft.Preset(key = AcquisitionKind.LIBRARY.toKey())

            // ----- Act -----
            val result = draft.withPresetClicked(key = AcquisitionKind.GIFT.toKey())

            // ----- Assert -----
            result shouldBe WhereYouGotItDraft.Preset(key = AcquisitionKind.GIFT.toKey())
        }

        @Test
        fun `clears to none when the picked preset is tapped again`() {
            // ----- Arrange -----
            val draft = WhereYouGotItDraft.Preset(key = AcquisitionKind.LIBRARY.toKey())

            // ----- Act -----
            val result = draft.withPresetClicked(key = AcquisitionKind.LIBRARY.toKey())

            // ----- Assert -----
            result shouldBe WhereYouGotItDraft.None
        }

        @Test
        fun `replaces custom text with the tapped preset`() {
            // ----- Arrange -----
            val draft = WhereYouGotItDraft.Custom(text = "A street stall")

            // ----- Act -----
            val result = draft.withPresetClicked(key = AcquisitionKind.BORROWED.toKey())

            // ----- Assert -----
            result shouldBe WhereYouGotItDraft.Preset(key = AcquisitionKind.BORROWED.toKey())
        }
    }

    @Nested
    inner class WithCustomText {
        @Test
        fun `turns typed text into custom text`() {
            // ----- Arrange -----
            val draft = WhereYouGotItDraft.None

            // ----- Act -----
            val result = draft.withCustomText(text = "A street stall")

            // ----- Assert -----
            result shouldBe WhereYouGotItDraft.Custom(text = "A street stall")
        }

        @Test
        fun `unticks a picked preset when text is typed`() {
            // ----- Arrange -----
            val draft = WhereYouGotItDraft.Preset(key = AcquisitionKind.LIBRARY.toKey())

            // ----- Act -----
            val result = draft.withCustomText(text = "A")

            // ----- Assert -----
            result shouldBe WhereYouGotItDraft.Custom(text = "A")
        }

        @Test
        fun `clears to none when the text is emptied`() {
            // ----- Arrange -----
            val draft = WhereYouGotItDraft.Custom(text = "A")

            // ----- Act -----
            val result = draft.withCustomText(text = "")

            // ----- Assert -----
            result shouldBe WhereYouGotItDraft.None
        }

        @Test
        fun `keeps whitespace-only text as custom text so the field keeps what was typed`() {
            // ----- Arrange -----
            val draft = WhereYouGotItDraft.None

            // ----- Act -----
            val result = draft.withCustomText(text = "  ")

            // ----- Assert -----
            result shouldBe WhereYouGotItDraft.Custom(text = "  ")
        }
    }

    @Nested
    inner class ToSource {
        @Test
        fun `maps none to no source`() {
            // ----- Arrange -----
            val draft = WhereYouGotItDraft.None

            // ----- Act -----
            val result = draft.toSource()

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `maps a preset to its kind`() {
            // ----- Arrange -----
            val draft = WhereYouGotItDraft.Preset(key = AcquisitionKind.BOUGHT_ONLINE.toKey())

            // ----- Act -----
            val result = draft.toSource()

            // ----- Assert -----
            result shouldBe AcquisitionSource.Preset(kind = AcquisitionKind.BOUGHT_ONLINE)
        }

        @Test
        fun `maps a preset with an unknown key to no source`() {
            // ----- Arrange -----
            val draft = WhereYouGotItDraft.Preset(key = "acquired-unknown")

            // ----- Act -----
            val result = draft.toSource()

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `trims custom text`() {
            // ----- Arrange -----
            val draft = WhereYouGotItDraft.Custom(text = "  A street stall \n")

            // ----- Act -----
            val result = draft.toSource()

            // ----- Assert -----
            result shouldBe AcquisitionSource.Other(text = "A street stall")
        }

        @Test
        fun `maps blank custom text to no source`() {
            // ----- Arrange -----
            val draft = WhereYouGotItDraft.Custom(text = " \t ")

            // ----- Act -----
            val result = draft.toSource()

            // ----- Assert -----
            result shouldBe null
        }
    }
}
