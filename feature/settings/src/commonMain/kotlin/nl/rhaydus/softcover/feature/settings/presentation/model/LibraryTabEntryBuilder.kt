package nl.rhaydus.softcover.feature.settings.presentation.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab

private val defaultStatusOrder: List<UserBookStatus> = listOf(
    UserBookStatus.CURRENTLY_READING,
    UserBookStatus.WANT_TO_READ,
    UserBookStatus.READ,
    UserBookStatus.DID_NOT_FINISH,
)

internal fun buildLibraryTabEntries(
    statusCounts: Map<Int, Int>,
    totalCount: Int,
    availableLists: List<BookList>,
    draftEnabledStatusCodes: Set<Int>,
    draftEnabledListIds: Set<Int>,
    draftTabOrder: List<String>,
): ImmutableList<LibraryTabEntry> {
    val statusEntries: List<LibraryTabEntry> = defaultStatusOrder
        .map { status ->
            val label = LibraryTab.Status.labelFor(status = status)

            LibraryTabEntry.Status(
                status = status,
                statusLabel = label,
                statusCount = statusCounts[status.code] ?: 0,
                statusVisibilityToggle = if (status.isAlwaysVisibleInLibrary) {
                    null
                } else {
                    libraryVisibilityToggleFor(
                        label = label,
                        visible = status.code in draftEnabledStatusCodes,
                    )
                },
            )
        }

    val listEntries: List<LibraryTabEntry> = availableLists
        .sortedBy { it.name.lowercase() }
        .map { list ->
            LibraryTabEntry.CustomList(
                listId = list.id,
                listName = list.name,
                listCount = list.books.size,
                listVisibilityToggle = libraryVisibilityToggleFor(
                    label = list.name,
                    visible = list.id in draftEnabledListIds,
                ),
            )
        }

    val reorderableUniverse: List<LibraryTabEntry> = statusEntries + listEntries

    val reorderableOrdered = if (draftTabOrder.isEmpty()) {
        reorderableUniverse
    } else {
        val byId = reorderableUniverse.associateBy { it.id }
        val ordered = draftTabOrder
            .distinct()
            .mapNotNull { byId[it] }
        val orderedIds = ordered.map { it.id }.toSet()
        val appended = reorderableUniverse.filter { it.id !in orderedIds }

        ordered + appended
    }

    return (listOf<LibraryTabEntry>(LibraryTabEntry.All(totalCount = totalCount)) + reorderableOrdered)
        .toImmutableList()
}
