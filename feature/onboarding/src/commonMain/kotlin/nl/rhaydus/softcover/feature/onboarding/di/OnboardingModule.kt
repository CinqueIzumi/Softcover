package nl.rhaydus.softcover.feature.onboarding.di

import org.koin.dsl.module
import nl.rhaydus.softcover.core.designsystem.presentation.di.designSystemModule
import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.core.identity.di.identityModule
import nl.rhaydus.softcover.feature.onboarding.presentation.screenmodel.OnboardingScreenScreenModel

val onboardingModule = module {
    includes(
        dispatcherModule,
        identityModule,
        designSystemModule,
    )

    factory {
        OnboardingScreenScreenModel(
            initializeUserIdAndBooksUseCase = get(),
            resetUserDataUseCase = get(),
            updateApiKeyUseCase = get(),
            dispatchers = get(),
            initializers = getAll(),
        )
    }
}
