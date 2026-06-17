package nl.rhaydus.softcover.core.book.data.storage

import io.kotest.matchers.shouldBe
import java.io.File
import okio.FileSystem
import okio.Path.Companion.toPath
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class EditionImageStorageImplTest {
    @TempDir
    lateinit var tempDir: File

    private lateinit var storage: EditionImageStorageImpl

    @BeforeEach
    fun setUp() {
        storage = EditionImageStorageImpl(
            fileSystem = FileSystem.SYSTEM,
            rootDir = tempDir.absolutePath.toPath(),
        )
    }

    @Nested
    inner class Exists {
        @Test
        fun `returns false when no file has been written for the edition`() {
            // ----- Arrange -----
            val editionId = 1

            // ----- Act -----
            val result = storage.exists(editionId = editionId)

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `returns true after write has been called for the edition`() {
            // ----- Arrange -----
            val editionId = 2
            storage.write(
                editionId = editionId,
                bytes = byteArrayOf(0),
            )

            // ----- Act -----
            val result = storage.exists(editionId = editionId)

            // ----- Assert -----
            result shouldBe true
        }
    }

    @Nested
    inner class Write {
        @Test
        fun `writes bytes to the edition file and they can be read back`() {
            // ----- Arrange -----
            val editionId = 10
            val bytes = byteArrayOf(1, 2, 3)

            // ----- Act -----
            val path = storage.write(
                editionId = editionId,
                bytes = bytes,
            )

            // ----- Assert -----
            File(path).readBytes() shouldBe bytes
        }

        @Test
        fun `returns the absolute path whose parent dir is edition_images under filesDir`() {
            // ----- Arrange -----
            val editionId = 11

            // ----- Act -----
            val path = storage.write(
                editionId = editionId,
                bytes = byteArrayOf(9),
            )

            // ----- Assert -----
            val file = File(path)
            file.name shouldBe "11"
            file.parentFile?.name shouldBe "edition_images"
            file.parentFile?.parentFile?.canonicalPath shouldBe tempDir.canonicalPath
        }

        @Test
        fun `overwrites an existing file for the same edition`() {
            // ----- Arrange -----
            val editionId = 12
            storage.write(
                editionId = editionId,
                bytes = byteArrayOf(1),
            )

            // ----- Act -----
            val path = storage.write(
                editionId = editionId,
                bytes = byteArrayOf(2, 3),
            )

            // ----- Assert -----
            File(path).readBytes() shouldBe byteArrayOf(2, 3)
        }
    }

    @Nested
    inner class Delete {
        @Test
        fun `removes the file at the given path when it exists`() {
            // ----- Arrange -----
            val editionId = 20
            val path = storage.write(
                editionId = editionId,
                bytes = byteArrayOf(7, 8),
            )

            // ----- Act -----
            storage.delete(path = path)

            // ----- Assert -----
            storage.exists(editionId = editionId) shouldBe false
        }

        @Test
        fun `is a no-op when no file exists at the given path`() {
            // ----- Arrange -----
            val missingPath = File(
                File(
                    tempDir,
                    "edition_images",
                ),
                "999",
            ).absolutePath

            // ----- Act & Assert -----
            storage.delete(path = missingPath)
        }
    }
}
