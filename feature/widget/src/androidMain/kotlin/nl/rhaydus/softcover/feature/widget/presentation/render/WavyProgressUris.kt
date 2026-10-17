package nl.rhaydus.softcover.feature.widget.presentation.render

import android.net.Uri

/** The stored form of [WavyProgressBitmaps], which the widget hands to Glance by [Uri]. */
internal class WavyProgressUris(
    val track: Uri,
    val indicator: Uri,
)
