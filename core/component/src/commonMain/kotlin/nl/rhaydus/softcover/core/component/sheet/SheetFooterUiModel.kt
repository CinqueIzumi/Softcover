package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

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
        override val previews: ImmutableMap<String, SheetFooterUiModel> = persistentMapOf(
            "Stacked" to Stacked(
                primary = SheetFooterButton(label = "Save"),
                secondary = SheetFooterButton(label = "Not now"),
            ),
            "Stacked, no secondary" to Stacked(
                primary = SheetFooterButton(label = "Save"),
                secondary = null,
            ),
            "Stacked, outlined secondary with icon" to Stacked(
                primary = SheetFooterButton(label = "Update progress"),
                secondary = SheetFooterButton(
                    label = "Mark as read",
                    emphasis = SheetFooterEmphasis.Outlined,
                    icon = SoftcoverIcon.Check,
                ),
            ),
            "Inline" to Inline(
                primary = SheetFooterButton(label = "Apply filters"),
                secondary = SheetFooterButton(label = "Clear"),
            ),
            "Inline, primary disabled" to Inline(
                primary = SheetFooterButton(
                    label = "Apply filters",
                    enabled = false,
                ),
                secondary = SheetFooterButton(label = "Clear"),
            ),
            "Inline, secondary running" to Inline(
                primary = SheetFooterButton(label = "Share"),
                secondary = SheetFooterButton(
                    label = "Save to gallery",
                    running = true,
                ),
            ),
        )
    }
}
