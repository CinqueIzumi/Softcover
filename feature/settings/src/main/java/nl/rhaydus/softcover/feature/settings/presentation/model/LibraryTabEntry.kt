package nl.rhaydus.softcover.feature.settings.presentation.model

import nl.rhaydus.softcover.core.domain.model.UserBookStatus

internal sealed class LibraryTabEntry(
    val id: String,
    val label: String,
) {
    data class Status(
        val status: UserBookStatus,
        val statusLabel: String,
    ) : LibraryTabEntry(
        id = "status-${status.code}",
        label = statusLabel,
    ) {
        val isAlwaysOn: Boolean = status.isAlwaysVisibleInLibrary
    }

    data class CustomList(
        val listId: Int,
        val listName: String,
    ) : LibraryTabEntry(
        id = "list-$listId",
        label = listName,
    )
}
