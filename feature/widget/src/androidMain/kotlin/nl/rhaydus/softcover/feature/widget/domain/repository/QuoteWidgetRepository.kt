package nl.rhaydus.softcover.feature.widget.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.widget.domain.model.QuoteSelection

internal interface QuoteWidgetRepository {
    /** Emits the id of the instance whose selected quote changed. */
    val selectionChanges: Flow<Int>

    suspend fun getSelection(appWidgetId: Int): QuoteSelection?

    suspend fun saveSelection(
        appWidgetId: Int,
        selection: QuoteSelection,
    )
}
