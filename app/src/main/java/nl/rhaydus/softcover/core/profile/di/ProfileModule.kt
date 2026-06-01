package nl.rhaydus.softcover.core.profile.di

import nl.rhaydus.softcover.core.profile.data.datasource.ProfileLocalDataSource
import nl.rhaydus.softcover.core.profile.data.datasource.ProfileLocalDataSourceImpl
import nl.rhaydus.softcover.core.profile.data.datasource.ProfileRemoteDataSource
import nl.rhaydus.softcover.core.profile.data.datasource.ProfileRemoteDataSourceImpl
import nl.rhaydus.softcover.core.profile.data.datastore.ProfileCacheDataStore
import nl.rhaydus.softcover.core.profile.data.datastore.profileCache
import nl.rhaydus.softcover.core.profile.data.repository.ProfileRepositoryImpl
import nl.rhaydus.softcover.core.profile.domain.repository.ProfileRepository
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveRecentReadingActivityUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveUserProfileDataUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.RefreshUserProfileDataUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.time.Clock

val profileModule = module {
    single<ProfileRemoteDataSource> {
        ProfileRemoteDataSourceImpl(apolloClient = get())
    }

    single<ProfileCacheDataStore> {
        ProfileCacheDataStore(store = androidContext().profileCache)
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
        )
    }

    factory {
        RefreshUserProfileDataUseCase(
            profileRepository = get(),
            getUserIdUseCase = get(),
            clock = get(),
        )
    }

    // Hardcover serves action_at as a UTC calendar date, so streak comparisons must use UTC too.
    single<Clock> { Clock.systemUTC() }
}
