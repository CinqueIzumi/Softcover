package nl.rhaydus.softcover.core.domain.model

sealed interface RefreshScope {

    data object All : RefreshScope

    data class ByStatuses(val statuses: Set<UserBookStatus>) : RefreshScope

    data class ByList(val listId: Int) : RefreshScope
}
