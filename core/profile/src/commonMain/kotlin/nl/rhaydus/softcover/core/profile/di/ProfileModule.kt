package nl.rhaydus.softcover.core.profile.di

import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import org.koin.dsl.module
import nl.rhaydus.softcover.core.domain.activity.MarkReadingActivityTodayUseCase
import nl.rhaydus.softcover.core.profile.data.datasource.ProfileLocalDataSource
import nl.rhaydus.softcover.core.profile.data.datasource.ProfileLocalDataSourceImpl
import nl.rhaydus.softcover.core.profile.data.datasource.ProfileRemoteDataSource
import nl.rhaydus.softcover.core.profile.data.datasource.ProfileRemoteDataSourceImpl
import nl.rhaydus.softcover.core.profile.data.repository.ProfileRepositoryImpl
import nl.rhaydus.softcover.core.profile.domain.repository.ProfileRepository
import nl.rhaydus.softcover.core.profile.domain.usecase.MarkReadingActivityTodayUseCaseImpl
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveRecentReadingActivityUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveUserProfileDataUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.RefreshUserProfileDataUseCase

val profileModule = module {
    includes(platformProfileModule)

    single<ProfileRemoteDataSource> {
        ProfileRemoteDataSourceImpl(
            apolloClient = get(),
            timeZone = get(),
        )
    }

    single<ProfileLocalDataSource> {
        ProfileLocalDataSourceImpl(profileCacheDataStore = get())
    }

    single<ProfileRepository> {
        ProfileRepositoryImpl(
            profileRemoteDataSource = get(),
            profileLocalDataSource = get(),
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

    factory<MarkReadingActivityTodayUseCase> {
        MarkReadingActivityTodayUseCaseImpl(
            profileRepository = get(),
            clock = get(),
            timeZone = get(),
        )
    }

    factory {
        RefreshUserProfileDataUseCase(
            profileRepository = get(),
            getUserIdUseCase = get(),
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
