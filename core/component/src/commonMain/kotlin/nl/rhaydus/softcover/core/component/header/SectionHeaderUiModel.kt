package nl.rhaydus.softcover.core.component.header

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Everything [SectionHeader] renders — one of three accent-bar registers, from heaviest to lightest:
 * [Section] opens a region with a rounded bar and an optional headline/description, [Inline] marks a
 * smaller region with a hairline bar and no headline, and [Label] carries no bar at all.
 */
@Immutable
sealed interface SectionHeaderUiModel {
    /**
     * @property pulseKey Bumped to replay the bar's widen-and-settle pulse; `0` plays nothing, so a
     * header that never needs the pulse can leave it at the default.
     * @property tone Colours the bar and eyebrow only; the headline and description stay onSurface /
     * onSurfaceVariant regardless.
     */
    data class Section(
        val eyebrow: String,
        val headline: String? = null,
        val description: String? = null,
        val headlineEmphasis: TextEmphasis? = null,
        val descriptionEmphasis: TextEmphasis? = null,
        val pulseKey: Int = 0,
        val tone: SectionHeaderTone = SectionHeaderTone.Accent,
    ) : SectionHeaderUiModel

    data class Inline(
        val eyebrow: String,
        val tone: SectionHeaderTone = SectionHeaderTone.Accent,
    ) : SectionHeaderUiModel

    data class Label(
        val eyebrow: String,
        val tone: SectionHeaderTone = SectionHeaderTone.Muted,
    ) : SectionHeaderUiModel

    companion object : UiModelPreviews<SectionHeaderUiModel> {
        /** One fixture per register, plus [Section] with and without headline/description. */
        override val previews: ImmutableList<SectionHeaderUiModel> = persistentListOf(
            Section(
                eyebrow = "Your progress",
                headline = "Halfway through",
                description = "You're on pace to finish by the deadline.",
            ),
            Section(eyebrow = "Currently reading"),
            Section(
                eyebrow = "Lists",
                headline = "Shelve Piranesi",
                headlineEmphasis = TextEmphasis(
                    substring = "Piranesi",
                    treatment = EmphasisTreatment.Accent,
                ),
            ),
            Section(
                eyebrow = "Couldn't sync — tap to retry",
                tone = SectionHeaderTone.Error,
            ),
            Inline(eyebrow = "Recently added"),
            Inline(
                eyebrow = "Couldn't save — tap to retry",
                tone = SectionHeaderTone.Error,
            ),
            Label(eyebrow = "Shelves"),
            Label(
                eyebrow = "Your tabs",
                tone = SectionHeaderTone.Accent,
            ),
        )
    }
}
