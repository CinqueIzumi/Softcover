package nl.rhaydus.softcover.core.component.feeling

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.button.PillButtonContent
import nl.rhaydus.softcover.core.component.button.PillButtonTone
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
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

/**
 * Everything [MoodMomentSheet] renders: one mood at one point in the book. The footer labels in
 * [scaffold] tell adding (Skip, Add mood) from editing (Delete, Save).
 */
@Immutable
data class MoodMomentSheetUiModel(
    val choices: MoodChoicesUiModel,
    val strength: MoodStrengthUiModel,
    val scaffold: SheetScaffoldUiModel,
) : SheetModel<MoodMomentSheetUiModel> {
    override val closeRequested: Boolean get() = scaffold.closeRequested

    override fun requestingClose(): MoodMomentSheetUiModel = copy(scaffold = scaffold.requestingClose())

    companion object : UiModelPreviews<MoodMomentSheetUiModel> {
        private fun moment(
            selected: String?,
            strength: Int?,
            editing: Boolean,
        ): MoodMomentSheetUiModel {
            val moods = listOf(
                "curious" to "Curious",
                "tense" to "Tense",
                "wrecked" to "Wrecked",
                "hopeful" to "Hopeful",
                "bored" to "Bored",
                "gripped" to "Gripped",
            )

            return MoodMomentSheetUiModel(
                choices = MoodChoicesUiModel(
                    chips = (
                        moods.map { (key, label) ->
                            ChipUiModel(
                                key = key,
                                label = label,
                                selected = key == selected,
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
                        text = "Wistful",
                        placeholder = "A word of your own",
                        variant = TextFieldVariant.Pill(
                            leadingIcon = SoftcoverIcon.Edit,
                            trailing = PillButtonUiModel(
                                key = "add-own-word",
                                content = PillButtonContent.Label(text = "Add"),
                                tone = PillButtonTone.Filled,
                            ),
                        ),
                        submittable = true,
                    ),
                    ownWordOpen = true,
                ),
                strength = MoodStrengthUiModel(
                    key = "strength",
                    label = "How strongly",
                    strength = strength,
                    unsetLabel = "Not set",
                ),
                scaffold = SheetScaffoldUiModel(
                    header = SheetHeaderUiModel(
                        section = SectionHeaderUiModel.Section(
                            eyebrow = "Page 286 · 62%",
                            headline = "How is it feeling?",
                            description = "Progress saved. A mood here marks this point along your progress bar.",
                        ),
                    ),
                    footer = SheetFooterUiModel.Inline(
                        primary = SheetFooterButton(
                            label = if (editing) "Save" else "Add mood",
                            dismissesSheet = true,
                        ),
                        secondary = SheetFooterButton(
                            label = if (editing) "Delete" else "Skip",
                            emphasis = SheetFooterEmphasis.Outlined,
                            dismissesSheet = true,
                        ),
                    ),
                ),
            )
        }

        override val previews: ImmutableMap<String, MoodMomentSheetUiModel> = persistentMapOf(
            "Adding" to moment(
                selected = "wrecked",
                strength = 90,
                editing = false,
            ),
            "Adding, nothing chosen" to moment(
                selected = null,
                strength = null,
                editing = false,
            ),
            "Editing" to moment(
                selected = "tense",
                strength = 70,
                editing = true,
            ),
        )
    }
}
