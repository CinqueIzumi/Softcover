package nl.rhaydus.softcover.feature.library.presentation.model

import nl.rhaydus.softcover.core.domain.model.UserBookStatus

sealed class LibraryTab(
    val id: String,
    val label: String,
) {
    data object All : LibraryTab(
        id = "all",
        label = "All",
    )

    class Status(
        val status: UserBookStatus,
        label: String,
    ) : LibraryTab(
        id = "status-${status.code}",
        label = label,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Status) return false
            return status == other.status && label == other.label
        }

        override fun hashCode(): Int = 31 * status.hashCode() + label.hashCode()

        companion object {
            fun labelFor(status: UserBookStatus): String = when (status) {
                UserBookStatus.WANT_TO_READ -> "Want to Read"
                UserBookStatus.CURRENTLY_READING -> "Currently Reading"
                UserBookStatus.READ -> "Read"
                UserBookStatus.PAUSED -> "Paused"
                UserBookStatus.DID_NOT_FINISH -> "Did Not Finish"
            }

            fun of(status: UserBookStatus) = Status(status = status, label = labelFor(status))
        }
    }

    data class CustomList(
        val listId: Int,
        val listName: String,
    ) : LibraryTab(
        id = "list-$listId",
        label = listName,
    )
}
