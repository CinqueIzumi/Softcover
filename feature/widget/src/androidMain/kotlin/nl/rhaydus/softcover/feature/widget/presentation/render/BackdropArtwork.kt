package nl.rhaydus.softcover.feature.widget.presentation.render

import android.graphics.Bitmap

/** The pre-rendered images behind the shown book; backdrops are `null` while the book has no cover. */
internal class BackdropArtwork(
    val compactBackdrop: Bitmap?,
    val tallBackdrop: Bitmap?,
    val gradients: GradientMasks,
)
