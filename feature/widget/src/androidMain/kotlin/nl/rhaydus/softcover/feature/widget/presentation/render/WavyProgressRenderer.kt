package nl.rhaydus.softcover.feature.widget.presentation.render

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

internal fun renderWavyProgress(
    widthPx: Int,
    heightPx: Int,
    density: Float,
    fraction: Float,
): WavyProgressBitmaps {
    val geometry = wavyProgressGeometry(
        widthPx = widthPx.toFloat(),
        density = density,
        fraction = fraction,
    )

    return WavyProgressBitmaps(
        track = drawMask(
            widthPx = widthPx,
            heightPx = heightPx,
            density = density,
        ) { drawTrack(geometry) },
        indicator = drawMask(
            widthPx = widthPx,
            heightPx = heightPx,
            density = density,
        ) { drawIndicator(geometry) },
    )
}

private fun drawMask(
    widthPx: Int,
    heightPx: Int,
    density: Float,
    draw: DrawScope.() -> Unit,
): Bitmap {
    val image = ImageBitmap(
        width = widthPx,
        height = heightPx,
        config = ImageBitmapConfig.Argb8888,
    )

    CanvasDrawScope().draw(
        density = Density(density = density),
        layoutDirection = LayoutDirection.Ltr,
        canvas = Canvas(image),
        size = Size(
            width = widthPx.toFloat(),
            height = heightPx.toFloat(),
        ),
        block = draw,
    )

    return image.asAndroidBitmap()
}

private fun DrawScope.drawTrack(geometry: WavyProgressGeometry) {
    if (geometry.hasTrack.not()) return

    val centerY = size.height / 2f

    val path = Path().apply {
        moveTo(
            x = geometry.trackStart,
            y = centerY,
        )
        lineTo(
            x = geometry.trackEnd,
            y = centerY,
        )
    }

    drawPath(
        path = path,
        color = Color.White,
        style = Stroke(
            width = geometry.strokeWidth,
            cap = StrokeCap.Round,
        ),
    )
}

private fun DrawScope.drawIndicator(geometry: WavyProgressGeometry) {
    if (geometry.hasIndicator) {
        drawPath(
            path = indicatorPath(geometry),
            color = Color.White,
            style = Stroke(
                width = geometry.strokeWidth,
                cap = StrokeCap.Round,
            ),
        )
    }

    if (geometry.stopRadius > 0f) {
        drawCircle(
            color = Color.White,
            radius = geometry.stopRadius,
            center = Offset(
                x = geometry.stopCenterX,
                y = size.height / 2f,
            ),
        )
    }
}

private fun DrawScope.indicatorPath(geometry: WavyProgressGeometry): Path {
    val centerY = size.height / 2f

    if (geometry.isWavy.not()) {
        return Path().apply {
            moveTo(
                x = geometry.indicatorStart,
                y = centerY,
            )
            lineTo(
                x = geometry.indicatorEnd,
                y = centerY,
            )
        }
    }

    val full = Path()
    val halfWavelength = geometry.wavelength / 2f
    val reach = size.width + geometry.wavelength * 2f

    var anchorX = halfWavelength
    var controlX = halfWavelength / 2f
    var controlY = size.height - geometry.strokeWidth

    full.moveTo(
        x = 0f,
        y = 0f,
    )

    while (anchorX <= reach) {
        full.quadraticTo(
            x1 = controlX,
            y1 = controlY,
            x2 = anchorX,
            y2 = 0f,
        )

        anchorX += halfWavelength
        controlX += halfWavelength
        controlY *= -1f
    }

    full.translate(
        Offset(
            x = 0f,
            y = centerY,
        ),
    )

    val measure = PathMeasure().apply {
        setPath(
            path = full,
            forceClosed = false,
        )
    }

    val scale = measure.length / (full.getBounds().width + 0.00000001f)
    val segment = Path()

    measure.getSegment(
        startDistance = geometry.indicatorStart * scale,
        stopDistance = geometry.indicatorEnd * scale,
        destination = segment,
        startWithMoveTo = true,
    )

    return segment
}
