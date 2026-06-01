package nl.rhaydus.softcover.feature.settings.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.presentation.toad.ToadScreenModel
import nl.rhaydus.softcover.feature.lists.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.feature.library.domain.usecase.RefreshLibraryUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetEnabledListIdsAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetEnabledStatusCodesAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetLibraryTabOrderAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetEnabledListIdsUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetEnabledStatusCodesUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetLibraryTabOrderUseCase
import nl.rhaydus.softcover.feature.settings.presentation.action.LibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.event.LibraryVisibilitySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.flows.LibraryVisibilityInitializer
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState

class LibraryVisibilitySettingsScreenModel(
    private val getEnabledStatusCodesAsFlowUseCase: GetEnabledStatusCodesAsFlowUseCase,
    private val getEnabledListIdsAsFlowUseCase: GetEnabledListIdsAsFlowUseCase,
    private val getLibraryTabOrderAsFlowUseCase: GetLibraryTabOrderAsFlowUseCase,
    private val setEnabledStatusCodesUseCase: SetEnabledStatusCodesUseCase,
    private val setEnabledListIdsUseCase: SetEnabledListIdsUseCase,
    private val setLibraryTabOrderUseCase: SetLibraryTabOrderUseCase,
    private val getAllUserListsUseCase: GetAllUserListsUseCase,
    private val refreshLibraryUseCase: RefreshLibraryUseCase,
    private val applicationScope: ApplicationScope,
    appDispatchers: AppDispatchers,
    flows: List<LibraryVisibilityInitializer>,
) : ToadScreenModel<
    LibraryVisibilitySettingsUiState,
    LibraryVisibilitySettingsEvent,
    LibraryVisibilitySettingsDependencies,
    LibraryVisibilityInitializer,
    LibraryVisibilitySettingsLocalVariables,
    >(
    initialState = LibraryVisibilitySettingsUiState(),
    initialLocalVariables = LibraryVisibilitySettingsLocalVariables(),
    initializers = flows,
) {
    override val dependencies = LibraryVisibilitySettingsDependencies(
        getEnabledStatusCodesAsFlowUseCase = getEnabledStatusCodesAsFlowUseCase,
        getEnabledListIdsAsFlowUseCase = getEnabledListIdsAsFlowUseCase,
        getLibraryTabOrderAsFlowUseCase = getLibraryTabOrderAsFlowUseCase,
        setEnabledStatusCodesUseCase = setEnabledStatusCodesUseCase,
        setEnabledListIdsUseCase = setEnabledListIdsUseCase,
        setLibraryTabOrderUseCase = setLibraryTabOrderUseCase,
        getAllUserListsUseCase = getAllUserListsUseCase,
        refreshLibraryUseCase = refreshLibraryUseCase,
        applicationScope = applicationScope,
        mainDispatcher = appDispatchers.main,
        coroutineScope = screenModelScope,
    )

    init {
        startInitializers()
    }

    fun runAction(action: LibraryVisibilityAction) = dispatch(action = action)
}
