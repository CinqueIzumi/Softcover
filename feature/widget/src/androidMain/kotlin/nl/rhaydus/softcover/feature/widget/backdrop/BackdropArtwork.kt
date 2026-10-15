package nl.rhaydus.softcover.feature.widget.backdrop

import android.graphics.Bitmap

/** The pre-rendered images behind the shown book; backdrops are `null` while the book has no cover. */
internal class BackdropArtwork(
    val compactBackdrop: Bitmap?,
    val tallBackdrop: Bitmap?,
    val gradients: GradientMasks,
)
