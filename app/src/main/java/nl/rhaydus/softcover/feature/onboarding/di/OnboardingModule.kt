package nl.rhaydus.softcover.feature.onboarding.di

import nl.rhaydus.softcover.feature.onboarding.presentation.viewmodel.OnboardingScreenModel
import org.koin.dsl.module

val onboardingModule = module {
    single<OnboardingScreenModel> {
        OnboardingScreenModel(
            initializeUserBooksUseCase = get(),
            initializeUserDataUseCase = get(),
            resetUserDataUseCase = get(),
            updateApiKeyUseCase = get(),
            dispatchers = get(),
            initializers = getAll()
        )
    }
}