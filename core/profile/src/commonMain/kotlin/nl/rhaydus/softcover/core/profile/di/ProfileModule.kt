package nl.rhaydus.softcover.core.profile.di

import kotlinx.datetime.TimeZone
import nl.rhaydus.softcover.core.corpus.di.corpusModule
import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.core.identity.di.identityModule
import nl.rhaydus.softcover.core.network.di.apolloModule
import nl.rhaydus.softcover.core.personal.di.personalModule
import nl.rhaydus.softcover.core.profile.data.datasource.ProfileLocalDataSource
import nl.rhaydus.softcover.core.profile.data.datasource.ProfileLocalDataSourceImpl
import nl.rhaydus.softcover.core.profile.data.datasource.ProfileRemoteDataSource
import nl.rhaydus.softcover.core.profile.data.datasource.ProfileRemoteDataSourceImpl
import nl.rhaydus.softcover.core.profile.data.repository.ProfileRepositoryImpl
import nl.rhaydus.softcover.core.profile.domain.ProfileRefreshGate
import nl.rhaydus.softcover.core.profile.domain.repository.ProfileRepository
import nl.rhaydus.softcover.core.profile.domain.usecase.FillReaderEditionLanguagesUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveDidNotFinishStatsUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveDiversityStatsUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveHourlyPagesReadUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveLongestWaitingUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveMonthlyPagesReadUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObservePageCreditInputsUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObservePagesReadUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingActivityUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingDayUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingHistoryUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingListStatsUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingStatsUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveTimeToFinishUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveUserProfileDataUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveWrittenStatsUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.RefreshUserProfileStatsUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.time.Clock

// Skipped at most once per session - see RefreshUserProfileStatsUseCase.
private val statsRefreshGateQualifier = named("statsRefreshGate")

val profileModule = module {
    includes(
        platformProfileModule,
        dispatcherModule,
        identityModule,
        apolloModule,
        personalModule,
        corpusModule,
    )

    single<ProfileRemoteDataSource> {
        ProfileRemoteDataSourceImpl(
            apolloClient = get(),
        )
    }

    single<ProfileLocalDataSource> {
        ProfileLocalDataSourceImpl(profileCacheDataStore = get())
    }

    single(statsRefreshGateQualifier) { ProfileRefreshGate() }

    single<ProfileRepository> {
        ProfileRepositoryImpl(
            profileRemoteDataSource = get(),
            profileLocalDataSource = get(),
            statsRefreshGate = get(statsRefreshGateQualifier),
        )
    }

    factory {
        ObserveUserProfileDataUseCase(profileRepository = get())
    }

    factory {
        RefreshUserProfileStatsUseCase(
            profileRepository = get(),
            statsRefreshGate = get(statsRefreshGateQualifier),
            refreshLibraryUseCase = get(),
            hasSyncedStatusesThisSessionUseCase = get(),
            refreshAllQuotesUseCase = get(),
        )
    }

    factory {
        ObserveDidNotFinishStatsUseCase(
            getDidNotFinishUserBooksUseCase = get(),
            observeUserTagsByBookUseCase = get(),
            appDispatchers = get(),
        )
    }

    factory {
        ObserveReadingListStatsUseCase(
            getAllUserBooksUseCase = get(),
            appDispatchers = get(),
        )
    }

    factory {
        ObserveLongestWaitingUseCase(
            getAllUserBooksUseCase = get(),
            getAllUserListsUseCase = get(),
            appDispatchers = get(),
        )
    }

    factory {
        ObserveReadingStatsUseCase(
            getReadUserBooksUseCase = get(),
            observeUserTagsByBookUseCase = get(),
            appDispatchers = get(),
        )
    }

    factory {
        FillReaderEditionLanguagesUseCase(
            getCurrentlyReadingUserBooksUseCase = get(),
            getPausedUserBooksUseCase = get(),
            getReadUserBooksUseCase = get(),
            getDidNotFinishUserBooksUseCase = get(),
            fillEditionLanguagesUseCase = get(),
        )
    }

    factory {
        ObserveDiversityStatsUseCase(
            getCurrentlyReadingUserBooksUseCase = get(),
            getPausedUserBooksUseCase = get(),
            getReadUserBooksUseCase = get(),
            getDidNotFinishUserBooksUseCase = get(),
            observeProgressUpdatesUseCase = get(),
            observeAllBookTraitsUseCase = get(),
            observeAllBookCopiesUseCase = get(),
            appDispatchers = get(),
            timeZone = get(),
        )
    }

    factory {
        ObserveWrittenStatsUseCase(
            getAllUserBooksUseCase = get(),
            observeAllQuotesUseCase = get(),
            timeZone = get(),
            appDispatchers = get(),
        )
    }

    factory {
        ObserveTimeToFinishUseCase(
            getAllUserBooksUseCase = get(),
            appDispatchers = get(),
        )
    }

    factory {
        ObserveHourlyPagesReadUseCase(
            observeProgressUpdatesUseCase = get(),
            timeZone = get(),
            appDispatchers = get(),
        )
    }

    factory {
        ObservePageCreditInputsUseCase(
            observeProgressUpdatesUseCase = get(),
            getReadUserBooksUseCase = get(),
            getDidNotFinishUserBooksUseCase = get(),
        )
    }

    factory {
        ObservePagesReadUseCase(
            observePageCreditInputsUseCase = get(),
            appDispatchers = get(),
            clock = get(),
            timeZone = get(),
        )
    }

    factory {
        ObserveMonthlyPagesReadUseCase(
            observePageCreditInputsUseCase = get(),
            timeZone = get(),
            appDispatchers = get(),
        )
    }

    factory {
        ObserveReadingActivityUseCase(
            observePageCreditInputsUseCase = get(),
            timeZone = get(),
            appDispatchers = get(),
        )
    }

    factory {
        ObserveReadingDayUseCase(
            observePageCreditInputsUseCase = get(),
            timeZone = get(),
            appDispatchers = get(),
        )
    }

    factory {
        ObserveReadingHistoryUseCase(
            observePageCreditInputsUseCase = get(),
            appDispatchers = get(),
            clock = get(),
            timeZone = get(),
        )
    }

    // Reading-activity dates are bucketed in the user's local timezone so a streak day matches the
    // calendar day they actually read on; Clock + TimeZone are the shared basis every collaborator uses.
    single<Clock> { Clock.System }

    // Captured once at graph creation (not a live call), so a mid-session timezone change only takes
    // effect after an app restart — an acceptable trade-off for a day-granularity streak.
    single<TimeZone> { TimeZone.currentSystemDefault() }
}
