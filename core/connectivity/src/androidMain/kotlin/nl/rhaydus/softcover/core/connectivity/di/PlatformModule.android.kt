package nl.rhaydus.softcover.core.connectivity.di

import nl.rhaydus.softcover.core.connectivity.data.datasource.ConnectivityDataSource
import nl.rhaydus.softcover.core.connectivity.data.datasource.ConnectivityDataSourceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
    single<ConnectivityDataSource> { ConnectivityDataSourceImpl(context = androidContext()) }
}
