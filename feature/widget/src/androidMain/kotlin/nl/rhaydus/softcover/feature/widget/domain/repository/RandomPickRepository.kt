package nl.rhaydus.softcover.feature.widget.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickResolution
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickSettings

internal interface RandomPickRepository {
    /** Emits the id of the instance whose pick or settings changed. */
    val changes: Flow<Int>

    suspend fun getSettings(appWidgetId: Int): RandomPickSettings

    suspend fun saveSettings(
        appWidgetId: Int,
        settings: RandomPickSettings,
    )

    suspend fun resolve(
        appWidgetId: Int,
        wantToRead: List<Book>,
        lists: List<BookList>,
    ): RandomPickResolution

    suspend fun shuffle(
        appWidgetId: Int,
        wantToRead: List<Book>,
        lists: List<BookList>,
    )
}
