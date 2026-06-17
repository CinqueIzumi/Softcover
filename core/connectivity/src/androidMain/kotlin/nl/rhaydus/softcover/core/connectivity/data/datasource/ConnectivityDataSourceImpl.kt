package nl.rhaydus.softcover.core.connectivity.data.datasource

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.core.content.getSystemService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class ConnectivityDataSourceImpl(
    context: Context,
) : ConnectivityDataSource {
    private val connectivityManager: ConnectivityManager? = context.getSystemService()

    private val _isOnline = MutableStateFlow(initialOnlineState())

    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    init {
        connectivityManager?.registerDefaultNetworkCallback(
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    val caps = connectivityManager.getNetworkCapabilities(network)
                    _isOnline.value = caps.isUsableInternet()
                }

                override fun onLost(network: Network) {
                    _isOnline.value = false
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    capabilities: NetworkCapabilities,
                ) {
                    _isOnline.value = capabilities.isUsableInternet()
                }
            },
        )
    }

    private fun initialOnlineState(): Boolean {
        val active = connectivityManager?.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(active)

        return caps.isUsableInternet()
    }

    private fun NetworkCapabilities?.isUsableInternet(): Boolean {
        if (this == null) return false

        return hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
