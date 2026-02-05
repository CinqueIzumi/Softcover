package nl.rhaydus.softcover.feature.onboarding.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import nl.rhaydus.softcover.core.presentation.toad.ToadScreenModel
import nl.rhaydus.softcover.feature.caching.domain.usecase.InitializeUserBooksUseCase
import nl.rhaydus.softcover.feature.onboarding.presentation.action.OnboardingAction
import nl.rhaydus.softcover.feature.onboarding.presentation.event.OnboardingEvent
import nl.rhaydus.softcover.feature.onboarding.presentation.initializer.OnboardingInitializer
import nl.rhaydus.softcover.feature.onboarding.presentation.state.LocalOnboardingVariables
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState
import nl.rhaydus.softcover.feature.settings.domain.usecase.InitializeUserIdAndBooksUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.ResetUserDataUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.UpdateApiKeyUseCase

class OnboardingScreenScreenModel(
    val initializeUserBooksUseCase: InitializeUserBooksUseCase,
    val initializeUserIdAndBooksUseCase: InitializeUserIdAndBooksUseCase,
    val resetUserDataUseCase: ResetUserDataUseCase,
    val updateApiKeyUseCase: UpdateApiKeyUseCase,
    dispatchers: AppDispatchers,
    initializers: List<OnboardingInitializer>,
) : ToadScreenModel<OnboardingUiState, OnboardingEvent, OnboardingDependencies, OnboardingInitializer, LocalOnboardingVariables>(
    initializers = initializers,
    initialState = OnboardingUiState(),
    initialLocalVariables = LocalOnboardingVariables(),
) {
    override val dependencies: OnboardingDependencies = OnboardingDependencies(
        initializeUserIdAndBooksUseCase = initializeUserIdAndBooksUseCase,
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