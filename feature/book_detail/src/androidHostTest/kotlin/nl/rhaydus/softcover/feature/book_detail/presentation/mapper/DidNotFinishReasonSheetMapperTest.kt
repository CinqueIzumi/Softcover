package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.DidNotFinishReason
import nl.rhaydus.softcover.core.domain.model.DidNotFinishReasons
import nl.rhaydus.softcover.feature.book_detail.presentation.buildDidNotFinishReasonDraft
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DidNotFinishReasonSheetMapperTest {
    @Nested
    inner class ToDidNotFinishReasonSheet {
        @Test
        fun `selects exactly the chips of the drafted reasons`() {
            // ----- Arrange -----
            val draft = buildDidNotFinishReasonDraft(
                reasons = setOf(DidNotFinishReason.TooSlow, DidNotFinishReason.NotTheRightTime),
            )

            // ----- Act -----
            val sheet = draft.toDidNotFinishReasonSheet(bookTitle = "Piranesi")

            // ----- Assert -----
            sheet.reasons.chips.filter { it.selected }.map { sheet.reasons[it.key] } shouldBe listOf(
                DidNotFinishReason.NotTheRightTime,
                DidNotFinishReason.TooSlow,
            )
        }

        @Test
        fun `offers every reason in enum order with none selected for an empty draft`() {
            // ----- Arrange -----
            val draft = buildDidNotFinishReasonDraft()

            // ----- Act -----
            val sheet = draft.toDidNotFinishReasonSheet(bookTitle = "Piranesi")

            // ----- Assert -----
            sheet.reasons.chips.map { sheet.reasons[it.key] } shouldBe DidNotFinishReason.entries
            sheet.reasons.chips.any { it.selected } shouldBe false
        }
    }

    @Nested
    inner class WithReasonToggled {
        @Test
        fun `adds a reason that is not selected`() {
            // ----- Arrange -----
            val draft = buildDidNotFinishReasonDraft(reasons = setOf(DidNotFinishReason.TooSlow))

            // ----- Act -----
            val result = draft.withReasonToggled(reason = DidNotFinishReason.LostInterest)

            // ----- Assert -----
            result.reasons shouldBe setOf(DidNotFinishReason.TooSlow, DidNotFinishReason.LostInterest)
        }

        @Test
        fun `removes a reason that is selected and keeps the others`() {
            // ----- Arrange -----
            val draft = buildDidNotFinishReasonDraft(
                reasons = setOf(DidNotFinishReason.TooSlow, DidNotFinishReason.LostInterest),
                ownWords = "Kept",
            )

            // ----- Act -----
            val result = draft.withReasonToggled(reason = DidNotFinishReason.TooSlow)

            // ----- Assert -----
            result.reasons shouldBe setOf(DidNotFinishReason.LostInterest)
            result.ownWords shouldBe "Kept"
        }
    }

    @Nested
    inner class ToDidNotFinishReasonDraft {
        @Test
        fun `seeds empty own words when none are stored`() {
            // ----- Arrange -----
            val stored = DidNotFinishReasons(bookId = 42)

            // ----- Act -----
            val draft = stored.toDidNotFinishReasonDraft()

            // ----- Assert -----
            draft shouldBe buildDidNotFinishReasonDraft()
        }
    }
}
