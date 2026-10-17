package nl.rhaydus.softcover.feature.widget.presentation.image

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.net.toUri
import java.io.File
import java.security.MessageDigest
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class WidgetImageStore(
    private val context: Context,
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

        val hit = withContext(Dispatchers.IO) { file.exists() && file.setLastModified(now()) }
        if (hit) return uriOf(file)

        val bitmap = render() ?: return null

        return withContext(Dispatchers.IO) {
            directory.mkdirs()
            val temp = File.createTempFile(
                "render",
                ".tmp",
                directory,
            )
            try {
                temp.outputStream().use { bitmap.compress(
                    Bitmap.CompressFormat.PNG,
                    100,
                    it,
                ) }
                if (temp.renameTo(file)) uriOf(file) else null
            } finally {
                temp.delete()
            }
        }
    }

    /** Deletes files not touched for [maxAge]. */
    suspend fun prune(maxAge: Duration = 7.days) {
        val cutoff = now() - maxAge.inWholeMilliseconds

        withContext(Dispatchers.IO) {
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
