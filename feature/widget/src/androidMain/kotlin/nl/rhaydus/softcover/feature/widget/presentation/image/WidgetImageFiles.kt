package nl.rhaydus.softcover.feature.widget.presentation.image

import android.content.Context
import java.io.File

internal object WidgetImageFiles {
    const val DIRECTORY_NAME = "widget-images"

    const val EXTENSION = ".png"

    val NAME_PATTERN = Regex("^[0-9a-f]{64}\\.png$")

    fun directory(context: Context): File = File(
        context.filesDir,
        DIRECTORY_NAME,
    )

    fun authority(context: Context): String = "${context.packageName}.widgetimages"
}
