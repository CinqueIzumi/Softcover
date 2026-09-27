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
     */
    data class Section(
        val eyebrow: String,
        val headline: String? = null,
        val description: String? = null,
        val pulseKey: Int = 0,
    ) : SectionHeaderUiModel

    data class Inline(val eyebrow: String) : SectionHeaderUiModel

    data class Label(val eyebrow: String) : SectionHeaderUiModel

    companion object : UiModelPreviews<SectionHeaderUiModel> {
        /** One fixture per register, plus [Section] with and without headline/description. */
        override val previews: ImmutableList<SectionHeaderUiModel> = persistentListOf(
            Section(
                eyebrow = "Your progress",
                headline = "Halfway through",
                description = "You're on pace to finish by the deadline.",
            ),
            Section(eyebrow = "Currently reading"),
            Inline(eyebrow = "Recently added"),
            Label(eyebrow = "Shelves"),
        )
    }
}
