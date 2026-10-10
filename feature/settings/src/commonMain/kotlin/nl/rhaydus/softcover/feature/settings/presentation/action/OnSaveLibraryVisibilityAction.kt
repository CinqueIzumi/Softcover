package nl.rhaydus.softcover.feature.settings.presentation.action

import kotlinx.coroutines.async
import nl.rhaydus.common.AppLog
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.presentation.error.onApiFailure
import nl.rhaydus.softcover.feature.settings.presentation.event.LibraryVisibilitySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsDependencies
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
                    it.copy(
                        discardSheetScaffold = it.discardSheetScaffold.copy(closeRequested = true),
                        leaveOnSheetDismissed = true,
                    )
                }
            }

            return
        }

        scope.setState {
            it.copy(
                isSaving = true,
                discardSheetScaffold = discardSheetScaffold(saving = leaveAfterSave).copy(
                    closeRequested = it.discardSheetScaffold.closeRequested,
                ),
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
                SnackBarManager.showSnackbar(title = "Couldn't save your tabs — try again.")

                scope.sendEvent(event = LibraryVisibilitySettingsEvent.SaveFailed)
            }

            scope.setState {
                it.copy(
                    isSaving = false,
                    discardSheetScaffold = discardSheetScaffold(saving = false).copy(
                        closeRequested = it.discardSheetScaffold.closeRequested,
                    ),
                )
            }

            allSaved
        }

        if (leaveAfterSave.not()) return

        val allSaved = save.await()

        scope.setState {
            if (it.showDiscardSheet.not()) return@setState it

            it.copy(
                discardSheetScaffold = it.discardSheetScaffold.copy(closeRequested = true),
                leaveOnSheetDismissed = allSaved,
            )
        }
    }
}
