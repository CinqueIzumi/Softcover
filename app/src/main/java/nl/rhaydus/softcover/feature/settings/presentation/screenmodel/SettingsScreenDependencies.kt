package nl.rhaydus.softcover.feature.settings.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.presentation.toad.ActionDependencies
import nl.rhaydus.softcover.feature.caching.domain.usecase.InitializeUserBooksUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetApiKeyUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.ResetUserDataUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.UpdateApiKeyUseCase

data class SettingsScreenDependencies(
    val updateApiKeyUseCase: UpdateApiKeyUseCase,
    val getApiKeyUseCase: GetApiKeyUseCase,
    val resetUserDataUseCase: ResetUserDataUseCase,
    val initializeUserBooksUseCase: InitializeUserBooksUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()