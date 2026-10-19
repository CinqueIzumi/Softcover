package nl.rhaydus.softcover.feature.widget.presentation.util

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.SizeF
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/**
 * The sizes the launcher reports for a widget, matching what Glance's exact size mode composes. On API 31+ that is
 * the full list, else the portrait and landscape sizes when all four min and max values are set. Below API 31 it is
 * the portrait (min width, max height) and landscape (max width, min height) sizes, each accepted on its own.
 * When none is usable Glance composes once at [minSize].
 */
@Suppress("DEPRECATION")
internal fun Bundle.reportedWidgetSizes(minSize: () -> DpSize): List<DpSize> {
    val portrait = orientationSize(
        widthKey = AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,
        heightKey = AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,
    )

    val landscape = orientationSize(
        widthKey = AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,
        heightKey = AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,
    )

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        return listOfNotNull(
            portrait,
            landscape,
        ).ifEmpty { listOf(minSize()) }
    }

    val sizes = getParcelableArrayList<SizeF>(AppWidgetManager.OPTION_APPWIDGET_SIZES).orEmpty()

    if (sizes.isNotEmpty()) {
        return sizes.map { size ->
            DpSize(
                width = size.width.dp,
                height = size.height.dp,
            )
        }
    }

    if (portrait == null || landscape == null) return listOf(minSize())

    return listOf(
        portrait,
        landscape,
    )
}

private fun Bundle.orientationSize(
    widthKey: String,
    heightKey: String,
): DpSize? {
    val width = getInt(
        widthKey,
        0,
    )
    val height = getInt(
        heightKey,
        0,
    )

    if (width == 0 || height == 0) return null

    return DpSize(
        width = width.dp,
        height = height.dp,
    )
}

/**
 * The provider's smallest size as Glance computes it: the declared minimum, lowered to the minimum resize size on
 * each axis the widget can be resized along. [DpSize.Zero] when the widget is not registered.
 */
internal fun Context.providerMinSize(appWidgetId: Int): DpSize {
    val info = AppWidgetManager.getInstance(this).getAppWidgetInfo(appWidgetId) ?: return DpSize.Zero
    val density = resources.displayMetrics.density

    val width = if (info.resizeMode and AppWidgetProviderInfo.RESIZE_HORIZONTAL != 0) {
        minOf(
            info.minWidth,
            info.minResizeWidth,
        )
    } else {
        info.minWidth
    }

    val height = if (info.resizeMode and AppWidgetProviderInfo.RESIZE_VERTICAL != 0) {
        minOf(
            info.minHeight,
            info.minResizeHeight,
        )
    } else {
        info.minHeight
    }

    return DpSize(
        width = (width / density).dp,
        height = (height / density).dp,
    )
}
