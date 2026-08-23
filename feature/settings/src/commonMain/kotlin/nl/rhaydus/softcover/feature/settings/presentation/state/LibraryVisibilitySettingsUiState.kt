package nl.rhaydus.softcover.feature.settings.presentation.state

import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.settings.presentation.model.LibraryTabEntry
import nl.rhaydus.toad.UiState

internal data class LibraryVisibilitySettingsUiState(
    val persistedEnabledStatusCodes: Set<Int> = emptySet(),
    val persistedEnabledListIds: Set<Int> = emptySet(),
    val persistedTabOrder: List<String> = emptyList(),
    val draftEnabledStatusCodes: Set<Int> = emptySet(),
    val draftEnabledListIds: Set<Int> = emptySet(),
    val draftTabOrder: List<String> = emptyList(),
    val initialized: Boolean = false,
    val isSaving: Boolean = false,
    val availableLists: List<BookList> = emptyList(),
    val statusCounts: Map<Int, Int> = emptyMap(),
    val totalCount: Int = 0,
) : UiState {
    val isDirty: Boolean
        get() = initialized && (
            draftEnabledStatusCodes != persistedEnabledStatusCodes ||
                draftEnabledListIds != persistedEnabledListIds ||
                draftTabOrder != persistedTabOrder
            )

    val orderedEntries: List<LibraryTabEntry>
        get() {
            val statusEntries: List<LibraryTabEntry> = defaultStatusOrder
                .map { status ->
                    LibraryTabEntry.Status(
                        status = status,
                        statusLabel = LibraryTab.Status.labelFor(status = status),
                        statusCount = statusCounts[status.code] ?: 0,
                    )
                }

            val listEntries: List<LibraryTabEntry> = availableLists
                .sortedBy { it.name.lowercase() }
                .map { list ->
                    LibraryTabEntry.CustomList(
                        listId = list.id,
                        listName = list.name,
                        listCount = list.books.size,
                    )
                }

            val reorderableUniverse: List<LibraryTabEntry> = statusEntries + listEntries

            val reorderableOrdered = if (draftTabOrder.isEmpty()) {
                reorderableUniverse
            } else {
                val byId = reorderableUniverse.associateBy { it.id }
                val ordered = draftTabOrder.mapNotNull { byId[it] }
                val orderedIds = ordered.map { it.id }.toSet()
                val appended = reorderableUniverse.filter { it.id !in orderedIds }

                ordered + appended
            }

            val allEntry: LibraryTabEntry = LibraryTabEntry.All(totalCount = totalCount)

            return listOf(allEntry) + reorderableOrdered
        }

    private companion object {
        val defaultStatusOrder: List<UserBookStatus> = listOf(
            UserBookStatus.CURRENTLY_READING,
            UserBookStatus.WANT_TO_READ,
            UserBookStatus.READ,
            UserBookStatus.DID_NOT_FINISH,
        )
    }
}
