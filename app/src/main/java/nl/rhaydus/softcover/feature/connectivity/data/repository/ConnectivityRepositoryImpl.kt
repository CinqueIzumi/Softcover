package nl.rhaydus.softcover.feature.connectivity.data.repository

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import nl.rhaydus.softcover.core.domain.connectivity.NetworkAvailabilityProvider
import nl.rhaydus.softcover.feature.connectivity.data.datasource.ConnectivityDataSource

class ConnectivityRepositoryImpl(
    private val dataSource: ConnectivityDataSource,
) : NetworkAvailabilityProvider {
    override val isOnline: StateFlow<Boolean> = dataSource.isOnline

    override suspend fun awaitOnline() {
        isOnline.first { it }
    }
}
