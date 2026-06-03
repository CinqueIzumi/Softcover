package nl.rhaydus.softcover.core.book.data.storage

import android.content.Context
import java.io.File

internal interface EditionImageStorage {
    fun exists(editionId: Int): Boolean

    fun write(
        editionId: Int,
        bytes: ByteArray,
    ): String

    fun delete(path: String)
}

internal class EditionImageStorageImpl(
    context: Context,
) : EditionImageStorage {
    private val rootDir: File = File(
        context.filesDir,
        DIRECTORY_NAME,
    ).apply {
        if (exists().not()) mkdirs()
    }

    override fun exists(editionId: Int): Boolean = fileFor(editionId).exists()

    override fun write(
        editionId: Int,
        bytes: ByteArray,
    ): String {
        val target = fileFor(editionId)
        target.writeBytes(bytes)

        return target.absolutePath
    }

    override fun delete(path: String) {
        File(path).takeIf { it.exists() }?.delete()
    }

    private fun fileFor(editionId: Int): File = File(
        rootDir,
        "$editionId",
    )

    companion object {
        private const val DIRECTORY_NAME = "edition_images"
    }
}
