package nl.rhaydus.softcover.core.book.data.storage

import android.content.Context
import java.io.File

interface EditionImageStorage {
    fun fileFor(editionId: Int): File

    fun exists(editionId: Int): Boolean

    fun copyFrom(
        editionId: Int,
        source: File,
    ): String

    fun delete(path: String)
}

class EditionImageStorageImpl(
    context: Context,
) : EditionImageStorage {
    private val rootDir: File = File(
        context.filesDir,
        DIRECTORY_NAME,
    ).apply {
        if (exists().not()) mkdirs()
    }

    override fun fileFor(editionId: Int): File = File(
        rootDir,
        "$editionId",
    )

    override fun exists(editionId: Int): Boolean = fileFor(editionId).exists()

    override fun copyFrom(
        editionId: Int,
        source: File,
    ): String {
        val target = fileFor(editionId)
        source.copyTo(
            target = target,
            overwrite = true,
        )

        return target.absolutePath
    }

    override fun delete(path: String) {
        File(path).takeIf { it.exists() }?.delete()
    }

    companion object {
        private const val DIRECTORY_NAME = "edition_images"
    }
}
