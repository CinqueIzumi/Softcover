package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

@Immutable
sealed interface SheetFooterUiModel {
    val primary: SheetFooterButton?
    val secondary: SheetFooterButton?

    /** Primary full width, secondary beneath it; either may be absent but not both. */
    data class Stacked(
        override val primary: SheetFooterButton?,
        override val secondary: SheetFooterButton?,
    ) : SheetFooterUiModel {
        init {
            require(primary != null || secondary != null) { "A stacked footer needs at least one button" }
        }
    }

    /** Secondary on the left, primary filling the rest of the row. */
    data class Inline(
        override val primary: SheetFooterButton,
        override val secondary: SheetFooterButton,
    ) : SheetFooterUiModel

    companion object : UiModelPreviews<SheetFooterUiModel> {
        override val previews: ImmutableList<SheetFooterUiModel> = persistentListOf(
            Stacked(
                primary = SheetFooterButton(label = "Save"),
                secondary = SheetFooterButton(label = "Not now"),
            ),
            Stacked(
                primary = SheetFooterButton(label = "Save"),
                secondary = null,
            ),
            Inline(
                primary = SheetFooterButton(label = "Apply filters"),
                secondary = SheetFooterButton(label = "Clear"),
            ),
            Inline(
                primary = SheetFooterButton(
                    label = "Apply filters",
                    enabled = false,
                ),
                secondary = SheetFooterButton(label = "Clear"),
            ),
        )
    }
}
