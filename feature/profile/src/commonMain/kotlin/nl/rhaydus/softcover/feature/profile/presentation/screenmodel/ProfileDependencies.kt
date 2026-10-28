package nl.rhaydus.softcover.feature.profile.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.domain.account.ResetUserDataUseCase
import nl.rhaydus.softcover.core.domain.util.ObserveCurrentDateUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.ObserveReadingStatsBackfilledUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingActivityUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingHistoryUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingStatsUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveUserProfileDataUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.RefreshUserProfileStatsUseCase
import nl.rhaydus.toad.ActionDependencies

internal data class ProfileDependencies(
    val observeUserProfileDataUseCase: ObserveUserProfileDataUseCase,
    val observeReadingStatsUseCase: ObserveReadingStatsUseCase,
    val observeReadingHistoryUseCase: ObserveReadingHistoryUseCase,
    val observeReadingActivityUseCase: ObserveReadingActivityUseCase,
    val observeCurrentDateUseCase: ObserveCurrentDateUseCase,
    val observeReadingStatsBackfilledUseCase: ObserveReadingStatsBackfilledUseCase,
    val refreshUserProfileStatsUseCase: RefreshUserProfileStatsUseCase,
    val resetUserDataUseCase: ResetUserDataUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
