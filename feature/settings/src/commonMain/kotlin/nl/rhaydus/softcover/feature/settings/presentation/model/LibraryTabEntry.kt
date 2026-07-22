package nl.rhaydus.softcover.feature.settings.presentation.model

import nl.rhaydus.softcover.core.domain.model.UserBookStatus

internal sealed class LibraryTabEntry(
    val id: String,
    val label: String,
    val count: Int,
    val isReorderable: Boolean,
    val canHide: Boolean,
    val isList: Boolean,
) {
    data class All(
        val totalCount: Int,
    ) : LibraryTabEntry(
        id = ALL_ID,
        label = "All",
        count = totalCount,
        isReorderable = false,
        canHide = false,
        isList = false,
    )

    data class Status(
        val status: UserBookStatus,
        val statusLabel: String,
        val statusCount: Int,
    ) : LibraryTabEntry(
        id = "status-${status.code}",
        label = statusLabel,
        count = statusCount,
        isReorderable = true,
        canHide = status.isAlwaysVisibleInLibrary.not(),
        isList = false,
    )

    data class CustomList(
        val listId: Int,
        val listName: String,
        val listCount: Int,
    ) : LibraryTabEntry(
        id = "list-$listId",
        label = listName,
        count = listCount,
        isReorderable = true,
        canHide = true,
        isList = true,
    )

    companion object {
        const val ALL_ID: String = "all"
    }
}
