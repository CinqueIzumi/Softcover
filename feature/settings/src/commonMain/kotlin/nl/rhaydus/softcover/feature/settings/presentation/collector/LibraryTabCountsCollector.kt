package nl.rhaydus.softcover.feature.settings.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.settings.presentation.event.LibraryVisibilitySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import nl.rhaydus.toad.ActionScope

internal class LibraryTabCountsCollector : LibraryVisibilityCollector {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryVisibilitySettingsUiState, LibraryVisibilitySettingsEvent, LibraryVisibilitySettingsLocalVariables>,
        dependencies: LibraryVisibilitySettingsDependencies,
    ) {
        combine(
            dependencies.getAllUserBooksUseCase(),
            dependencies.getCurrentlyReadingUserBooksUseCase(),
            dependencies.getWantToReadUserBooksUseCase(),
            dependencies.getReadUserBooksUseCase(),
            dependencies.getDidNotFinishUserBooksUseCase(),
        ) { all, currentlyReading, wantToRead, read, didNotFinish ->
            val statusCounts: Map<Int, Int> = mapOf(
                UserBookStatus.CURRENTLY_READING.code to currentlyReading.size,
                UserBookStatus.WANT_TO_READ.code to wantToRead.size,
                UserBookStatus.READ.code to read.size,
                UserBookStatus.DID_NOT_FINISH.code to didNotFinish.size,
            )

            all.size to statusCounts
        }.collectLatest { (totalCount, statusCounts) ->
            scope.setState { state ->
                state.copy(
                    totalCount = totalCount,
                    statusCounts = statusCounts,
                )
            }
        }
    }
}
