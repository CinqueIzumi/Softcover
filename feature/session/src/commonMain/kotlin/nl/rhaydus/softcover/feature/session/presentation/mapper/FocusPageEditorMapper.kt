package nl.rhaydus.softcover.feature.session.presentation.mapper

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.session.presentation.state.FocusPageEditorUiModel
import kotlin.math.roundToInt

internal fun Book.pageCount(): Int = currentEdition?.pages ?: defaultEdition?.pages ?: 0

internal fun Book.toFocusPageEditor(draft: String): FocusPageEditorUiModel {
    val totalPages = pageCount()

    if (totalPages <= 0) {
        val percent = currentRead?.progress ?: 0f

        return FocusPageEditorUiModel(
            caption = "${percent.roundToInt()}% complete",
            fraction = percent / 100f,
            isEditable = false,
        )
    }

    val page = draft.toIntOrNull() ?: 0

    return FocusPageEditorUiModel(
        caption = "of $totalPages pages",
        fraction = page.toFloat() / totalPages,
        isEditable = true,
    )
}
