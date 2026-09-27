package nl.rhaydus.softcover.core.connectivity.di

import nl.rhaydus.platform.AndroidNetworkAvailabilityProvider
import nl.rhaydus.platform.NetworkAvailabilityProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
    single<NetworkAvailabilityProvider> {
        AndroidNetworkAvailabilityProvider(context = androidContext())
    }
}
