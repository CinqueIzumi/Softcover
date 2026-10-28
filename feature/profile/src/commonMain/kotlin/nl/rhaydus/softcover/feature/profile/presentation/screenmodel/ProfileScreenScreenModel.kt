package nl.rhaydus.softcover.feature.profile.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.domain.account.ResetUserDataUseCase
import nl.rhaydus.softcover.core.domain.util.ObserveCurrentDateUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.ObserveReadingStatsBackfilledUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingActivityUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingHistoryUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingStatsUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveUserProfileDataUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.RefreshUserProfileStatsUseCase
import nl.rhaydus.softcover.feature.profile.presentation.action.ProfileAction
import nl.rhaydus.softcover.feature.profile.presentation.collector.ProfileCollector
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.toad.ToadScreenModel

internal class ProfileScreenScreenModel(
    private val observeUserProfileDataUseCase: ObserveUserProfileDataUseCase,
    private val observeReadingStatsUseCase: ObserveReadingStatsUseCase,
    private val observeReadingHistoryUseCase: ObserveReadingHistoryUseCase,
    private val observeReadingActivityUseCase: ObserveReadingActivityUseCase,
    private val observeCurrentDateUseCase: ObserveCurrentDateUseCase,
    private val observeReadingStatsBackfilledUseCase: ObserveReadingStatsBackfilledUseCase,
    private val refreshUserProfileStatsUseCase: RefreshUserProfileStatsUseCase,
    private val resetUserDataUseCase: ResetUserDataUseCase,
    dispatchers: AppDispatchers,
    initializers: List<ProfileCollector>,
) : ToadScreenModel<ProfileUiState, ProfileEvent, ProfileDependencies, ProfileCollector, LocalProfileVariables>(
    initializers = initializers,
    initialState = ProfileUiState(),
    initialLocalVariables = LocalProfileVariables(),
) {
    override val dependencies: ProfileDependencies = ProfileDependencies(
        observeUserProfileDataUseCase = observeUserProfileDataUseCase,
        observeReadingStatsUseCase = observeReadingStatsUseCase,
        observeReadingHistoryUseCase = observeReadingHistoryUseCase,
        observeReadingActivityUseCase = observeReadingActivityUseCase,
        observeCurrentDateUseCase = observeCurrentDateUseCase,
        observeReadingStatsBackfilledUseCase = observeReadingStatsBackfilledUseCase,
        refreshUserProfileStatsUseCase = refreshUserProfileStatsUseCase,
        resetUserDataUseCase = resetUserDataUseCase,
        mainDispatcher = dispatchers.main,
        coroutineScope = screenModelScope,
    )

    init {
        startInitializers()
    }

    fun runAction(action: ProfileAction) = dispatch(action)
}
