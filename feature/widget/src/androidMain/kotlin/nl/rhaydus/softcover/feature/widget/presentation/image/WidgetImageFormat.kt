package nl.rhaydus.softcover.feature.widget.presentation.image

import android.graphics.Bitmap
import android.os.Build
import java.io.OutputStream

internal enum class WidgetImageFormat(
    val extension: String,
    val mimeType: String,
) {
    /** Lossy; for opaque photos such as covers and backdrops. */
    Photo(
        extension = ".webp",
        mimeType = "image/webp",
    ),

    /** Lossless; for images whose alpha must survive exactly, such as masks and veils. */
    Lossless(
        extension = ".png",
        mimeType = "image/png",
    ),
    ;

    @Suppress("DEPRECATION")
    fun compress(
        bitmap: Bitmap,
        output: OutputStream,
    ): Boolean = when (this) {
        Photo -> bitmap.compress(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                Bitmap.CompressFormat.WEBP
            },
            PHOTO_QUALITY,
            output,
        )

        Lossless -> bitmap.compress(
            Bitmap.CompressFormat.PNG,
            100,
            output,
        )
    }

    companion object {
        private const val PHOTO_QUALITY = 85

        fun forFileName(name: String): WidgetImageFormat? = entries.firstOrNull { name.endsWith(it.extension) }
    }
}
