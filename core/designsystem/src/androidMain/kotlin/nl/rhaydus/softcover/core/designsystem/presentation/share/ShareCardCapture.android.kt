package nl.rhaydus.softcover.core.designsystem.presentation.share

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import java.io.File

internal class AndroidShareCardCapture internal constructor(
    override val graphicsLayer: GraphicsLayer,
    private val context: Context,
    private val appDispatchers: AppDispatchers,
) : ShareCardCapture {
    // TODO: capture density follows the host device; revisit for a fixed-pixel export when share intents land.
    override suspend fun saveToGallery(displayName: String): SaveOutcome {
        val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
        val filename = buildFilename(displayName = displayName)

        return withContext(appDispatchers.io) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveViaScopedStorage(
                    bitmap,
                    filename,
                )
            } else {
                saveViaLegacyStorage(
                    bitmap,
                    filename,
                )
            }
        }
    }

    override suspend fun saveToCache(displayName: String): SaveOutcome.Cached {
        val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
        val filename = buildFilename(displayName = displayName)

        return withContext(appDispatchers.io) {
            val shareDir = File(
                context.cacheDir,
                SHARE_CACHE_FOLDER,
            ).apply { mkdirs() }

            shareDir.listFiles()?.forEach { it.delete() }

            val file = File(
                shareDir,
                filename,
            )
            file.outputStream().use { stream ->
                bitmap.compress(
                    Bitmap.CompressFormat.PNG, /* quality = */
                    100,
                    stream,
                )
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.shareprovider",
                file,
            )

            SaveOutcome.Cached(identifier = uri.toString())
        }
    }

    private fun buildFilename(displayName: String): String {
        val sanitized = displayName.replace(
            Regex("[^A-Za-z0-9-_]"),
            "-",
        )

        return "softcover-$sanitized-${System.currentTimeMillis()}.png"
    }

    private fun saveViaScopedStorage(
        bitmap: Bitmap,
        filename: String,
    ): SaveOutcome {
        val resolver = context.contentResolver

        val values = ContentValues().apply {
            put(
                MediaStore.MediaColumns.DISPLAY_NAME,
                filename,
            )
            put(
                MediaStore.MediaColumns.MIME_TYPE,
                "image/png",
            )
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                "$RELATIVE_PICTURES_PATH/$GALLERY_FOLDER",
            )
            put(
                MediaStore.MediaColumns.IS_PENDING,
                1,
            )
        }

        val uri = resolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values,
        )
            ?: error("MediaStore insert returned null")

        resolver.openOutputStream(uri).use { stream ->
            requireNotNull(stream) { "openOutputStream returned null for $uri" }

            bitmap.compress(
                Bitmap.CompressFormat.PNG, /* quality = */
                100,
                stream,
            )
        }

        values.clear()
        values.put(
            MediaStore.MediaColumns.IS_PENDING,
            0,
        )
        resolver.update(
            uri,
            values,
            null,
            null,
        )

        return SaveOutcome.Saved(
            identifier = uri.toString(),
            displayPath = "$RELATIVE_PICTURES_PATH/$GALLERY_FOLDER/$filename",
        )
    }

    @Suppress("DEPRECATION")
    private fun saveViaLegacyStorage(
        bitmap: Bitmap,
        filename: String,
    ): SaveOutcome {
        val picturesDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            GALLERY_FOLDER,
        )

        picturesDir.mkdirs()

        val file = File(
            picturesDir,
            filename,
        )
        file.outputStream().use { stream ->
            bitmap.compress(
                Bitmap.CompressFormat.PNG, /* quality = */
                100,
                stream,
            )
        }

        val values = ContentValues().apply {
            put(
                MediaStore.Images.Media.DATA,
                file.absolutePath,
            )
            put(
                MediaStore.Images.Media.MIME_TYPE,
                "image/png",
            )
            put(
                MediaStore.Images.Media.DISPLAY_NAME,
                filename,
            )
        }

        val uri = context.contentResolver
            .insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values,
            )
            ?: Uri.fromFile(file)

        return SaveOutcome.Saved(
            identifier = uri.toString(),
            displayPath = file.absolutePath,
        )
    }

    private companion object {
        const val GALLERY_FOLDER = "Softcover"
        const val SHARE_CACHE_FOLDER = "share"

        val RELATIVE_PICTURES_PATH: String = Environment.DIRECTORY_PICTURES
    }
}

@Composable
actual fun rememberShareCardCapture(): ShareCardCapture {
    val graphicsLayer = rememberGraphicsLayer()
    val context = LocalContext.current
    val appDispatchers = koinInject<AppDispatchers>()

    return remember(graphicsLayer, context, appDispatchers) {
        AndroidShareCardCapture(
            graphicsLayer,
            context,
            appDispatchers,
        )
    }
}
