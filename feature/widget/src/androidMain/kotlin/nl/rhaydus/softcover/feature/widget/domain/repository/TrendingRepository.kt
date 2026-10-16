package nl.rhaydus.softcover.feature.widget.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingPosition
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingSettings

internal interface TrendingRepository {
    /** Emits the id of the instance whose page or settings changed. */
    val changes: Flow<Int>

    suspend fun getSettings(appWidgetId: Int): TrendingSettings

    suspend fun getPosition(appWidgetId: Int): TrendingPosition

    suspend fun saveSettings(
        appWidgetId: Int,
        settings: TrendingSettings,
    )

    suspend fun advancePage(
        appWidgetId: Int,
        pageCount: Int,
        step: Int,
    )
}
