package nl.rhaydus.softcover.core.component.chip

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Everything the pill-shaped chip renders: a label on a fully-rounded surface, optionally selected,
 * concealed, or inert.
 *
 * @property key Identity — the event carries it back (`component-contract.md` § 7.2 R1), and the
 * mapper's lookup map (kept beside the models in `UiState`, not rebuilt in composition) resolves it
 * back to whatever payload dispatching the original action needs (a filter value, a tag, a category).
 * @property label The chip's text.
 * @property selected Swaps the chip to its selected treatment (`secondaryContainer`).
 * @property concealed Renders the chip as a spoiler redaction: the label draws transparent so it
 * reserves its width but cannot be read, beneath a solid cover fill — the same treatment
 * `RichText` gives an inline spoiler run. Because the label still measures at its real
 * width, revealing it (re-render with `concealed = false`) does not reflow the row.
 * @property clickable Whether the chip is interactive. `false` renders the read-only chip (e.g. the
 * book-detail community/user tag chips, which carry no click role or ripple).
 */
@Immutable
data class ChipUiModel(
    val key: String,
    val label: String,
    val selected: Boolean = false,
    val concealed: Boolean = false,
    val clickable: Boolean = true,
) {
    companion object : UiModelPreviews<ChipUiModel> {
        /**
         * Per R5, one fixture per anatomy branch: idle, selected, concealed (spoiler redaction),
         * read-only (not clickable), and a label long enough to exercise ellipsis.
         */
        override val previews: ImmutableList<ChipUiModel> = persistentListOf(
            ChipUiModel(
                key = "idle",
                label = "Fantasy",
            ),
            ChipUiModel(
                key = "selected",
                label = "Currently reading",
                selected = true,
            ),
            ChipUiModel(
                key = "concealed",
                label = "Contains a major death",
                concealed = true,
            ),
            ChipUiModel(
                key = "read-only",
                label = "Cozy mystery",
                clickable = false,
            ),
            ChipUiModel(
                key = "long-label",
                label = "A Chip Label Long Enough That It Must Ellipsise Somewhere",
            ),
        )
    }
}
