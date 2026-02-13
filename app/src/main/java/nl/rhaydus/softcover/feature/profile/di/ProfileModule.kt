package nl.rhaydus.softcover.feature.profile.di

import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileScreenScreenModel
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserProfileDataUseCase
import org.koin.dsl.module

val profileModule = module {
    factory {
        ProfileScreenScreenModel(
            dispatchers = get(),
            initializers = getAll(),
            resetUserDataUseCase = get(),
            getUserProfileDataUseCase = get(),
        )
    }

    factory {
        GetUserProfileDataUseCase(settingsRepository = get())
    }
}