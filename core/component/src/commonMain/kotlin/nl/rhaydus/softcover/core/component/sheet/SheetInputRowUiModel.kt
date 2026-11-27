package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldVariant
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * A free-text choice after a [SheetRowGroupUiModel]'s rows. Typing never selects it: the host sets [selected]
 * and unticks the other rows itself.
 */
@Immutable
data class SheetInputRowUiModel(
    val field: TextFieldUiModel,
    val selected: Boolean,
) {
    companion object : UiModelPreviews<SheetInputRowUiModel> {
        override val previews: ImmutableMap<String, SheetInputRowUiModel> = persistentMapOf(
            "Empty" to SheetInputRowUiModel(
                field = TextFieldUiModel(
                    key = "empty",
                    text = "",
                    placeholder = "Somewhere else…",
                    variant = TextFieldVariant.Bare,
                ),
                selected = false,
            ),
            "Typed and selected" to SheetInputRowUiModel(
                field = TextFieldUiModel(
                    key = "typed",
                    text = "A friend's bookshelf",
                    placeholder = "Somewhere else…",
                    variant = TextFieldVariant.Bare,
                ),
                selected = true,
            ),
        )
    }
}
