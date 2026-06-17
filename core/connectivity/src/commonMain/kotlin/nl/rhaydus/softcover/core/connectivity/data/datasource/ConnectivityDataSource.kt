package nl.rhaydus.softcover.core.connectivity.data.datasource

import kotlinx.coroutines.flow.StateFlow

internal interface ConnectivityDataSource {
    val isOnline: StateFlow<Boolean>
}
