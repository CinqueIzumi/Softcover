package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

class OnEnterRearrangeModeAction : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        // Rearrange and selection are mutually exclusive editing modes — clear any active selection
        // so the two never co-exist (the control strip only offers Rearrange when not selecting, but
        // we guard here too so the invariant holds regardless of entry path).
        scope.setState {
            it.copy(
                isRearranging = true,
                selectionMode = false,
                selectedBookIds = emptySet(),
            )
        }
    }
}
