package nl.rhaydus.softcover.feature.widget.presentation.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rhaydus.common.AppLog
import nl.rhaydus.common.runCatchingCancellable
import java.io.File
import java.security.MessageDigest
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

internal class WidgetImageStore(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val clock: Clock = Clock.System,
) {
    private val directory: File = WidgetImageFiles.directory(context)

    /**
     * [render] runs only when no readable file exists for [key] yet; a file that fails to decode is deleted first.
     * Null when [render] returns null. A failed write still returns the rendered bitmap, which then goes uncached.
     */
    suspend fun bitmapFor(
        key: String,
        format: WidgetImageFormat = WidgetImageFormat.Lossless,
        render: suspend () -> Bitmap?,
    ): Bitmap? {
        val file = fileFor(
            key = key,
            format = format,
        )

        if (touch(file)) decode(file)?.let { return it }

        val bitmap = render() ?: return null

        write(
            bitmap = bitmap,
            format = format,
            file = file,
        )

        return bitmap
    }

    /**
     * Warms the file for [key] without decoding it: an existing file is only touched, and [render] runs only on a
     * miss. A file that [bitmapFor] later fails to decode is deleted and rendered again there.
     */
    suspend fun ensureCached(
        key: String,
        format: WidgetImageFormat = WidgetImageFormat.Lossless,
        render: suspend () -> Bitmap?,
    ) {
        val file = fileFor(
            key = key,
            format = format,
        )

        if (touch(file)) return

        val bitmap = render() ?: return

        write(
            bitmap = bitmap,
            format = format,
            file = file,
        )
    }

    private fun fileFor(
        key: String,
        format: WidgetImageFormat,
    ) = File(
        directory,
        fileName(
            key = key,
            format = format,
        ),
    )

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

    private suspend fun decode(file: File): Bitmap? = withContext(ioDispatcher) {
        BitmapFactory.decodeFile(file.path) ?: run {
            file.delete()
            null
        }
    }

    private suspend fun write(
        bitmap: Bitmap,
        format: WidgetImageFormat,
        file: File,
    ) = withContext(ioDispatcher) {
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

            compressed && created.length() > 0L && created.renameTo(file)
        }.onFailure { error ->
            AppLog.e(
                error,
                "Writing a widget image failed",
            )
        }.also {
            temp?.delete()
        }
    }

    private fun fileName(
        key: String,
        format: WidgetImageFormat,
    ): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(key.toByteArray())

        val hex = digest.joinToString(separator = "") { byte ->
            String.format(
                Locale.ROOT,
                "%02x",
                byte,
            )
        }

        return hex + format.extension
    }

    private companion object {
        const val TEMP_SUFFIX = ".tmp"
    }
}
