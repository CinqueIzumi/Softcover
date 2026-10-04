package nl.rhaydus.softcover.feature.session.presentation.mapper

import nl.rhaydus.softcover.core.component.progress.ProgressBarUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.session.presentation.state.FocusPageEditorUiModel
import kotlin.math.roundToInt

internal fun Book.pageCount(): Int = currentEdition?.pages ?: defaultEdition?.pages ?: 0

internal fun Book.toFocusPageEditor(draft: String): FocusPageEditorUiModel {
    val totalPages = pageCount()

    if (totalPages <= 0) {
        val percent = userBookRead?.progress ?: 0f

        return FocusPageEditorUiModel(
            caption = "${percent.roundToInt()}% complete",
            bar = ProgressBarUiModel(
                fraction = (percent / 100f).coerceIn(
                    minimumValue = 0f,
                    maximumValue = 1f,
                ),
            ),
            isEditable = false,
        )
    }

    val page = draft.toIntOrNull() ?: 0

    return FocusPageEditorUiModel(
        caption = "of $totalPages pages",
        bar = ProgressBarUiModel(
            fraction = (page.toFloat() / totalPages).coerceIn(
                minimumValue = 0f,
                maximumValue = 1f,
            ),
        ),
        isEditable = true,
    )
}
