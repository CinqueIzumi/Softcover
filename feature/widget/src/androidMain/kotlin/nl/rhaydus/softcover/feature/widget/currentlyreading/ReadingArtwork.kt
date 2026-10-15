package nl.rhaydus.softcover.feature.widget.currentlyreading

import android.graphics.Bitmap
import nl.rhaydus.softcover.feature.widget.backdrop.GradientMasks

/** The pre-rendered images behind the shown book; backdrops are `null` while the book has no cover. */
internal class ReadingArtwork(
    val compactBackdrop: Bitmap?,
    val tallBackdrop: Bitmap?,
    val gradients: GradientMasks,
)
