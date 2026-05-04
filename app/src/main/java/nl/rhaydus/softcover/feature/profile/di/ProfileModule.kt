package nl.rhaydus.softcover.feature.profile.di

import nl.rhaydus.softcover.feature.profile.data.datasource.ProfileRemoteDataSource
import nl.rhaydus.softcover.feature.profile.data.datasource.ProfileRemoteDataSourceImpl
import nl.rhaydus.softcover.feature.profile.data.repository.ProfileRepositoryImpl
import nl.rhaydus.softcover.feature.profile.domain.repository.ProfileRepository
import nl.rhaydus.softcover.feature.profile.domain.usecase.GetUserProfileDataUseCase
import nl.rhaydus.softcover.feature.profile.presentation.initializer.ProfileInitializer
import nl.rhaydus.softcover.feature.profile.presentation.initializer.UserInformationInitializer
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileScreenScreenModel
import org.koin.dsl.bind
import org.koin.dsl.module
import java.time.Clock

val profileModule = module {
    factory {
        ProfileScreenScreenModel(
            dispatchers = get(),
            initializers = getAll(),
            resetUserDataUseCase = get(),
            getUserProfileDataUseCase = get(),
        )
    }

    factory { UserInformationInitializer() } bind ProfileInitializer::class

    single<ProfileRemoteDataSource> {
        ProfileRemoteDataSourceImpl(apolloClient = get())
    }

    single<ProfileRepository> {
        ProfileRepositoryImpl(profileRemoteDataSource = get())
    }

    factory {
        GetUserProfileDataUseCase(
            profileRepository = get(),
            getUserIdUseCase = get(),
            clock = get(),
        )
    }

    // Hardcover serves action_at as a UTC calendar date, so streak comparisons must use UTC too.
    single<Clock> { Clock.systemUTC() }
}
