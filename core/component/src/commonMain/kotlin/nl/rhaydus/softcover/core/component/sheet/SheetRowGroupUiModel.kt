package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldVariant
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * A row selection runs the sheet's animated dismiss after [SheetRowEvent.Selected], so the host's
 * handler must not close the sheet itself. When [dismissesSheet] is false the group leaves the
 * sheet open and the host closes it through [SheetSession.requestClose], for a host that
 * must apply the selection before the hide starts. Edits to [input] never dismiss the sheet.
 */
@Immutable
data class SheetRowGroupUiModel<K>(
    val rows: ImmutableList<SheetRowUiModel<K>>,
    val dismissesSheet: Boolean = true,
    val input: SheetInputRowUiModel? = null,
    val face: SheetRowFace = SheetRowFace.Editorial,
) {
    companion object : UiModelPreviews<SheetRowGroupUiModel<String>> {
        override val previews: ImmutableMap<String, SheetRowGroupUiModel<String>> = persistentMapOf(
            "With input row" to SheetRowGroupUiModel(
                rows = persistentListOf(
                    SheetRowUiModel(
                        key = "bookshop",
                        label = "Bought in a bookshop",
                        selected = false,
                        gloss = "A physical copy, in person.",
                    ),
                    SheetRowUiModel(
                        key = "library",
                        label = "Borrowed from a library",
                        selected = false,
                    ),
                ),
                dismissesSheet = false,
                input = SheetInputRowUiModel(
                    field = TextFieldUiModel(
                        key = "somewhere-else",
                        text = "A friend's bookshelf",
                        placeholder = "Somewhere else…",
                        variant = TextFieldVariant.Bare,
                    ),
                    selected = true,
                ),
            ),
            "Body face with input row" to SheetRowGroupUiModel(
                rows = persistentListOf(
                    SheetRowUiModel(
                        key = "bookshop",
                        label = "Bought in a bookshop",
                        selected = true,
                        gloss = "A physical copy, in person.",
                    ),
                    SheetRowUiModel(
                        key = "library",
                        label = "Borrowed from a library",
                        selected = false,
                    ),
                ),
                dismissesSheet = false,
                input = SheetInputRowUiModel(
                    field = TextFieldUiModel(
                        key = "somewhere-else",
                        text = "",
                        placeholder = "Somewhere else…",
                        variant = TextFieldVariant.Plain,
                    ),
                    selected = false,
                ),
                face = SheetRowFace.Body,
            ),
            "Single choice" to SheetRowGroupUiModel(
                rows = persistentListOf(
                    SheetRowUiModel(
                        key = "title",
                        label = "Title",
                        selected = false,
                    ),
                    SheetRowUiModel(
                        key = "author",
                        label = "Author",
                        selected = true,
                    ),
                    SheetRowUiModel(
                        key = "date-added",
                        label = "Date added",
                        selected = false,
                    ),
                ),
            ),
        )
    }
}
