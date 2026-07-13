package nl.rhaydus.softcover.core.connectivity.di

import org.koin.dsl.module
import nl.rhaydus.platform.IosNetworkAvailabilityProvider
import nl.rhaydus.platform.NetworkAvailabilityProvider

actual val platformModule = module {
    single<NetworkAvailabilityProvider> {
        IosNetworkAvailabilityProvider()
    }
}
