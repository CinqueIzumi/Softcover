package nl.rhaydus.softcover.core.component.control

import androidx.compose.runtime.Immutable

@Immutable
sealed interface SegmentedControlVariant {
    /** A full-width pill track; the selected segment fills with the primary colour. */
    data object Track : SegmentedControlVariant

    /** The options as words in a sentence, "[prefix]option / option[suffix]"; the selected word is underlined. */
    data class Prose(
        val prefix: String,
        val suffix: String,
    ) : SegmentedControlVariant
}
