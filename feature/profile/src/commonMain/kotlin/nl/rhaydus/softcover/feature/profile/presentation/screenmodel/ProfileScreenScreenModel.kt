package nl.rhaydus.softcover.feature.profile.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.datetime.TimeZone
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.domain.account.ResetUserDataUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingLifeUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveUserProfileDataUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.RefreshUserProfileDataUseCase
import nl.rhaydus.softcover.feature.profile.presentation.action.ProfileAction
import nl.rhaydus.softcover.feature.profile.presentation.collector.ProfileCollector
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.toad.ToadScreenModel
import kotlin.time.Clock

internal class ProfileScreenScreenModel(
    private val observeUserProfileDataUseCase: ObserveUserProfileDataUseCase,
    private val observeReadingLifeUseCase: ObserveReadingLifeUseCase,
    private val refreshUserProfileDataUseCase: RefreshUserProfileDataUseCase,
    private val resetUserDataUseCase: ResetUserDataUseCase,
    private val clock: Clock,
    private val timeZone: TimeZone,
    dispatchers: AppDispatchers,
    initializers: List<ProfileCollector>,
) : ToadScreenModel<ProfileUiState, ProfileEvent, ProfileDependencies, ProfileCollector, LocalProfileVariables>(
    initializers = initializers,
    initialState = ProfileUiState(),
    initialLocalVariables = LocalProfileVariables(),
) {
    override val dependencies: ProfileDependencies = ProfileDependencies(
        observeUserProfileDataUseCase = observeUserProfileDataUseCase,
        observeReadingLifeUseCase = observeReadingLifeUseCase,
        refreshUserProfileDataUseCase = refreshUserProfileDataUseCase,
        resetUserDataUseCase = resetUserDataUseCase,
        clock = clock,
        timeZone = timeZone,
        mainDispatcher = dispatchers.main,
        coroutineScope = screenModelScope,
    )

    init {
        startInitializers()
    }

    fun runAction(action: ProfileAction) = dispatch(action)
}
