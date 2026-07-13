package nl.rhaydus.softcover.core.connectivity.di

import org.koin.dsl.binds
import org.koin.dsl.module
import nl.rhaydus.platform.JvmNetworkAvailabilityProvider
import nl.rhaydus.platform.NetworkAvailabilityProvider

actual val platformModule = module {
    // Also bound as AutoCloseable so the desktop shutdown teardown can stop its reachability poll loop
    // without orchestration naming the concrete provider.
    single { JvmNetworkAvailabilityProvider(dispatchers = get()) } binds
        arrayOf(NetworkAvailabilityProvider::class, AutoCloseable::class)
}
