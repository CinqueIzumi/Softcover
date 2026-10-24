package nl.rhaydus.softcover.core.component.traits

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.toChipSet
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterEmphasis
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

/**
 * Everything [HowItReadSheet] renders: the draft of a book's private survey. Every draft value is
 * host-owned.
 *
 * [pace] and the first row of [writingStyle] are single choice; the host clears a re-tapped selection. The
 * chips of [characters] cycle through off, yes and [ChipTone.TonalDashed], which the host sets.
 */
@Immutable
data class HowItReadSheetUiModel(
    val pace: HowItReadChipGroupUiModel,
    val balance: HowItReadBalanceUiModel,
    val characters: HowItReadChipGroupUiModel,
    val writingStyle: HowItReadChipGroupUiModel,
    val scaffold: SheetScaffoldUiModel,
) : SheetModel<HowItReadSheetUiModel> {
    override val closeRequested: Boolean get() = scaffold.closeRequested

    override fun requestingClose(): HowItReadSheetUiModel = copy(scaffold = scaffold.requestingClose())

    companion object : UiModelPreviews<HowItReadSheetUiModel> {
        fun scaffoldFor(bookTitle: String): SheetScaffoldUiModel = SheetScaffoldUiModel(
            header = SheetHeaderUiModel(
                section = SectionHeaderUiModel.Section(
                    eyebrow = bookTitle,
                    headline = "How it read",
                    description = "Answer what you like and skip the rest.",
                ),
            ),
            footer = SheetFooterUiModel.Inline(
                primary = SheetFooterButton(
                    label = "Save",
                    dismissesSheet = true,
                ),
                secondary = SheetFooterButton(
                    label = "Cancel",
                    emphasis = SheetFooterEmphasis.Outlined,
                    dismissesSheet = true,
                ),
            ),
        )

        private fun chipSet(
            options: List<Pair<String, String>>,
            selected: Set<String>,
            complicated: Set<String>,
        ): ChipSet<String> = options
            .map { (key, label) ->
                ChipUiModel(
                    key = key,
                    label = label,
                    selected = key in selected,
                    tone = if (key in complicated) ChipTone.TonalDashed else ChipTone.Tonal,
                ) to key
            }
            .toChipSet()

        private fun draft(
            balance: Float?,
            selected: Set<String>,
            complicated: Set<String> = emptySet(),
        ): HowItReadSheetUiModel {
            fun rowOf(options: List<Pair<String, String>>): ChipSet<String> = chipSet(
                options = options,
                selected = selected,
                complicated = complicated,
            )

            return HowItReadSheetUiModel(
                pace = HowItReadChipGroupUiModel(
                    label = "Pace",
                    rows = persistentListOf(
                        rowOf(
                            listOf(
                                "pace-slow" to "Slow",
                                "pace-moderate" to "Moderate",
                                "pace-fast" to "Fast",
                                "pace-variable" to "Variable",
                            ),
                        ),
                    ),
                ),
                balance = HowItReadBalanceUiModel(
                    label = "Plot or character",
                    value = balance,
                    startLabel = "Plot",
                    centreLabel = "A mix",
                    endLabel = "Character",
                    clearLabel = "Clear",
                    unsetLabel = "Not answered",
                ),
                characters = HowItReadChipGroupUiModel(
                    label = "The characters were",
                    rows = persistentListOf(
                        rowOf(
                            listOf(
                                "likeable" to "Likeable",
                                "believable" to "Believable",
                                "well-developed" to "Well developed",
                                "multi-layered" to "Multi-layered",
                                "flawed" to "Defined by their flaws",
                                "bipoc" to "BIPOC characters",
                                "lgbtq" to "LGBTQ+ characters",
                            ),
                        ),
                    ),
                    caption = "Tap once for yes, again for complicated, a third time to clear.",
                ),
                writingStyle = HowItReadChipGroupUiModel(
                    label = "Writing style",
                    rows = persistentListOf(
                        rowOf(
                            listOf(
                                "style-simple" to "Simple",
                                "style-moderate" to "Moderate",
                                "style-demanding" to "Demanding",
                            ),
                        ),
                        rowOf(
                            listOf(
                                "flag-vivid" to "Vivid",
                                "flag-poetic" to "Poetic",
                                "flag-unconventional" to "Unconventional",
                            ),
                        ),
                    ),
                ),
                scaffold = scaffoldFor(bookTitle = "Piranesi"),
            )
        }

        /** An empty draft, one with a complicated chip and the slider set, and one answered with the slider unset. */
        override val previews: ImmutableMap<String, HowItReadSheetUiModel> = persistentMapOf(
            "Empty draft" to draft(
                balance = null,
                selected = emptySet(),
            ),
            "Filled draft" to draft(
                balance = 0.8f,
                selected = setOf(
                    "pace-variable",
                    "likeable",
                    "believable",
                    "style-demanding",
                    "flag-poetic",
                ),
                complicated = setOf("multi-layered"),
            ),
            "Answered, slider unset" to draft(
                balance = null,
                selected = setOf(
                    "pace-slow",
                    "well-developed",
                    "style-simple",
                ),
            ),
        )
    }
}
