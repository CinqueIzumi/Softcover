package nl.rhaydus.softcover.feature.settings.presentation.mapper

import nl.rhaydus.softcover.core.component.control.ToggleRowUiModel
import nl.rhaydus.softcover.core.component.control.ToggleRowVariant
import nl.rhaydus.softcover.feature.settings.presentation.model.MoodPromptToggle

internal fun moodPromptToggleRowFor(askForMoodChecked: Boolean): ToggleRowUiModel<MoodPromptToggle> = ToggleRowUiModel(
    key = MoodPromptToggle.ASK_FOR_MOOD_AFTER_PROGRESS,
    title = "Moods along the way",
    gloss = "Ask how a book feels after you update your progress",
    checked = askForMoodChecked,
    variant = ToggleRowVariant.Inset,
)
