package nl.rhaydus.softcover.feature.widget.domain.repository

import kotlinx.coroutines.flow.Flow

internal interface CurrentlyReadingRepository {
    /** Emits the id of the instance whose selected book changed. */
    val selectionChanges: Flow<Int>

    suspend fun getSelectedBookId(appWidgetId: Int): Int?

    suspend fun cycleSelectedBook(
        appWidgetId: Int,
        orderedBookIds: List<Int>,
        step: Int,
    )
}
