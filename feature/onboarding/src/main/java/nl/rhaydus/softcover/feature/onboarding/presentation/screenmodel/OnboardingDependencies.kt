package nl.rhaydus.softcover.feature.onboarding.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionDependencies
import nl.rhaydus.softcover.core.domain.account.InitializeUserIdAndBooksUseCase
import nl.rhaydus.softcover.core.domain.account.ResetUserDataUseCase
import nl.rhaydus.softcover.core.identity.domain.usecase.UpdateApiKeyUseCase

class OnboardingDependencies(
    val initializeUserIdAndBooksUseCase: InitializeUserIdAndBooksUseCase,
    val resetUserDataUseCase: ResetUserDataUseCase,
    val updateApiKeyUseCase: UpdateApiKeyUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()