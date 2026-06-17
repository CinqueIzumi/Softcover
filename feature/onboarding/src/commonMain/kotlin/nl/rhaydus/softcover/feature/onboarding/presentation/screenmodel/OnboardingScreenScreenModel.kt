package nl.rhaydus.softcover.feature.onboarding.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.softcover.core.domain.account.InitializeUserIdAndBooksUseCase
import nl.rhaydus.softcover.core.domain.account.ResetUserDataUseCase
import nl.rhaydus.softcover.core.identity.domain.usecase.UpdateApiKeyUseCase
import nl.rhaydus.softcover.feature.onboarding.presentation.action.OnboardingAction
import nl.rhaydus.softcover.feature.onboarding.presentation.event.OnboardingEvent
import nl.rhaydus.softcover.feature.onboarding.presentation.flows.OnboardingInitializer
import nl.rhaydus.softcover.feature.onboarding.presentation.state.LocalOnboardingVariables
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState
import nl.rhaydus.toad.ToadScreenModel
import nl.rhaydus.ui.common.AppDispatchers

internal class OnboardingScreenScreenModel(
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
