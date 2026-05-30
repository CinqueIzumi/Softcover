package nl.rhaydus.softcover.feature.connectivity.di

import nl.rhaydus.softcover.core.data.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.domain.connectivity.ListWriteDrainer
import nl.rhaydus.softcover.core.domain.connectivity.ListWriteQueue
import nl.rhaydus.softcover.core.domain.connectivity.NetworkAvailabilityProvider
import nl.rhaydus.softcover.core.domain.connectivity.UserBookWriteQueue
import nl.rhaydus.softcover.core.domain.connectivity.UserBookWriteDrainer
import nl.rhaydus.softcover.feature.connectivity.data.dao.PendingListWriteDao
import nl.rhaydus.softcover.feature.connectivity.data.dao.PendingUserBookWriteDao
import nl.rhaydus.softcover.feature.connectivity.data.datasource.ConnectivityDataSource
import nl.rhaydus.softcover.feature.connectivity.data.datasource.ConnectivityDataSourceImpl
import nl.rhaydus.softcover.feature.connectivity.data.repository.ConnectivityRepositoryImpl
import nl.rhaydus.softcover.feature.connectivity.data.repository.ListWriteQueueImpl
import nl.rhaydus.softcover.feature.connectivity.data.repository.UserBookWriteQueueImpl
import nl.rhaydus.softcover.feature.connectivity.data.sync.PendingListWriteSyncer
import nl.rhaydus.softcover.feature.connectivity.data.sync.PendingUserBookWriteSyncer
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val connectivityModule = module {
    single<ConnectivityDataSource> {
        ConnectivityDataSourceImpl(context = androidContext())
    }

    single<NetworkAvailabilityProvider> {
        ConnectivityRepositoryImpl(dataSource = get())
    }

    single<PendingUserBookWriteDao> {
        get<SoftcoverDatabase>().pendingUserBookWriteDao()
    }

    single<PendingListWriteDao> {
        get<SoftcoverDatabase>().pendingListWriteDao()
    }

    single<UserBookWriteQueue> {
        UserBookWriteQueueImpl(dao = get())
    }

    single<ListWriteQueue> {
        ListWriteQueueImpl(dao = get())
    }

    single {
        PendingUserBookWriteSyncer(
            networkAvailability = get(),
            dao = get(),
            booksRemoteDataSource = get(),
        )
    }

    single<UserBookWriteDrainer> {
        get<PendingUserBookWriteSyncer>()
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
