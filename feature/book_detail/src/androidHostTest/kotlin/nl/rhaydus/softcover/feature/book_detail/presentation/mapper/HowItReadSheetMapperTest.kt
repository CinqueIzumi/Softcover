package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.traits.HowItReadSheetUiModel
import nl.rhaydus.softcover.core.domain.model.CharacterTrait
import nl.rhaydus.softcover.core.domain.model.Pace
import nl.rhaydus.softcover.core.domain.model.WritingFlag
import nl.rhaydus.softcover.core.domain.model.WritingStyle
import nl.rhaydus.softcover.feature.book_detail.presentation.state.HowItReadAnswer
import nl.rhaydus.softcover.feature.book_detail.presentation.state.HowItReadDraft
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HowItReadSheetMapperTest {
    private fun HowItReadSheetUiModel.allChips(): List<ChipUiModel> = listOf(pace, characters, writingStyle)
        .flatMap { it.rows }
        .flatMap { it.chips }

    private fun HowItReadSheetUiModel.chip(key: String): ChipUiModel = allChips().first { it.key == key }

    @Nested
    inner class ToHowItReadSheet {
        @Test
        fun `shows exactly the drafted selections as selected chips`() {
            // ----- Arrange -----
            val draft = HowItReadDraft(
                paceKey = Pace.FAST.toKey(),
                characters = persistentMapOf(CharacterTrait.LIKEABLE.toKey() to HowItReadAnswer.YES),
                writingStyleKey = WritingStyle.SIMPLE.toKey(),
                flagKeys = persistentSetOf(WritingFlag.VIVID.toKey()),
            )

            // ----- Act -----
            val result = draft.toHowItReadSheet(bookTitle = "Piranesi")

            // ----- Assert -----
            result.allChips().filter { it.selected }.map { it.key }.shouldContainExactlyInAnyOrder(
                Pace.FAST.toKey(),
                CharacterTrait.LIKEABLE.toKey(),
                WritingStyle.SIMPLE.toKey(),
                WritingFlag.VIVID.toKey(),
            )
        }

        @Test
        fun `shows a yes character chip as selected in the tonal tone`() {
            // ----- Arrange -----
            val draft = HowItReadDraft(
                characters = persistentMapOf(CharacterTrait.BELIEVABLE.toKey() to HowItReadAnswer.YES),
            )

            // ----- Act -----
            val result = draft.toHowItReadSheet(bookTitle = "Piranesi")

            // ----- Assert -----
            val chip = result.chip(CharacterTrait.BELIEVABLE.toKey())
            chip.selected shouldBe true
            chip.tone shouldBe ChipTone.Tonal
        }

        @Test
        fun `shows a complicated character chip with the dashed tone and unselected`() {
            // ----- Arrange -----
            val draft = HowItReadDraft(
                characters = persistentMapOf(CharacterTrait.BELIEVABLE.toKey() to HowItReadAnswer.COMPLICATED),
            )

            // ----- Act -----
            val result = draft.toHowItReadSheet(bookTitle = "Piranesi")

            // ----- Assert -----
            val chip = result.chip(CharacterTrait.BELIEVABLE.toKey())
            chip.tone shouldBe ChipTone.TonalDashed
            chip.selected shouldBe false
            result.allChips().filter { it.tone == ChipTone.TonalDashed }.size shouldBe 1
        }

        @Test
        fun `yields a null slider value when the balance is unset`() {
            // ----- Arrange -----
            val draft = HowItReadDraft(balance = null)

            // ----- Act -----
            val result = draft.toHowItReadSheet(bookTitle = "Piranesi")

            // ----- Assert -----
            result.balance.value shouldBe null
        }
    }
}
