package nl.rhaydus.softcover.feature.profile.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.domain.account.ResetUserDataUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveUserProfileDataUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.RefreshUserProfileDataUseCase
import nl.rhaydus.toad.ActionDependencies

internal data class ProfileDependencies(
    val observeUserProfileDataUseCase: ObserveUserProfileDataUseCase,
    val refreshUserProfileDataUseCase: RefreshUserProfileDataUseCase,
    val resetUserDataUseCase: ResetUserDataUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
