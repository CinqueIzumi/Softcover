package nl.rhaydus.softcover.core.component.feeling

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.toChipSet
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldVariant
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterEmphasis
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/** Everything [HowItFeltSheet] renders, with one [strengths] row per selected mood. */
@Immutable
data class HowItFeltSheetUiModel(
    val choices: MoodChoicesUiModel,
    val strengths: ImmutableList<MoodStrengthUiModel>,
    val caption: String,
    val scaffold: SheetScaffoldUiModel,
) : SheetModel<HowItFeltSheetUiModel> {
    override val closeRequested: Boolean get() = scaffold.closeRequested

    override fun requestingClose(): HowItFeltSheetUiModel = copy(scaffold = scaffold.requestingClose())

    companion object : UiModelPreviews<HowItFeltSheetUiModel> {
        private fun draft(
            selected: Map<String, Int?>,
            ownWordOpen: Boolean = false,
        ): HowItFeltSheetUiModel {
            val moods = listOf(
                "tense" to "Tense",
                "hopeful" to "Hopeful",
                "funny" to "Funny",
                "sad" to "Sad",
                "reflective" to "Reflective",
                "dark" to "Dark",
                "cosy" to "Cosy",
            )

            return HowItFeltSheetUiModel(
                choices = MoodChoicesUiModel(
                    chips = (
                        moods.map { (key, label) ->
                            ChipUiModel(
                                key = key,
                                label = label,
                                selected = key in selected,
                            ) to key
                        } + (
                            ChipUiModel(
                                key = "own-word",
                                label = "Your own word",
                                tone = ChipTone.Dashed,
                            ) to "own-word"
                        )
                    ).toChipSet(),
                    ownWord = TextFieldUiModel(
                        key = "own-word",
                        text = "",
                        placeholder = "A word of your own",
                        variant = TextFieldVariant.Pill(
                            leadingIcon = SoftcoverIcon.Edit,
                        ),
                        submittable = true,
                    ),
                    ownWordOpen = ownWordOpen,
                ),
                strengths = moods
                    .filter { (key, _) -> key in selected }
                    .map { (key, label) ->
                        MoodStrengthUiModel(
                            key = key,
                            label = label,
                            strength = selected[key],
                            unsetLabel = "Not set",
                        )
                    }
                    .let { it.toPersistentList() },
                caption = "Moods along the way are added when you update your progress.",
                scaffold = SheetScaffoldUiModel(
                    header = SheetHeaderUiModel(
                        section = SectionHeaderUiModel.Section(
                            eyebrow = "Piranesi",
                            headline = "How it felt",
                            description = "Tap a mood to add it. Slide to say how strongly, or leave it and it " +
                                "simply counts as there.",
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
                ),
            )
        }

        override val previews: ImmutableMap<String, HowItFeltSheetUiModel> = persistentMapOf(
            "Empty draft" to draft(selected = emptyMap()),
            "Filled draft" to draft(
                selected = mapOf(
                    "tense" to 80,
                    "hopeful" to 55,
                    "funny" to 20,
                ),
            ),
            "Strength unset, own word open" to draft(
                selected = mapOf("sad" to null),
                ownWordOpen = true,
            ),
        )
    }
}
