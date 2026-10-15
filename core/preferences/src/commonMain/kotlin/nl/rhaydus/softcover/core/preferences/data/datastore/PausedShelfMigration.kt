package nl.rhaydus.softcover.core.preferences.data.datastore

import androidx.datastore.core.DataMigration
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.preferences.data.model.AppSettingsEntity

private val PAUSED_STATUS_CODE = UserBookStatus.PAUSED.code
private val PAUSED_TAB_KEY = "status-$PAUSED_STATUS_CODE"
private val CURRENTLY_READING_TAB_KEY = "status-${UserBookStatus.CURRENTLY_READING.code}"

internal object PausedShelfMigration : DataMigration<AppSettingsEntity> {
    override suspend fun shouldMigrate(currentData: AppSettingsEntity): Boolean =
        currentData.pausedShelfSeeded.not()

    override suspend fun migrate(currentData: AppSettingsEntity): AppSettingsEntity {
        val order = currentData.libraryTabOrder
        val anchor = order.indexOf(CURRENTLY_READING_TAB_KEY)
        val migratedOrder = when {
            order.isEmpty() || PAUSED_TAB_KEY in order -> order
            anchor >= 0 -> order.take(anchor + 1) + PAUSED_TAB_KEY + order.drop(anchor + 1)
            else -> order + PAUSED_TAB_KEY
        }

        return currentData.copy(
            enabledStatusCodes = currentData.enabledStatusCodes + PAUSED_STATUS_CODE,
            libraryTabOrder = migratedOrder,
            pausedShelfSeeded = true,
        )
    }

    override suspend fun cleanUp() = Unit
}
