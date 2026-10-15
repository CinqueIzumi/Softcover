package nl.rhaydus.softcover.feature.widget.progress

import android.graphics.Bitmap

/** White alpha masks; the widget tints each with its theme role so one render serves every colour mode. */
internal class WavyProgressBitmaps(
    val track: Bitmap,
    val indicator: Bitmap,
)
