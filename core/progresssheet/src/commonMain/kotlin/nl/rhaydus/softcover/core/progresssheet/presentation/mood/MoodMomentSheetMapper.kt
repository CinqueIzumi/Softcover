package nl.rhaydus.softcover.core.progresssheet.presentation.mood

import nl.rhaydus.softcover.core.component.feeling.MoodMomentSheetUiModel
import nl.rhaydus.softcover.core.component.feeling.MoodStrengthUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterEmphasis
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.domain.model.Mood
import nl.rhaydus.softcover.core.uibinding.mood.moodChoices
import nl.rhaydus.softcover.core.uibinding.mood.moodMomentEyebrow

private const val MARKS_THIS_POINT = "A mood here marks this point along your progress bar."

internal fun MoodMomentDraft.toMoodMomentSheet(ownWords: List<String>): MoodMomentSheetUiModel {
    val editing = editing != null

    return MoodMomentSheetUiModel(
        choices = moodChoices(
            presets = Mood.Moment,
            selected = listOfNotNull(word),
            ownWordsInUse = ownWords,
            ownWordOpen = ownWordOpen,
            ownWordText = ownWordText,
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
                    eyebrow = moodMomentEyebrow(
                        page = page,
                        seconds = seconds,
                        percent = percent,
                    ),
                    headline = "How is it feeling?",
                    description = if (editing) MARKS_THIS_POINT else "Progress saved. $MARKS_THIS_POINT",
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
