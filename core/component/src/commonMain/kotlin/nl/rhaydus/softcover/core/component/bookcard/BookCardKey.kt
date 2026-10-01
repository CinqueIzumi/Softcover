package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.runtime.Immutable

@Immutable
sealed interface BookCardKey {
    data class Book(val bookId: Int) : BookCardKey

    data class Edition(val editionId: Int) : BookCardKey

    data class Series(val seriesId: Int) : BookCardKey
}
