package nl.rhaydus.softcover.feature.profile.di

import nl.rhaydus.softcover.feature.profile.presentation.initializer.ProfileInitializer
import nl.rhaydus.softcover.feature.profile.presentation.initializer.UserInformationInitializer
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileScreenScreenModel
import org.koin.dsl.bind
import org.koin.dsl.module

val profileScreenModule = module {
    factory {
        ProfileScreenScreenModel(
            dispatchers = get(),
            initializers = getAll(),
            resetUserDataUseCase = get(),
            observeUserProfileDataUseCase = get(),
            refreshUserProfileDataUseCase = get(),
        )
    }

    factory { UserInformationInitializer() } bind ProfileInitializer::class
}
