package nl.rhaydus.softcover.feature.library.presentation.state

internal fun LibraryFilterValue.chipKey(): String = when (this) {
    is LibraryFilterValue.Tag -> "tag:${tag.id}"
    is LibraryFilterValue.Format -> "format:$value"
    is LibraryFilterValue.ReleaseYear -> "releaseYear:$year"
    is LibraryFilterValue.ReadYear -> "readYear:$year"
    is LibraryFilterValue.Owned -> "owned:$owned"
    is LibraryFilterValue.RatingMin -> "rating:$threshold"
}
