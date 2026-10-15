package nl.rhaydus.softcover.core.uibinding.cover

import android.content.Context
import android.graphics.Bitmap
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import nl.rhaydus.softcover.core.component.cover.CoverSource

/**
 * Loads [source] through the app's singleton Coil loader as a software [Bitmap] downsampled to fit
 * [sizePx], or `null` when [source] is `null` or the load fails.
 *
 * Software, because the consumers (notification large icon, `RemoteViews` / Glance image providers)
 * cross a process boundary that cannot carry a hardware bitmap.
 */
suspend fun loadCoverBitmap(
    context: Context,
    source: CoverSource?,
    sizePx: Int,
): Bitmap? {
    val data = when (source) {
        is CoverSource.Local -> source.path
        is CoverSource.Remote -> source.url
        null -> return null
    }

    val request = ImageRequest.Builder(context)
        .data(data)
        .allowHardware(false)
        .size(sizePx)
        .build()

    val result = SingletonImageLoader.get(context).execute(request)

    return (result as? SuccessResult)?.image?.toBitmap()
}
