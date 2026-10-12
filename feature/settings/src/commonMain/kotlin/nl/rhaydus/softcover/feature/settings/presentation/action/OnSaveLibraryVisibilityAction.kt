package nl.rhaydus.softcover.feature.settings.presentation.action

import kotlinx.coroutines.async
import nl.rhaydus.common.AppLog
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.component.sheet.isOpen
import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.core.component.sheet.withModel
import nl.rhaydus.softcover.core.presentation.error.onApiFailure
import nl.rhaydus.softcover.feature.settings.presentation.event.LibraryVisibilitySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.LeaveScreen
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import nl.rhaydus.softcover.feature.settings.presentation.state.discardSheetScaffold
import nl.rhaydus.toad.ActionScope

internal class OnSaveLibraryVisibilityAction(
    private val leaveAfterSave: Boolean = false,
) : LibraryVisibilityAction {
    override suspend fun execute(
        dependencies: LibraryVisibilitySettingsDependencies,
        scope: ActionScope<LibraryVisibilitySettingsUiState, LibraryVisibilitySettingsEvent, LibraryVisibilitySettingsLocalVariables>,
    ) {
        val current = scope.currentState

        if (current.isDirty.not() || current.isSaving) {
            if (leaveAfterSave) {
                scope.setState {
                    it.copy(discardSheet = it.discardSheet.requestClose(pending = LeaveScreen))
                }
            }

            return
        }

        scope.setState {
            it.copy(
                isSaving = true,
                discardSheet = it.discardSheet.withModel { discardSheetScaffold(saving = leaveAfterSave) },
            )
        }

        val save = dependencies.applicationScope.scope.async {
            val results = listOf(
                dependencies.setEnabledStatusCodesUseCase(codes = current.draftEnabledStatusCodes),
                dependencies.setEnabledListIdsUseCase(ids = current.draftEnabledListIds),
                dependencies.setLibraryTabOrderUseCase(order = current.draftTabOrder),
            )

            results.forEach { result -> result.onFailure { AppLog.e("$it") } }

            if (results.any { it.isSuccess }) {
                dependencies.refreshLibraryUseCase().onApiFailure()
            }

            val allSaved = results.all { it.isSuccess }

            if (allSaved.not()) {
                SnackBarManager.showSnackbar(title = "Couldn't save your tabs. Try again.")

                scope.sendEvent(event = LibraryVisibilitySettingsEvent.SaveFailed)
            }

            scope.setState {
                it.copy(
                    isSaving = false,
                    discardSheet = it.discardSheet.withModel { discardSheetScaffold(saving = false) },
                )
            }

            allSaved
        }

        if (leaveAfterSave.not()) return

        val allSaved = save.await()

        scope.setState {
            if (it.discardSheet.isOpen.not()) return@setState it

            it.copy(
                discardSheet = it.discardSheet.requestClose(pending = LeaveScreen.takeIf { allSaved }),
            )
        }
    }
}
