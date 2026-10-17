package nl.rhaydus.softcover.feature.widget.presentation.render

import android.graphics.Bitmap

/** The pre-rendered images behind the shown book; the backdrop is `null` while the book has no cover. */
internal class BackdropArtwork(
    val backdrop: Bitmap?,
    val gradients: GradientMasks,
)
