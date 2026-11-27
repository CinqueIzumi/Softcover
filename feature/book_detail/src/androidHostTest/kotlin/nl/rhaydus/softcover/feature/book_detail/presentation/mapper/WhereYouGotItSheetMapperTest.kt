package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.AcquisitionKind
import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItDraft
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WhereYouGotItSheetMapperTest {
    @Nested
    inner class ToWhereYouGotItSheet {
        @Test
        fun `selects only the drafted preset row and leaves the input unticked`() {
            // ----- Arrange -----
            val draft = WhereYouGotItDraft.Preset(key = AcquisitionKind.GIFT.toKey())

            // ----- Act -----
            val result = draft.toWhereYouGotItSheet(bookTitle = "Piranesi")

            // ----- Assert -----
            result.group.rows.filter { it.selected }.map { it.key } shouldBe listOf(AcquisitionKind.GIFT.toKey())
            result.group.input?.selected shouldBe false
            result.group.input?.field?.text shouldBe ""
        }

        @Test
        fun `selects the input row and carries its text for custom text`() {
            // ----- Arrange -----
            val draft = WhereYouGotItDraft.Custom(text = "A street stall")

            // ----- Act -----
            val result = draft.toWhereYouGotItSheet(bookTitle = "Piranesi")

            // ----- Assert -----
            result.group.rows.filter { it.selected }.shouldBeEmpty()
            result.group.input?.selected shouldBe true
            result.group.input?.field?.text shouldBe "A street stall"
        }

        @Test
        fun `selects nothing for none`() {
            // ----- Arrange -----
            val draft = WhereYouGotItDraft.None

            // ----- Act -----
            val result = draft.toWhereYouGotItSheet(bookTitle = "Piranesi")

            // ----- Assert -----
            result.group.rows.filter { it.selected }.shouldBeEmpty()
            result.group.input?.selected shouldBe false
        }

        @Test
        fun `offers a row for every acquisition kind`() {
            // ----- Arrange -----
            val draft = WhereYouGotItDraft.None

            // ----- Act -----
            val result = draft.toWhereYouGotItSheet(bookTitle = "Piranesi")

            // ----- Assert -----
            result.group.rows.map { it.key } shouldBe AcquisitionKind.entries.map { it.toKey() }
        }
    }
}
