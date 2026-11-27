package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.graphics.Bitmap

internal class TrendingCover(
    val key: String,
    val render: suspend () -> Bitmap?,
)
