package nl.rhaydus.softcover.core.connectivity.di

import org.koin.dsl.binds
import org.koin.dsl.module
import nl.rhaydus.softcover.core.connectivity.data.datasource.ConnectivityDataSource
import nl.rhaydus.softcover.core.connectivity.data.datasource.ConnectivityDataSourceImpl

actual val platformModule = module {
    // Also bound as AutoCloseable so the desktop shutdown teardown can stop its poll loop without
    // orchestration naming this internal type.
    single { ConnectivityDataSourceImpl(dispatchers = get()) } binds
        arrayOf(ConnectivityDataSource::class, AutoCloseable::class)
}
