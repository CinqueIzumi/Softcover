package nl.rhaydus.softcover.feature.settings.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetDidNotFinishUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetReadUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetWantToReadUserBooksUseCase
import nl.rhaydus.softcover.core.domain.account.RefreshLibraryUseCase
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.lists.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetEnabledListIdsAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetEnabledStatusCodesAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetLibraryTabOrderAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetEnabledListIdsUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetEnabledStatusCodesUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetLibraryTabOrderUseCase
import nl.rhaydus.softcover.feature.settings.presentation.action.LibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.collector.LibraryVisibilityCollector
import nl.rhaydus.softcover.feature.settings.presentation.event.LibraryVisibilitySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import nl.rhaydus.toad.ToadScreenModel

internal class LibraryVisibilitySettingsScreenModel(
    private val getEnabledStatusCodesAsFlowUseCase: GetEnabledStatusCodesAsFlowUseCase,
    private val getEnabledListIdsAsFlowUseCase: GetEnabledListIdsAsFlowUseCase,
    private val getLibraryTabOrderAsFlowUseCase: GetLibraryTabOrderAsFlowUseCase,
    private val setEnabledStatusCodesUseCase: SetEnabledStatusCodesUseCase,
    private val setEnabledListIdsUseCase: SetEnabledListIdsUseCase,
    private val setLibraryTabOrderUseCase: SetLibraryTabOrderUseCase,
    private val getAllUserListsUseCase: GetAllUserListsUseCase,
    private val getAllUserBooksUseCase: GetAllUserBooksUseCase,
    private val getCurrentlyReadingUserBooksUseCase: GetCurrentlyReadingUserBooksUseCase,
    private val getWantToReadUserBooksUseCase: GetWantToReadUserBooksUseCase,
    private val getReadUserBooksUseCase: GetReadUserBooksUseCase,
    private val getDidNotFinishUserBooksUseCase: GetDidNotFinishUserBooksUseCase,
    private val refreshLibraryUseCase: RefreshLibraryUseCase,
    private val applicationScope: ApplicationScope,
    appDispatchers: AppDispatchers,
    flows: List<LibraryVisibilityCollector>,
) : ToadScreenModel<
    LibraryVisibilitySettingsUiState,
    LibraryVisibilitySettingsEvent,
    LibraryVisibilitySettingsDependencies,
    LibraryVisibilityCollector,
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
        getAllUserBooksUseCase = getAllUserBooksUseCase,
        getCurrentlyReadingUserBooksUseCase = getCurrentlyReadingUserBooksUseCase,
        getWantToReadUserBooksUseCase = getWantToReadUserBooksUseCase,
        getReadUserBooksUseCase = getReadUserBooksUseCase,
        getDidNotFinishUserBooksUseCase = getDidNotFinishUserBooksUseCase,
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
