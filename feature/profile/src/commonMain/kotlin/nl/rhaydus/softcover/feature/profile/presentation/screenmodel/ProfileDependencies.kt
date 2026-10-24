package nl.rhaydus.softcover.feature.profile.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.TimeZone
import nl.rhaydus.softcover.core.domain.account.ResetUserDataUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.ObserveAuthorDemographicsBackfilledUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingHistoryUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingStatsUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveUserProfileDataUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.RefreshUserProfileDataUseCase
import nl.rhaydus.toad.ActionDependencies
import kotlin.time.Clock

internal data class ProfileDependencies(
    val observeUserProfileDataUseCase: ObserveUserProfileDataUseCase,
    val observeReadingStatsUseCase: ObserveReadingStatsUseCase,
    val observeReadingHistoryUseCase: ObserveReadingHistoryUseCase,
    val observeAuthorDemographicsBackfilledUseCase: ObserveAuthorDemographicsBackfilledUseCase,
    val refreshUserProfileDataUseCase: RefreshUserProfileDataUseCase,
    val resetUserDataUseCase: ResetUserDataUseCase,
    val clock: Clock,
    val timeZone: TimeZone,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
