package nl.rhaydus.softcover.core.connectivity.di

import nl.rhaydus.softcover.core.connectivity.data.repository.ConnectivityRepositoryImpl
import nl.rhaydus.softcover.core.connectivity.data.repository.ListWriteQueueImpl
import nl.rhaydus.softcover.core.connectivity.data.repository.UserBookWriteQueueImpl
import nl.rhaydus.softcover.core.connectivity.data.sync.PendingListWriteSyncer
import nl.rhaydus.softcover.core.connectivity.data.sync.PendingUserBookWriteSyncer
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.database.dao.PendingListWriteDao
import nl.rhaydus.softcover.core.database.dao.PendingUserBookWriteDao
import nl.rhaydus.softcover.core.domain.connectivity.ListWriteDrainer
import nl.rhaydus.softcover.core.domain.connectivity.ListWriteQueue
import nl.rhaydus.softcover.core.domain.connectivity.NetworkAvailabilityProvider
import nl.rhaydus.softcover.core.domain.connectivity.UserBookWriteDrainer
import nl.rhaydus.softcover.core.domain.connectivity.UserBookWriteQueue
import org.koin.dsl.module

val connectivityModule = module {
    includes(platformModule)

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
            appDispatchers = get(),
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
            appDispatchers = get(),
        )
    }

    single<ListWriteDrainer> {
        get<PendingListWriteSyncer>()
    }
}
