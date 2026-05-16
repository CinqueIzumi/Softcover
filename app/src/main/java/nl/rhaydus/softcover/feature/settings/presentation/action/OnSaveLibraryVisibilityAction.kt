package nl.rhaydus.softcover.feature.settings.presentation.action

import kotlinx.coroutines.launch
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.settings.presentation.event.LibraryVisibilitySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import timber.log.Timber

class OnSaveLibraryVisibilityAction : LibraryVisibilityAction {
    override suspend fun execute(
        dependencies: LibraryVisibilitySettingsDependencies,
        scope: ActionScope<LibraryVisibilitySettingsUiState, LibraryVisibilitySettingsEvent, LibraryVisibilitySettingsLocalVariables>,
    ) {
        val current = scope.currentState

        if (current.isDirty.not() || current.isSaving) return

        scope.setState { it.copy(isSaving = true) }

        dependencies.applicationScope.scope.launch {
            dependencies.setEnabledStatusCodesUseCase(codes = current.draftEnabledStatusCodes).onFailure {
                Timber.e("-=- $it")
            }

            dependencies.setEnabledListIdsUseCase(ids = current.draftEnabledListIds).onFailure {
                Timber.e("-=- $it")
            }

            dependencies.setLibraryTabOrderUseCase(order = current.draftTabOrder).onFailure {
                Timber.e("-=- $it")
            }

            dependencies.refreshUserBooksUseCase().onFailure { Timber.e("-=- $it") }

            scope.setState { it.copy(isSaving = false) }
        }
    }
}
