package nl.rhaydus.softcover.core.connectivity.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import nl.rhaydus.platform.AndroidNetworkAvailabilityProvider
import nl.rhaydus.platform.NetworkAvailabilityProvider

actual val platformModule = module {
    single<NetworkAvailabilityProvider> {
        AndroidNetworkAvailabilityProvider(context = androidContext())
    }
}
