package nl.rhaydus.softcover.core.corpus.data.mapper

import nl.rhaydus.softcover.core.domain.model.Mood
import nl.rhaydus.softcover.core.domain.model.MoodWord

internal const val MAX_MOOD_STRENGTH = 100

internal fun Int?.clampedStrength(): Int? = this?.coerceIn(
    0,
    MAX_MOOD_STRENGTH,
)

internal fun MoodWord.cleaned(): MoodWord? = when (this) {
    is MoodWord.Preset -> this
    is MoodWord.Own -> text.trim().takeIf { it.isNotEmpty() }?.let { MoodWord.Own(text = it) }
}

internal fun MoodWord.isAllowedIn(presets: List<Mood>): Boolean = when (this) {
    is MoodWord.Preset -> mood in presets
    is MoodWord.Own -> true
}
