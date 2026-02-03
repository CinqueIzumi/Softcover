package nl.rhaydus.softcover.feature.onboarding.presentation.viewmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import nl.rhaydus.softcover.core.presentation.toad.ToadViewModel
import nl.rhaydus.softcover.feature.caching.domain.usecase.InitializeUserBooksUseCase
import nl.rhaydus.softcover.feature.onboarding.presentation.action.OnboardingAction
import nl.rhaydus.softcover.feature.onboarding.presentation.event.OnboardingEvent
import nl.rhaydus.softcover.feature.onboarding.presentation.initializer.OnboardingInitializer
import nl.rhaydus.softcover.feature.onboarding.presentation.state.LocalOnboardingVariables
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState
import nl.rhaydus.softcover.feature.settings.domain.usecase.InitializeUserDataUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.ResetUserDataUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.UpdateApiKeyUseCase

class OnboardingScreenModel(
    val initializeUserBooksUseCase: InitializeUserBooksUseCase,
    val initializeUserDataUseCase: InitializeUserDataUseCase,
    val resetUserDataUseCase: ResetUserDataUseCase,
    val updateApiKeyUseCase: UpdateApiKeyUseCase,
    dispatchers: AppDispatchers,
    initializers: List<OnboardingInitializer>,
) : ToadViewModel<OnboardingUiState, OnboardingEvent, OnboardingDependencies, OnboardingInitializer, LocalOnboardingVariables>(
    initializers = initializers,
    initialState = OnboardingUiState(),
    initialLocalVariables = LocalOnboardingVariables(),
) {
    override val dependencies: OnboardingDependencies = OnboardingDependencies(
        initializeUserDataUseCase = initializeUserDataUseCase,
        initializeUserBooksUseCase = initializeUserBooksUseCase,
        resetUserDataUseCase = resetUserDataUseCase,
        updateApiKeyUseCase = updateApiKeyUseCase,
        mainDispatcher = dispatchers.main,
        coroutineScope = screenModelScope,
    )

    init {
        startInitializers()
    }

    fun runAction(action: OnboardingAction) = dispatch(action)
}