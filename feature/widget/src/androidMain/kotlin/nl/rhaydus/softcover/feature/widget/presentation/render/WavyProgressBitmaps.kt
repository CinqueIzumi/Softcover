package nl.rhaydus.softcover.feature.widget.presentation.render

import android.graphics.Bitmap
import android.net.Uri

/** White alpha masks; the widget tints each with its theme role so one render serves every colour mode. */
internal class WavyProgressBitmaps(
    val track: Bitmap,
    val indicator: Bitmap,
)

/** The stored form of [WavyProgressBitmaps], which the widget hands to Glance by [Uri]. */
internal class WavyProgressUris(
    val track: Uri,
    val indicator: Uri,
)
