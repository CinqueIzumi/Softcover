package nl.rhaydus.softcover.core.profile.di

import kotlinx.datetime.TimeZone
import nl.rhaydus.softcover.core.domain.activity.MarkReadingActivityUseCase
import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.core.identity.di.identityModule
import nl.rhaydus.softcover.core.network.di.apolloModule
import nl.rhaydus.softcover.core.profile.data.datasource.ProfileLocalDataSource
import nl.rhaydus.softcover.core.profile.data.datasource.ProfileLocalDataSourceImpl
import nl.rhaydus.softcover.core.profile.data.datasource.ProfileRemoteDataSource
import nl.rhaydus.softcover.core.profile.data.datasource.ProfileRemoteDataSourceImpl
import nl.rhaydus.softcover.core.profile.data.repository.ProfileRepositoryImpl
import nl.rhaydus.softcover.core.profile.domain.ProfileRefreshGate
import nl.rhaydus.softcover.core.profile.domain.repository.ProfileRepository
import nl.rhaydus.softcover.core.profile.domain.usecase.MarkReadingActivityUseCaseImpl
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveMonthlyPagesReadUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingHistoryUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingStatsUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveRecentReadingActivityUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveUserProfileDataUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.RefreshReadingActivityUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.RefreshUserProfileDataUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.RefreshUserProfileStatsUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.time.Clock

// The activity half (streak + recent reading days) and the stats half (everything the Profile tab
// renders) refresh independently, each skipped at most once per session - see
// RefreshReadingActivityUseCase / RefreshUserProfileStatsUseCase.
private val activityRefreshGateQualifier = named("activityRefreshGate")
private val statsRefreshGateQualifier = named("statsRefreshGate")

val profileModule = module {
    includes(
        platformProfileModule,
        dispatcherModule,
        identityModule,
        apolloModule,
    )

    single<ProfileRemoteDataSource> {
        ProfileRemoteDataSourceImpl(
            apolloClient = get(),
            timeZone = get(),
        )
    }

    single<ProfileLocalDataSource> {
        ProfileLocalDataSourceImpl(profileCacheDataStore = get())
    }

    single(activityRefreshGateQualifier) { ProfileRefreshGate() }
    single(statsRefreshGateQualifier) { ProfileRefreshGate() }

    single<ProfileRepository> {
        ProfileRepositoryImpl(
            profileRemoteDataSource = get(),
            profileLocalDataSource = get(),
            activityRefreshGate = get(activityRefreshGateQualifier),
            statsRefreshGate = get(statsRefreshGateQualifier),
        )
    }

    factory {
        ObserveUserProfileDataUseCase(profileRepository = get())
    }

    factory {
        ObserveRecentReadingActivityUseCase(
            profileRepository = get(),
            clock = get(),
            timeZone = get(),
        )
    }

    factory<MarkReadingActivityUseCase> {
        MarkReadingActivityUseCaseImpl(
            profileRepository = get(),
            clock = get(),
            timeZone = get(),
        )
    }

    factory {
        RefreshReadingActivityUseCase(
            profileRepository = get(),
            getUserIdUseCase = get(),
            activityRefreshGate = get(activityRefreshGateQualifier),
            clock = get(),
            timeZone = get(),
        )
    }

    factory {
        RefreshUserProfileStatsUseCase(
            profileRepository = get(),
            statsRefreshGate = get(statsRefreshGateQualifier),
            refreshLibraryUseCase = get(),
            hasSyncedReadBooksThisSessionUseCase = get(),
        )
    }

    factory {
        RefreshUserProfileDataUseCase(
            refreshReadingActivityUseCase = get(),
            refreshUserProfileStatsUseCase = get(),
        )
    }

    factory {
        ObserveReadingStatsUseCase(
            getReadUserBooksUseCase = get(),
            appDispatchers = get(),
        )
    }

    factory {
        ObserveMonthlyPagesReadUseCase(
            observeProgressUpdatesUseCase = get(),
            getReadUserBooksUseCase = get(),
            timeZone = get(),
            appDispatchers = get(),
        )
    }

    factory {
        ObserveReadingHistoryUseCase(
            getReadUserBooksUseCase = get(),
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
