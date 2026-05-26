package nl.rhaydus.softcover.feature.connectivity.di

import nl.rhaydus.softcover.core.data.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.domain.connectivity.ListWriteDrainer
import nl.rhaydus.softcover.core.domain.connectivity.ListWriteQueue
import nl.rhaydus.softcover.core.domain.connectivity.NetworkAvailabilityProvider
import nl.rhaydus.softcover.core.domain.connectivity.OfflineProgressQueue
import nl.rhaydus.softcover.core.domain.connectivity.PendingProgressDrainer
import nl.rhaydus.softcover.feature.connectivity.data.dao.PendingListWriteDao
import nl.rhaydus.softcover.feature.connectivity.data.dao.PendingProgressUpdateDao
import nl.rhaydus.softcover.feature.connectivity.data.datasource.ConnectivityDataSource
import nl.rhaydus.softcover.feature.connectivity.data.datasource.ConnectivityDataSourceImpl
import nl.rhaydus.softcover.feature.connectivity.data.repository.ConnectivityRepositoryImpl
import nl.rhaydus.softcover.feature.connectivity.data.repository.ListWriteQueueImpl
import nl.rhaydus.softcover.feature.connectivity.data.repository.OfflineProgressQueueImpl
import nl.rhaydus.softcover.feature.connectivity.data.sync.PendingListWriteSyncer
import nl.rhaydus.softcover.feature.connectivity.data.sync.PendingProgressSyncer
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val connectivityModule = module {
    single<ConnectivityDataSource> {
        ConnectivityDataSourceImpl(context = androidContext())
    }

    single<NetworkAvailabilityProvider> {
        ConnectivityRepositoryImpl(dataSource = get())
    }

    single<PendingProgressUpdateDao> {
        get<SoftcoverDatabase>().pendingProgressUpdateDao()
    }

    single<PendingListWriteDao> {
        get<SoftcoverDatabase>().pendingListWriteDao()
    }

    single<OfflineProgressQueue> {
        OfflineProgressQueueImpl(dao = get())
    }

    single<ListWriteQueue> {
        ListWriteQueueImpl(dao = get())
    }

    single {
        PendingProgressSyncer(
            networkAvailability = get(),
            dao = get(),
            booksRemoteDataSource = get(),
        )
    }

    single<PendingProgressDrainer> {
        get<PendingProgressSyncer>()
    }

    single {
        PendingListWriteSyncer(
            networkAvailability = get(),
            dao = get(),
            listsRemoteDataSource = get(),
            listsLocalDataSource = get(),
        )
    }

    single<ListWriteDrainer> {
        get<PendingListWriteSyncer>()
    }
}
