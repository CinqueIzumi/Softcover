package nl.rhaydus.softcover.core.book.data.storage

import android.content.Context
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.io.File
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class EditionImageStorageImplTest {
    @TempDir
    lateinit var tempDir: File

    private lateinit var context: Context
    private lateinit var storage: EditionImageStorageImpl

    @BeforeEach
    fun setUp() {
        context = mockk {
            every { filesDir } returns tempDir
        }

        storage = EditionImageStorageImpl(context = context)
    }

    @Nested
    inner class FileFor {
        @Test
        fun `returns a file whose parent is edition_images under filesDir`() {
            // ----- Arrange -----
            val editionId = 42

            // ----- Act -----
            val result = storage.fileFor(editionId = editionId)

            // ----- Assert -----
            result.parentFile?.name shouldBe "edition_images"
            result.parentFile?.parentFile?.canonicalPath shouldBe tempDir.canonicalPath
        }

        @Test
        fun `returns a file whose name equals the edition id`() {
            // ----- Arrange -----
            val editionId = 99

            // ----- Act -----
            val result = storage.fileFor(editionId = editionId)

            // ----- Assert -----
            result.name shouldBe "99"
        }
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
        fun `returns true when a file for the edition exists on disk`() {
            // ----- Arrange -----
            val editionId = 2
            val target = storage.fileFor(editionId = editionId)
            target.writeText("image-data")

            // ----- Act -----
            val result = storage.exists(editionId = editionId)

            // ----- Assert -----
            result shouldBe true
        }
    }

    @Nested
    inner class CopyFrom {
        @Test
        fun `copies bytes from source file to the edition file`() {
            // ----- Arrange -----
            val editionId = 10
            val source = File(
                tempDir,
                "source.jpg",
            ).also { it.writeBytes(byteArrayOf(1, 2, 3)) }

            // ----- Act -----
            storage.copyFrom(
                editionId = editionId,
                source = source,
            )

            // ----- Assert -----
            val dest = storage.fileFor(editionId = editionId)
            dest.readBytes() shouldBe byteArrayOf(1, 2, 3)
        }

        @Test
        fun `returns the absolute path of the written file`() {
            // ----- Arrange -----
            val editionId = 11
            val source = File(
                tempDir,
                "source2.jpg",
            ).also { it.writeText("content") }
            val expected = storage.fileFor(editionId = editionId).absolutePath

            // ----- Act -----
            val result = storage.copyFrom(
                editionId = editionId,
                source = source,
            )

            // ----- Assert -----
            result shouldBe expected
        }

        @Test
        fun `overwrites an existing file for the same edition`() {
            // ----- Arrange -----
            val editionId = 12
            val source1 = File(
                tempDir,
                "v1.jpg",
            ).also { it.writeText("version-1") }
            val source2 = File(
                tempDir,
                "v2.jpg",
            ).also { it.writeText("version-2") }

            storage.copyFrom(
                editionId = editionId,
                source = source1,
            )

            // ----- Act -----
            storage.copyFrom(
                editionId = editionId,
                source = source2,
            )

            // ----- Assert -----
            storage.fileFor(editionId = editionId).readText() shouldBe "version-2"
        }
    }

    @Nested
    inner class Delete {
        @Test
        fun `removes the file at the given path when it exists`() {
            // ----- Arrange -----
            val editionId = 20
            val source = File(
                tempDir,
                "to_delete.jpg",
            ).also { it.writeText("data") }
            val path = storage.copyFrom(
                editionId = editionId,
                source = source,
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
