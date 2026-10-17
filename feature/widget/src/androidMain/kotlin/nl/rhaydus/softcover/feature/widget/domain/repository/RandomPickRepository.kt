package nl.rhaydus.softcover.feature.widget.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickResolution
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickSettings
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickSettingsChange

internal interface RandomPickRepository {
    /** Emits the id of the instance whose pick or settings changed. */
    val changes: Flow<Int>

    /**
     * Emits only when a save actually changed the settings, ahead of the matching [changes] emission, so a widget
     * can show a loading state while the new settings resolve.
     */
    val settingsChanges: Flow<RandomPickSettingsChange>

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
