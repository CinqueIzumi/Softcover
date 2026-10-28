package nl.rhaydus.softcover.feature.library.presentation.state

internal fun LibraryFilterValue.chipKey(): String = when (this) {
    is LibraryFilterValue.HardcoverTag -> "hardcover:${direction.keyPart()}:${key.id}"
    is LibraryFilterValue.YourTag -> "yours:${direction.keyPart()}:${key.category.name}:${key.name}"
    is LibraryFilterValue.Format -> "format:$value"
    is LibraryFilterValue.ReleaseYear -> "releaseYear:$year"
    is LibraryFilterValue.ReadYear -> "readYear:$year"
    is LibraryFilterValue.Owned -> "owned:$owned"
    is LibraryFilterValue.RatingMin -> "rating:$threshold"
}

private fun TagDirection.keyPart(): String = when (this) {
    TagDirection.INCLUDE -> "include"
    TagDirection.EXCLUDE -> "exclude"
}
