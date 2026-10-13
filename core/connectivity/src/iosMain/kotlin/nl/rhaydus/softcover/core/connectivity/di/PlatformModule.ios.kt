package nl.rhaydus.softcover.core.connectivity.di

import nl.rhaydus.platform.IosNetworkAvailabilityProvider
import nl.rhaydus.platform.NetworkAvailabilityProvider
import org.koin.dsl.module

actual val platformModule = module {
    single<NetworkAvailabilityProvider> {
        IosNetworkAvailabilityProvider()
    }
}
