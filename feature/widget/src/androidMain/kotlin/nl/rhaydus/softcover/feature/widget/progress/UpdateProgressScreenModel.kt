package nl.rhaydus.softcover.feature.widget.progress

import android.content.Context
import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RecordBookProgressUseCase
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetLastUsedProgressUnitAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetLastUsedProgressUnitUseCase
import nl.rhaydus.softcover.core.presentation.navigation.AppEntryPoint
import nl.rhaydus.toad.ToadScreenModel

internal class UpdateProgressScreenModel(
    context: Context,
    getUserIdAsFlow: GetUserIdAsFlowUseCase,
    getCurrentlyReadingUserBooks: GetCurrentlyReadingUserBooksUseCase,
    recordBookProgress: RecordBookProgressUseCase,
    markBookAsRead: MarkBookAsReadUseCase,
    getLastUsedProgressUnitAsFlow: GetLastUsedProgressUnitAsFlowUseCase,
    setLastUsedProgressUnit: SetLastUsedProgressUnitUseCase,
    getThemeConfiguration: GetThemeConfigurationUseCase,
    appEntryPoint: AppEntryPoint,
    appDispatchers: AppDispatchers,
    flows: List<UpdateProgressCollector>,
) : ToadScreenModel<
    UpdateProgressUiState,
    UpdateProgressEvent,
    UpdateProgressDependencies,
    UpdateProgressCollector,
    UpdateProgressLocalVariables,
    >(
    initialState = UpdateProgressUiState(),
    initialLocalVariables = UpdateProgressLocalVariables(),
    initializers = flows,
) {
    override val dependencies = UpdateProgressDependencies(
        context = context,
        getUserIdAsFlow = getUserIdAsFlow,
        getCurrentlyReadingUserBooks = getCurrentlyReadingUserBooks,
        recordBookProgress = recordBookProgress,
        markBookAsRead = markBookAsRead,
        getLastUsedProgressUnitAsFlow = getLastUsedProgressUnitAsFlow,
        setLastUsedProgressUnit = setLastUsedProgressUnit,
        getThemeConfiguration = getThemeConfiguration,
        appEntryPoint = appEntryPoint,
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
    )

    init {
        startInitializers()
    }

    fun runAction(action: UpdateProgressAction) = dispatch(action = action)
}
