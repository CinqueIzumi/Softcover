package nl.rhaydus.softcover.feature.library.presentation.action

import kotlinx.collections.immutable.persistentSetOf
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.withSelectedBookIds
import nl.rhaydus.toad.ActionScope

internal class OnEnterRearrangeModeAction : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        // Rearrange and selection are mutually exclusive editing modes — clear any active selection
        // so the two never co-exist (the control strip only offers Rearrange when not selecting, but
        // we guard here too so the invariant holds regardless of entry path).
        scope.setState {
            it.withSelectedBookIds(persistentSetOf()).copy(
                isRearranging = true,
                selectionMode = false,
            )
        }
    }
}
