package nl.rhaydus.softcover.feature.widget.presentation.model

import android.net.Uri

/** [backdrop] is `null` while the book has no cover. */
internal data class BookArtworkUiModel(
    val backdrop: Uri?,
    val veil: Uri,
    val veilLift: Uri,
    val washPrimary: Uri,
    val washTertiary: Uri,
)
