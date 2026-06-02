package nl.rhaydus.softcover.feature.profile.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ToadScreenModel
import nl.rhaydus.softcover.core.domain.account.ResetUserDataUseCase
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveUserProfileDataUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.RefreshUserProfileDataUseCase
import nl.rhaydus.softcover.feature.profile.presentation.action.ProfileAction
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.flows.ProfileInitializer
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState

class ProfileScreenScreenModel(
    private val observeUserProfileDataUseCase: ObserveUserProfileDataUseCase,
    private val refreshUserProfileDataUseCase: RefreshUserProfileDataUseCase,
    private val resetUserDataUseCase: ResetUserDataUseCase,
    dispatchers: AppDispatchers,
    initializers: List<ProfileInitializer>,
) : ToadScreenModel<ProfileUiState, ProfileEvent, ProfileDependencies, ProfileInitializer, LocalProfileVariables>(
    initializers = initializers,
    initialState = ProfileUiState(),
    initialLocalVariables = LocalProfileVariables(),
) {
    override val dependencies: ProfileDependencies = ProfileDependencies(
        observeUserProfileDataUseCase = observeUserProfileDataUseCase,
        refreshUserProfileDataUseCase = refreshUserProfileDataUseCase,
        resetUserDataUseCase = resetUserDataUseCase,
        mainDispatcher = dispatchers.main,
        coroutineScope = screenModelScope,
    )

    init {
        startInitializers()
    }

    fun runAction(action: ProfileAction) = dispatch(action)
}
