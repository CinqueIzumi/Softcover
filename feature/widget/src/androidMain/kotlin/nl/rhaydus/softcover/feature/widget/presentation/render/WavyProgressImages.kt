package nl.rhaydus.softcover.feature.widget.presentation.render

import android.graphics.Bitmap

/** The stored form of [WavyProgressBitmaps], which the widget hands to Glance as bitmaps. */
internal class WavyProgressImages(
    val track: Bitmap,
    val indicator: Bitmap,
)
