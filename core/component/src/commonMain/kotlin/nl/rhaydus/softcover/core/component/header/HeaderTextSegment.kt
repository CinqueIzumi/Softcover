package nl.rhaydus.softcover.core.component.header

import androidx.compose.runtime.Immutable

/**
 * One run of a [SectionHeaderUiModel.Section] headline or description. The builder that interpolates
 * the copy decides which runs are emphasised; the component never searches the text for them.
 */
@Immutable
sealed interface HeaderTextSegment {
    val text: String

    data class Plain(override val text: String) : HeaderTextSegment

    data class Emphasised(override val text: String) : HeaderTextSegment
}
