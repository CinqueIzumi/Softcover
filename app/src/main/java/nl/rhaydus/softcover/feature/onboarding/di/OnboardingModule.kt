package nl.rhaydus.softcover.feature.onboarding.di

import nl.rhaydus.softcover.feature.onboarding.presentation.screenmodel.OnboardingScreenScreenModel
import org.koin.dsl.module

val onboardingModule = module {
    factory {
        OnboardingScreenScreenModel(
            initializeUserIdAndBooksUseCase = get(),
            resetUserDataUseCase = get(),
            updateApiKeyUseCase = get(),
            dispatchers = get(),
            initializers = getAll()
        )
    }
}