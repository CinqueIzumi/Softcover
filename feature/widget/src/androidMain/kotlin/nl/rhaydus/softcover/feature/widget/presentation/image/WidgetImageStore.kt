package nl.rhaydus.softcover.feature.widget.presentation.image

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.net.toUri
import java.io.File
import java.security.MessageDigest
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rhaydus.common.AppLog
import nl.rhaydus.common.runCatchingCancellable

internal class WidgetImageStore(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private val directory: File = WidgetImageFiles.directory(context)

    /** [render] runs only when no file exists for [key] yet. Null when [render] returns null. */
    suspend fun uriFor(
        key: String,
        render: suspend () -> Bitmap?,
    ): Uri? {
        val file = File(
            directory,
            fileName(key),
        )

        val hit = withContext(ioDispatcher) { file.exists() && file.setLastModified(now()) }
        if (hit) return uriOf(file)

        val bitmap = render() ?: return null

        return withContext(ioDispatcher) {
            var temp: File? = null
            runCatchingCancellable {
                directory.mkdirs()
                val created = File.createTempFile(
                    "render",
                    ".tmp",
                    directory,
                )
                temp = created
                created.outputStream().use {
                    bitmap.compress(
                        Bitmap.CompressFormat.PNG,
                        100,
                        it,
                    )
                }
                if (created.renameTo(file)) uriOf(file) else null
            }.onFailure { error ->
                AppLog.e(
                    error,
                    "Writing a widget image failed",
                )
            }.also {
                temp?.delete()
            }.getOrNull()
        }
    }

    /** Deletes files not touched for [maxAge]. */
    suspend fun prune(maxAge: Duration = 7.days) {
        val cutoff = now() - maxAge.inWholeMilliseconds

        withContext(ioDispatcher) {
            directory.listFiles().orEmpty()
                .filter { it.lastModified() < cutoff }
                .forEach { it.delete() }
        }
    }

    private fun uriOf(file: File): Uri = "content://${WidgetImageFiles.authority(context)}/${file.name}".toUri()

    private fun fileName(key: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(key.toByteArray())
        return digest.joinToString(separator = "") { "%02x".format(it) } + WidgetImageFiles.EXTENSION
    }
}
