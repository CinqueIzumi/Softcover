package nl.rhaydus.softcover.core.component.header

import androidx.compose.runtime.Immutable

/**
 * A span of a [SectionHeaderUiModel.Section] headline or description to draw with [treatment].
 *
 * [substring] is matched at its last occurrence, because the emphasised span is normally the
 * interpolated tail of the copy ("Shelve {title}"); a substring that does not occur emphasises nothing.
 */
@Immutable
data class TextEmphasis(
    val substring: String,
    val treatment: EmphasisTreatment,
)
