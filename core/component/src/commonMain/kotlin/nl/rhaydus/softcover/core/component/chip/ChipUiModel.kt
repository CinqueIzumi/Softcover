package nl.rhaydus.softcover.core.component.chip

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/**
 * Everything [Chip] renders: a label on a fully-rounded surface, shaped by [variant] and gated by
 * [interaction] (`component-contract.md` § 7.2 R2).
 *
 * @property key Identity — the event carries it back (R1), and the mapper's lookup map (kept beside
 * the models in `UiState`, not rebuilt in composition) resolves it back to whatever payload
 * dispatching the original action needs (a filter value, a tag, a category).
 * @property leadingIcon Rendered at 18dp with an 8dp gap before the label, on every variant except
 * [ChipVariant.Remove].
 * @property dismissLabel Non-null renders a trailing ✕ as its own tap target reporting
 * [ChipEvent.Dismissed], using this string as its content description. [ChipVariant.Remove] ignores
 * it — its own ✕ is display-only.
 */
@Immutable
data class ChipUiModel(
    val key: String,
    val label: String,
    val variant: ChipVariant = ChipVariant.Tonal(),
    val interaction: ChipInteraction = ChipInteraction.Clickable,
    val leadingIcon: SoftcoverIcon? = null,
    val dismissLabel: String? = null,
) {
    companion object : UiModelPreviews<ChipUiModel> {
        /**
         * Per R5, one fixture per [ChipVariant] branch, plus the anatomy-changing cases a leading
         * icon, a dismissible chip, [ChipInteraction.Disabled] and [ChipInteraction.Inert] add, and
         * a label long enough to exercise ellipsis.
         */
        override val previews: ImmutableList<ChipUiModel> = persistentListOf(
            ChipUiModel(
                key = "tonal-idle",
                label = "Fantasy",
            ),
            ChipUiModel(
                key = "tonal-selected",
                label = "Currently reading",
                variant = ChipVariant.Tonal(selected = true),
            ),
            ChipUiModel(
                key = "spoiler",
                label = "Contains a major death",
                variant = ChipVariant.Spoiler,
            ),
            ChipUiModel(
                key = "add",
                label = "Winter reading",
                variant = ChipVariant.Add,
            ),
            ChipUiModel(
                key = "add-outlined",
                label = "Second helpings",
                variant = ChipVariant.AddOutlined,
            ),
            ChipUiModel(
                key = "remove",
                label = "On the list",
                variant = ChipVariant.Remove,
            ),
            ChipUiModel(
                key = "format",
                label = "Bold",
                variant = ChipVariant.Format(
                    active = true,
                    face = ChipFace.Bold,
                ),
            ),
            ChipUiModel(
                key = "leading-icon",
                label = "Just now",
                leadingIcon = SoftcoverIcon.DateRange,
            ),
            ChipUiModel(
                key = "dismissible",
                label = "Yesterday, 14:30",
                variant = ChipVariant.Tonal(selected = true),
                leadingIcon = SoftcoverIcon.DateRange,
                dismissLabel = "Reset to just now",
            ),
            ChipUiModel(
                key = "disabled",
                label = "Cozy mystery",
                interaction = ChipInteraction.Disabled,
            ),
            ChipUiModel(
                key = "inert",
                label = "Cozy mystery",
                interaction = ChipInteraction.Inert,
            ),
            ChipUiModel(
                key = "long-label",
                label = "A Chip Label Long Enough That It Must Ellipsise Somewhere",
            ),
        )
    }
}
