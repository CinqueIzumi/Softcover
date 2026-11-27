package nl.rhaydus.softcover.feature.widget.presentation.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.longs.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.nio.file.Files
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class WidgetImageStoreTest {
    private val packageName = "nl.rhaydus.test"

    private lateinit var filesDir: File
    private lateinit var context: Context
    private val decoded: Bitmap = mockk()
    private val decodedPaths = mutableListOf<String>()
    private var decodeResult: Bitmap? = decoded
    private var nowMillis = 1_000_000_000L
    private val clock = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(nowMillis)
    }
    private lateinit var store: WidgetImageStore

    companion object {
        @JvmStatic
        @BeforeAll
        fun mockStatics() {
            mockkStatic(BitmapFactory::class)
        }

        @JvmStatic
        @AfterAll
        fun unmockStatics() {
            unmockkStatic(BitmapFactory::class)
        }
    }

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        filesDir = Files.createTempDirectory("widget-store").toFile()

        context = mockk {
            every {
                filesDir
            } returns this@WidgetImageStoreTest.filesDir

            every {
                packageName
            } returns this@WidgetImageStoreTest.packageName
        }

        decodedPaths.clear()
        decodeResult = decoded

        every {
            BitmapFactory.decodeFile(any<String>())
        } answers {
            decodedPaths += firstArg<String>()
            decodeResult
        }

        store = WidgetImageStore(
            context,
            clock = clock,
        )
    }

    @AfterEach
    fun tearDown() {
        filesDir.deleteRecursively()
    }

    private fun bitmap(): Bitmap = mockk {
        every {
            compress(
                any(),
                any(),
                any(),
            )
        } answers {
            thirdArg<OutputStream>().write(byteArrayOf(1, 2, 3))
            true
        }
    }

    private fun failingBitmap(): Bitmap = mockk {
        every {
            compress(
                any(),
                any(),
                any(),
            )
        } throws IOException("disk full")
    }

    private fun storedFiles(): List<File> = File(
        filesDir,
        WidgetImageFiles.DIRECTORY_NAME,
    ).listFiles().orEmpty().toList()

    private fun storedFile(
        name: String,
        ageMillis: Long,
    ): File {
        val directory = File(
            filesDir,
            WidgetImageFiles.DIRECTORY_NAME,
        ).also { it.mkdirs() }

        return File(
            directory,
            name,
        ).also {
            it.writeBytes(byteArrayOf(1, 2, 3))
            it.setLastModified(nowMillis - ageMillis)
        }
    }

    @Nested
    inner class BitmapFor {
        @Test
        fun `a hit returns the decoded file without rendering`() = runTest {
            // ----- Arrange -----
            store.bitmapFor("cover-1") { bitmap() }
            val file = storedFiles().single()
            var renders = 0

            // ----- Act -----
            val result = store.bitmapFor("cover-1") {
                renders++
                bitmap()
            }

            // ----- Assert -----
            (result === decoded) shouldBe true
            renders shouldBe 0
            decodedPaths shouldBe listOf(file.path)
        }

        @Test
        fun `a hit refreshes the file's last modified to the clock`() = runTest {
            // ----- Arrange -----
            store.bitmapFor("cover-1") { bitmap() }
            val file = storedFiles().single()
            file.setLastModified(5_000L)
            nowMillis = 2_000_000_000L

            // ----- Act -----
            store.bitmapFor("cover-1") { error("must not render") }

            // ----- Assert -----
            file.lastModified() shouldBeExactly 2_000_000_000L
        }

        @Test
        fun `a miss renders once, writes the file and returns the rendered bitmap`() = runTest {
            // ----- Arrange -----
            val rendered = bitmap()
            var renders = 0

            // ----- Act -----
            val result = store.bitmapFor("cover-1") {
                renders++
                rendered
            }

            // ----- Assert -----
            (result === rendered) shouldBe true
            renders shouldBe 1
            storedFiles().single().length() shouldBeExactly 3L
            decodedPaths.shouldBeEmpty()
        }

        @Test
        fun `returns null and leaves no file when render yields null`() = runTest {
            // ----- Arrange -----
            // (nothing to set up)

            // ----- Act -----
            val result = store.bitmapFor("cover-1") { null }

            // ----- Assert -----
            result.shouldBeNull()
            storedFiles().shouldBeEmpty()
        }

        @Test
        fun `leaves no temp file after a successful render`() = runTest {
            // ----- Arrange -----
            // (nothing to set up)

            // ----- Act -----
            store.bitmapFor("cover-1") { bitmap() }

            // ----- Assert -----
            storedFiles().all { WidgetImageFiles.NAME_PATTERN.matches(it.name) } shouldBe true
        }

        @Test
        fun `still returns the rendered bitmap but leaves no file when the write throws`() = runTest {
            // ----- Arrange -----
            val failing = failingBitmap()

            // ----- Act -----
            val result = store.bitmapFor("cover-1") { failing }

            // ----- Assert -----
            (result === failing) shouldBe true
            storedFiles().shouldBeEmpty()
        }

        @Test
        fun `still returns the rendered bitmap but leaves no file when compress reports failure`() = runTest {
            // ----- Arrange -----
            val failing: Bitmap = mockk {
                every {
                    compress(
                        any(),
                        any(),
                        any(),
                    )
                } returns false
            }

            // ----- Act -----
            val result = store.bitmapFor("cover-1") { failing }

            // ----- Assert -----
            (result === failing) shouldBe true
            storedFiles().shouldBeEmpty()
        }

        @Test
        fun `renders again for the same key after a failed write`() = runTest {
            // ----- Arrange -----
            store.bitmapFor("cover-1") { failingBitmap() }
            var renders = 0

            // ----- Act -----
            store.bitmapFor("cover-1") {
                renders++
                bitmap()
            }

            // ----- Assert -----
            renders shouldBe 1
            storedFiles() shouldHaveSize 1
        }

        @Test
        fun `stores different keys in different files`() = runTest {
            // ----- Arrange -----
            // (nothing to set up)

            // ----- Act -----
            store.bitmapFor("cover-1") { bitmap() }
            store.bitmapFor("cover-2") { bitmap() }

            // ----- Assert -----
            storedFiles() shouldHaveSize 2
        }

        @Test
        fun `treats a zero-byte file as a miss and renders it again`() = runTest {
            // ----- Arrange -----
            store.bitmapFor("cover-1") { bitmap() }
            storedFiles().single().writeBytes(ByteArray(0))
            var renders = 0

            // ----- Act -----
            store.bitmapFor("cover-1") {
                renders++
                bitmap()
            }

            // ----- Assert -----
            renders shouldBe 1
            decodedPaths.shouldBeEmpty()
            storedFiles().single().length() shouldBeExactly 3L
        }

        @Test
        fun `deletes a file that fails to decode and renders it again`() = runTest {
            // ----- Arrange -----
            store.bitmapFor("cover-1") { bitmap() }
            val corrupt = storedFiles().single()
            corrupt.writeBytes(byteArrayOf(9, 9, 9, 9, 9))
            decodeResult = null
            val rendered = bitmap()
            var renders = 0

            // ----- Act -----
            val result = store.bitmapFor("cover-1") {
                renders++
                rendered
            }

            // ----- Assert -----
            (result === rendered) shouldBe true
            renders shouldBe 1
            decodedPaths shouldBe listOf(corrupt.path)
            storedFiles().single().length() shouldBeExactly 3L
        }

        @Test
        fun `returns null and leaves no file when a corrupt file is followed by a null render`() = runTest {
            // ----- Arrange -----
            store.bitmapFor("cover-1") { bitmap() }
            decodeResult = null

            // ----- Act -----
            val result = store.bitmapFor("cover-1") { null }

            // ----- Assert -----
            result.shouldBeNull()
            storedFiles().shouldBeEmpty()
        }
    }

    @Nested
    inner class Prune {
        @Test
        fun `deletes files older than max age and keeps newer ones`() = runTest {
            // ----- Arrange -----
            store.bitmapFor("old") { bitmap() }
            val old = storedFiles().single()
            store.bitmapFor("fresh") { bitmap() }
            val fresh = storedFiles().single { it != old }
            old.setLastModified(nowMillis - 8.days.inWholeMilliseconds)
            fresh.setLastModified(nowMillis - 1.days.inWholeMilliseconds)

            // ----- Act -----
            store.prune(7.days)

            // ----- Assert -----
            old.exists() shouldBe false
            fresh.exists() shouldBe true
        }

        @Test
        fun `does nothing when the directory does not exist`() = runTest {
            // ----- Arrange -----
            // (nothing to set up)

            // ----- Act -----
            store.prune(7.days)

            // ----- Assert -----
            storedFiles().shouldBeEmpty()
        }
    }

    @Nested
    inner class Clear {
        @Test
        fun `deletes every stored image`() = runTest {
            // ----- Arrange -----
            store.bitmapFor("a") { bitmap() }
            store.bitmapFor("b") { bitmap() }

            // ----- Act -----
            store.clear()

            // ----- Assert -----
            storedFiles().shouldBeEmpty()
        }

        @Test
        fun `does nothing when the directory does not exist`() = runTest {
            // ----- Arrange -----
            // (nothing to set up)

            // ----- Act -----
            store.clear()

            // ----- Assert -----
            storedFiles().shouldBeEmpty()
        }

        @Test
        fun `leaves a fresh temp file and a file that is not a stored image alone`() = runTest {
            // ----- Arrange -----
            val temp = storedFile(
                name = "render1.tmp",
                ageMillis = 1.minutes.inWholeMilliseconds,
            )

            val foreign = storedFile(
                name = "notes.txt",
                ageMillis = 1.minutes.inWholeMilliseconds,
            )

            store.bitmapFor("a") { bitmap() }

            // ----- Act -----
            store.clear()

            // ----- Assert -----
            storedFiles().toSet() shouldBe setOf(temp, foreign)
        }
    }

    @Nested
    inner class EnsureCached {
        @Test
        fun `a hit touches the file to the clock without rendering or decoding`() = runTest {
            // ----- Arrange -----
            store.bitmapFor("cover-1") { bitmap() }
            val file = storedFiles().single()
            file.setLastModified(5_000L)
            nowMillis = 2_000_000_000L
            decodedPaths.clear()
            var renders = 0

            // ----- Act -----
            store.ensureCached("cover-1") {
                renders++
                bitmap()
            }

            // ----- Assert -----
            file.lastModified() shouldBeExactly 2_000_000_000L
            renders shouldBe 0
            decodedPaths.shouldBeEmpty()
        }

        @Test
        fun `a miss renders once and writes the file so a following bitmapFor is a hit`() = runTest {
            // ----- Arrange -----
            var renders = 0

            // ----- Act -----
            store.ensureCached("cover-1") {
                renders++
                bitmap()
            }

            val result = store.bitmapFor("cover-1") {
                renders++
                bitmap()
            }

            // ----- Assert -----
            renders shouldBe 1
            (result === decoded) shouldBe true
            storedFiles().single().length() shouldBeExactly 3L
        }

        @Test
        fun `leaves no file when render yields null`() = runTest {
            // ----- Arrange -----
            // (nothing to set up)

            // ----- Act -----
            store.ensureCached("cover-1") { null }

            // ----- Assert -----
            storedFiles().shouldBeEmpty()
        }
    }
}
