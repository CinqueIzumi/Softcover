package nl.rhaydus.softcover.feature.widget.presentation.image

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.net.toUri
import java.io.File
import java.security.MessageDigest
import kotlin.time.Clock
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
    private val clock: Clock = Clock.System,
) {
    private val directory: File = WidgetImageFiles.directory(context)

    /**
     * [render] runs only when no non-empty file exists for [key] yet. Null when [render] returns null or the write
     * fails.
     */
    suspend fun uriFor(
        key: String,
        format: WidgetImageFormat = WidgetImageFormat.Lossless,
        render: suspend () -> Bitmap?,
    ): Uri? {
        val file = File(
            directory,
            fileName(
                key = key,
                format = format,
            ),
        )

        if (touch(file)) return uriOf(file)

        val bitmap = render() ?: return null

        return write(
            bitmap = bitmap,
            format = format,
            file = file,
        )
    }

    /** Deletes files not touched for [maxAge]. */
    suspend fun prune(maxAge: Duration = 7.days) {
        val cutoff = clock.now().toEpochMilliseconds() - maxAge.inWholeMilliseconds

        withContext(ioDispatcher) {
            directory.listFiles().orEmpty()
                .filter { it.lastModified() < cutoff }
                .forEach { it.delete() }
        }
    }

    suspend fun clear() {
        withContext(ioDispatcher) {
            directory.listFiles().orEmpty().filter { it.isImage() }.forEach { it.delete() }
        }
    }

    private fun File.isImage(): Boolean = WidgetImageFiles.NAME_PATTERN.matches(name)

    private suspend fun touch(file: File): Boolean {
        val touchedAt = clock.now().toEpochMilliseconds()

        return withContext(ioDispatcher) {
            when {
                file.exists().not() -> false
                file.length() == 0L -> {
                    file.delete()
                    false
                }

                else -> file.setLastModified(touchedAt)
            }
        }
    }

    private suspend fun write(
        bitmap: Bitmap,
        format: WidgetImageFormat,
        file: File,
    ): Uri? = withContext(ioDispatcher) {
        var temp: File? = null
        runCatchingCancellable {
            directory.mkdirs()
            val created = File.createTempFile(
                "render",
                TEMP_SUFFIX,
                directory,
            )
            temp = created
            val compressed = created.outputStream().use {
                format.compress(
                    bitmap = bitmap,
                    output = it,
                )
            }
            if (compressed && created.length() > 0L && created.renameTo(file)) uriOf(file) else null
        }.onFailure { error ->
            AppLog.e(
                error,
                "Writing a widget image failed",
            )
        }.also {
            temp?.delete()
        }.getOrNull()
    }

    private fun uriOf(file: File): Uri = "content://${WidgetImageFiles.authority(context)}/${file.name}".toUri()

    private fun fileName(
        key: String,
        format: WidgetImageFormat,
    ): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(key.toByteArray())
        return digest.joinToString(separator = "") { "%02x".format(it) } + format.extension
    }

    private companion object {
        const val TEMP_SUFFIX = ".tmp"
    }
}
