package nl.rhaydus.softcover.feature.explore.presentation.util

/**
 * Presentation-only title-casing for a mood label (explore-3a feedback item 3): the API returns
 * moods lowercase ("adventurous", "cosy & comforting"). The domain model stays untouched — only the
 * render layer capitalizes each whitespace-separated word before it reaches a `Text`.
 */
internal fun String.toTitleCaseWords(): String =
    split(' ').joinToString(" ") { word -> word.replaceFirstChar { it.titlecase() } }
