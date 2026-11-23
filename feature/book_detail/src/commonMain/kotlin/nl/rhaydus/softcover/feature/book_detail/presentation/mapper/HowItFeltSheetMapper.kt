package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.feeling.HowItFeltSheetUiModel
import nl.rhaydus.softcover.core.component.feeling.MoodStrengthUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterEmphasis
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.domain.model.Mood
import nl.rhaydus.softcover.core.uibinding.mood.moodChoices
import nl.rhaydus.softcover.core.uibinding.mood.toChipKey
import nl.rhaydus.softcover.core.uibinding.mood.toLabel
import nl.rhaydus.softcover.feature.book_detail.presentation.state.HowItFeltDraft

internal fun HowItFeltDraft.toHowItFeltSheet(
    bookTitle: String,
    ownWords: List<String>,
): HowItFeltSheetUiModel = HowItFeltSheetUiModel(
    choices = moodChoices(
        presets = Mood.HowItFelt,
        selected = moods.map { it.word },
        ownWordsInUse = ownWords,
        ownWordOpen = ownWordOpen,
        ownWordText = ownWordText,
    ),
    strengths = moods
        .map { mood ->
            MoodStrengthUiModel(
                key = mood.word.toChipKey(),
                label = mood.word.toLabel(),
                strength = mood.strength,
                unsetLabel = "Not set",
            )
        }
        .toImmutableList(),
    caption = "Moods along the way are added when you update your progress.",
    scaffold = SheetScaffoldUiModel(
        header = SheetHeaderUiModel(
            section = SectionHeaderUiModel.Section(
                eyebrow = bookTitle,
                headline = "How it felt",
                description = "Tap a mood to add it. Slide to say how strongly, or leave it and it simply counts " +
                    "as there.",
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
