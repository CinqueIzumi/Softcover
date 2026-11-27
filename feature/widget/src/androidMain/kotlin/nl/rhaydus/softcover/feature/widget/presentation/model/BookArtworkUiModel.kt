package nl.rhaydus.softcover.feature.widget.presentation.model

import android.graphics.Bitmap

/** [backdrop] is `null` while the book has no cover. */
internal data class BookArtworkUiModel(
    val backdrop: Bitmap?,
    val veil: Bitmap,
    val veilLift: Bitmap,
    val washPrimary: Bitmap,
    val washTertiary: Bitmap,
)
