package nl.rhaydus.softcover.feature.settings.presentation.state

import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.toad.UiState

data class LibraryVisibilitySettingsUiState(
    val persistedEnabledStatusCodes: Set<Int> = emptySet(),
    val persistedEnabledListIds: Set<Int> = emptySet(),
    val draftEnabledStatusCodes: Set<Int> = emptySet(),
    val draftEnabledListIds: Set<Int> = emptySet(),
    val initialized: Boolean = false,
    val isSaving: Boolean = false,
    val availableLists: List<BookList> = emptyList(),
    val togglableStatuses: List<UserBookStatus> = UserBookStatus.togglableInLibrary,
) : UiState {
    val isDirty: Boolean
        get() = initialized && (
            draftEnabledStatusCodes != persistedEnabledStatusCodes ||
                draftEnabledListIds != persistedEnabledListIds
            )
}
