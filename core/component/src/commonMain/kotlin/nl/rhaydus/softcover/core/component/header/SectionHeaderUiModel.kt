package nl.rhaydus.softcover.core.component.header

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Everything [SectionHeader] renders — one of three accent-bar registers, from heaviest to lightest:
 * [Section] opens a region with a rounded bar and an optional headline/description, [Inline] marks a
 * smaller region with a hairline bar and no headline, and [Label] carries no bar at all.
 *
 * @property isLoading Masks the header's text in place; the model's own text sizes the mask.
 */
@Immutable
sealed interface SectionHeaderUiModel {
    val isLoading: Boolean

    /**
     * [headline] and [description] are segment runs so a builder can emphasise an interpolated span;
     * the secondary constructor takes plain strings for the common case of no emphasis.
     */
    data class Section(
        val eyebrow: String,
        val headline: ImmutableList<HeaderTextSegment>?,
        val description: ImmutableList<HeaderTextSegment>?,
        override val isLoading: Boolean = false,
    ) : SectionHeaderUiModel {
        /** [headline] with its emphasis dropped, for surfaces that need the words and not the styling. */
        val headlineText: String? get() = headline?.joinToString(separator = "") { it.text }

        constructor(
            eyebrow: String,
            headline: String? = null,
            description: String? = null,
            isLoading: Boolean = false,
        ) : this(
            eyebrow = eyebrow,
            headline = headline?.toPlainSegments(),
            description = description?.toPlainSegments(),
            isLoading = isLoading,
        )
    }

    data class Inline(
        val eyebrow: String,
        val tone: SectionHeaderTone = SectionHeaderTone.Accent,
        override val isLoading: Boolean = false,
    ) : SectionHeaderUiModel

    data class Label(
        val eyebrow: String,
        val tone: SectionHeaderTone = SectionHeaderTone.Muted,
        override val isLoading: Boolean = false,
    ) : SectionHeaderUiModel

    companion object : UiModelPreviews<SectionHeaderUiModel> {
        /** One fixture per register, plus [Section] with and without headline/description. */
        override val previews: ImmutableMap<String, SectionHeaderUiModel> = persistentMapOf(
            "Section, with headline" to Section(
                eyebrow = "Your progress",
                headline = "Halfway through",
                description = "You're on pace to finish by the deadline.",
            ),
            "Section, eyebrow only" to Section(eyebrow = "Currently reading"),
            "Section, with emphasis" to Section(
                eyebrow = "Lists",
                headline = persistentListOf(
                    HeaderTextSegment.Plain(text = "Shelve "),
                    HeaderTextSegment.Emphasised(text = "Piranesi"),
                ),
                description = null,
            ),
            "Inline" to Inline(eyebrow = "Recently added"),
            "Inline, error" to Inline(
                eyebrow = "Couldn't save — tap to retry",
                tone = SectionHeaderTone.Error,
            ),
            "Label" to Label(eyebrow = "Shelves"),
            "Label, accent" to Label(
                eyebrow = "Your tabs",
                tone = SectionHeaderTone.Accent,
            ),
        )
    }
}

private fun String.toPlainSegments(): ImmutableList<HeaderTextSegment> =
    persistentListOf(HeaderTextSegment.Plain(text = this))
