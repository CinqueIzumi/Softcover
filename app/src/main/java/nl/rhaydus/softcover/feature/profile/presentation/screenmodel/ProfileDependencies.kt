package nl.rhaydus.softcover.feature.profile.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.presentation.toad.ActionDependencies
import nl.rhaydus.softcover.feature.profile.domain.usecase.ObserveUserProfileDataUseCase
import nl.rhaydus.softcover.feature.profile.domain.usecase.RefreshUserProfileDataUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.ResetUserDataUseCase

data class ProfileDependencies(
    val observeUserProfileDataUseCase: ObserveUserProfileDataUseCase,
    val refreshUserProfileDataUseCase: RefreshUserProfileDataUseCase,
    val resetUserDataUseCase: ResetUserDataUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
