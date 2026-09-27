package nl.rhaydus.softcover.core.connectivity.di

import nl.rhaydus.offlinesync.DefaultOfflineWriteDrainer
import nl.rhaydus.offlinesync.DrainPolicy
import nl.rhaydus.softcover.core.book.di.bookModule
import nl.rhaydus.softcover.core.connectivity.data.store.PendingListWriteStore
import nl.rhaydus.softcover.core.connectivity.data.store.PendingUserBookWriteStore
import nl.rhaydus.softcover.core.connectivity.data.sync.ListWriteDrainerImpl
import nl.rhaydus.softcover.core.connectivity.data.sync.ListWriteReplay
import nl.rhaydus.softcover.core.connectivity.data.sync.UserBookWriteDrainerImpl
import nl.rhaydus.softcover.core.connectivity.data.sync.UserBookWriteReplay
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.database.dao.PendingListWriteDao
import nl.rhaydus.softcover.core.database.dao.PendingUserBookWriteDao
import nl.rhaydus.softcover.core.database.di.databaseModule
import nl.rhaydus.softcover.core.domain.connectivity.ListWriteDrainer
import nl.rhaydus.softcover.core.domain.connectivity.ListWriteQueue
import nl.rhaydus.softcover.core.domain.connectivity.UserBookWriteDrainer
import nl.rhaydus.softcover.core.domain.connectivity.UserBookWriteQueue
import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.core.domain.exception.RetryableSyncException
import nl.rhaydus.softcover.core.lists.di.listsModule
import org.koin.dsl.module

// The drain loop, its online trigger, the mutex, the poison cap, and the in-drain backoff all live in the
// foundation's DefaultOfflineWriteDrainer. What stays here is what is genuinely Softcover's: the replay
// dispatch, and the two places the queues legitimately differ.
//
//  - A user-book write that the server *rejects* (GraphQL error, 4xx) would fail identically on replay, so
//    only a RetryableSyncException halts the pass; everything else discards the row. A list write is always
//    treated as transient and retried.
//  - Lists retry three times in-drain with exponential backoff before halting; user-book writes do not.
private const val LIST_IN_DRAIN_RETRIES = 3
private const val USER_BOOK_IN_DRAIN_RETRIES = 1

val connectivityModule = module {
    includes(
        platformModule,
        dispatcherModule,
        databaseModule,
        bookModule,
        listsModule,
    )

    single<PendingUserBookWriteDao> {
        get<SoftcoverDatabase>().pendingUserBookWriteDao()
    }

    single<PendingListWriteDao> {
        get<SoftcoverDatabase>().pendingListWriteDao()
    }

    // The store is both the persistence seam the drainer reads and the queue the app enqueues onto.
    single { PendingUserBookWriteStore(dao = get()) }

    single { PendingListWriteStore(dao = get()) }

    single<UserBookWriteQueue> { get<PendingUserBookWriteStore>() }

    single<ListWriteQueue> { get<PendingListWriteStore>() }

    single { UserBookWriteReplay(booksRemoteDataSource = get()) }

    single {
        ListWriteReplay(
            listsRemoteDataSource = get(),
            listsLocalDataSource = get(),
        )
    }

    single<UserBookWriteDrainer> {
        val replay = get<UserBookWriteReplay>()

        UserBookWriteDrainerImpl(
            delegate = DefaultOfflineWriteDrainer(
                store = get<PendingUserBookWriteStore>(),
                networkAvailability = get(),
                dispatchers = get(),
                replay = { payload -> replay(payload) },
                hintKey = { payload -> payload.userBookId to payload.kind },
                isTransient = { error -> error is RetryableSyncException },
                policy = DrainPolicy(inDrainRetries = USER_BOOK_IN_DRAIN_RETRIES),
            ),
        )
    }

    single<ListWriteDrainer> {
        val replay = get<ListWriteReplay>()

        ListWriteDrainerImpl(
            delegate = DefaultOfflineWriteDrainer(
                store = get<PendingListWriteStore>(),
                networkAvailability = get(),
                dispatchers = get(),
                replay = { payload -> replay(payload) },
                hintKey = { null },
                isTransient = { true },
                policy = DrainPolicy(inDrainRetries = LIST_IN_DRAIN_RETRIES),
            ),
        )
    }
}
