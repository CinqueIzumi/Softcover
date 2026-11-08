package nl.rhaydus.softcover.core.presentation.model

import nl.rhaydus.softcover.core.domain.model.BookList

fun <T> List<T>.inLibraryTabOrder(
    savedOrder: List<String>,
    tabId: (T) -> String,
): List<T> {
    if (savedOrder.isEmpty()) return this

    val byId = associateBy(tabId)
    val placed = savedOrder.distinct().mapNotNull { byId[it] }
    val placedIds = placed.map(tabId).toSet()

    return placed + filter { tabId(it) !in placedIds }
}

fun List<BookList>.inLibraryTabOrder(savedOrder: List<String>): List<BookList> =
    sortedBy { it.name.lowercase() }
        .inLibraryTabOrder(savedOrder) { LibraryTab.CustomList.idFor(it.id) }
