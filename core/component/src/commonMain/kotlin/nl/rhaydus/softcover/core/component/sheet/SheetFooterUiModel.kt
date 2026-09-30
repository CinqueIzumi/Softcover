package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

@Immutable
data class SheetFooterUiModel(
    val primary: SheetFooterButton?,
    val secondary: SheetFooterButton?,
    val layout: SheetFooterLayout,
) {
    companion object : UiModelPreviews<SheetFooterUiModel> {
        override val previews: ImmutableList<SheetFooterUiModel> = persistentListOf(
            SheetFooterUiModel(
                primary = SheetFooterButton(label = "Save"),
                secondary = SheetFooterButton(label = "Not now"),
                layout = SheetFooterLayout.Stacked,
            ),
            SheetFooterUiModel(
                primary = SheetFooterButton(
                    label = "Save",
                    loading = true,
                ),
                secondary = null,
                layout = SheetFooterLayout.Stacked,
            ),
            SheetFooterUiModel(
                primary = SheetFooterButton(label = "Apply filters"),
                secondary = SheetFooterButton(label = "Clear"),
                layout = SheetFooterLayout.Inline,
            ),
            SheetFooterUiModel(
                primary = SheetFooterButton(
                    label = "Apply filters",
                    enabled = false,
                ),
                secondary = SheetFooterButton(label = "Clear"),
                layout = SheetFooterLayout.Inline,
            ),
        )
    }
}
