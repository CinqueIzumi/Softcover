package nl.rhaydus.softcover.core.book.data.storage

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

internal interface EditionImageStorage {
    fun write(
        editionId: Int,
        bytes: ByteArray,
    ): String

    fun delete(path: String)
}

/**
 * Persists edition cover images under `<rootDir>/edition_images/<editionId>`. The store is fully
 * multiplatform via okio; the only platform-bound piece is [rootDir] — the per-target base location,
 * supplied by the platform Koin module (Android: `filesDir`; iOS: the documents directory). The
 * Android layout matches the previous `java.io.File` location, so existing images are picked up
 * unchanged.
 */
internal class EditionImageStorageImpl(
    private val fileSystem: FileSystem,
    rootDir: Path,
) : EditionImageStorage {
    private val imagesDir: Path = rootDir / DIRECTORY_NAME

    init {
        if (fileSystem.exists(imagesDir).not()) fileSystem.createDirectories(imagesDir)
    }

    override fun write(
        editionId: Int,
        bytes: ByteArray,
    ): String {
        val target = fileFor(editionId)
        fileSystem.write(target) { write(bytes) }

        return target.toString()
    }

    override fun delete(path: String) {
        val target = path.toPath()
        if (fileSystem.exists(target)) fileSystem.delete(target)
    }

    private fun fileFor(editionId: Int): Path = imagesDir / "$editionId"

    companion object {
        private const val DIRECTORY_NAME = "edition_images"
    }
}
