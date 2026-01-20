package nl.rhaydus.softcover.feature.settings.presentation.viewmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.presentation.toad.ActionDependencies
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetApiKeyUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.InitializeUserIdUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.ResetUserDataUSeCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.UpdateApiKeyUseCase

data class SettingsScreenDependencies(
    val updateApiKeyUseCase: UpdateApiKeyUseCase,
    val getApiKeyUseCase: GetApiKeyUseCase,
    val initializeUserIdUseCase: InitializeUserIdUseCase,
    val resetUserDataUseCase: ResetUserDataUSeCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()