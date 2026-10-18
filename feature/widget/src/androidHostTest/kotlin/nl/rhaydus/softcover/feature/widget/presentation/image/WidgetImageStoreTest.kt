package nl.rhaydus.softcover.feature.widget.presentation.image

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.longs.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.nio.file.Files
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WidgetImageStoreTest {
    private val packageName = "nl.rhaydus.test"

    private lateinit var filesDir: File
    private lateinit var context: Context
    private val parsedUris = mutableListOf<String>()
    private var nowMillis = 1_000_000_000L
    private val clock = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(nowMillis)
    }
    private lateinit var store: WidgetImageStore

    @BeforeEach
    fun setUp() {
        filesDir = Files.createTempDirectory("widget-store").toFile()
        context = mockk {
            every {
                filesDir
            } returns this@WidgetImageStoreTest.filesDir
            every {
                packageName
            } returns this@WidgetImageStoreTest.packageName
        }
        mockkStatic(Uri::class)
        parsedUris.clear()
        every {
            Uri.parse(any())
        } answers {
            parsedUris += firstArg<String>()
            mockk<Uri>(relaxed = true)
        }
        store = WidgetImageStore(
            context,
            clock = clock,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Uri::class)
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
    inner class UriFor {
        @Test
        fun `renders once and serves the second request for the same key from disk`() = runTest {
            // ----- Arrange -----
            var renders = 0
            val render: suspend () -> Bitmap? = {
                renders++
                bitmap()
            }

            // ----- Act -----
            val first = store.uriFor(
                "cover-1",
                render = render,
            )
            val second = store.uriFor(
                "cover-1",
                render = render,
            )

            // ----- Assert -----
            first.shouldNotBeNull()
            second.shouldNotBeNull()
            parsedUris shouldHaveSize 2
            parsedUris[1] shouldBe parsedUris[0]
            renders shouldBe 1
            storedFiles() shouldHaveSize 1
        }

        @Test
        fun `a hit refreshes the file's last modified to the clock`() = runTest {
            // ----- Arrange -----
            store.uriFor("cover-1") { bitmap() }
            val file = storedFiles().single()
            file.setLastModified(5_000L)
            nowMillis = 2_000_000_000L

            // ----- Act -----
            store.uriFor("cover-1") { error("must not render") }

            // ----- Assert -----
            file.lastModified() shouldBeExactly 2_000_000_000L
        }

        @Test
        fun `returns null and leaves no file when render yields null`() = runTest {
            // ----- Arrange -----
            // (nothing to set up)

            // ----- Act -----
            val result = store.uriFor("cover-1") { null }

            // ----- Assert -----
            result.shouldBeNull()
            storedFiles().shouldBeEmpty()
        }

        @Test
        fun `leaves no temp file after a successful render`() = runTest {
            // ----- Arrange -----
            // (nothing to set up)

            // ----- Act -----
            store.uriFor("cover-1") { bitmap() }

            // ----- Assert -----
            storedFiles().all { WidgetImageFiles.NAME_PATTERN.matches(it.name) } shouldBe true
        }

        @Test
        fun `returns null and leaves no target or temp file when the write throws`() = runTest {
            // ----- Arrange -----
            val failing: Bitmap = mockk {
                every {
                    compress(
                        any(),
                        any(),
                        any(),
                    )
                } throws IOException("disk full")
            }

            // ----- Act -----
            val result = store.uriFor("cover-1") { failing }

            // ----- Assert -----
            result.shouldBeNull()
            storedFiles().shouldBeEmpty()
        }

        @Test
        fun `renders again for the same key after a failed write`() = runTest {
            // ----- Arrange -----
            val failing: Bitmap = mockk {
                every {
                    compress(
                        any(),
                        any(),
                        any(),
                    )
                } throws IOException("disk full")
            }
            store.uriFor("cover-1") { failing }
            var renders = 0

            // ----- Act -----
            val result = store.uriFor("cover-1") {
                renders++
                bitmap()
            }

            // ----- Assert -----
            result.shouldNotBeNull()
            renders shouldBe 1
            storedFiles() shouldHaveSize 1
        }

        @Test
        fun `maps different keys to different files under the provider authority`() = runTest {
            // ----- Arrange -----
            // (nothing to set up)

            // ----- Act -----
            val first = store.uriFor("cover-1") { bitmap() }
            val second = store.uriFor("cover-2") { bitmap() }

            // ----- Assert -----
            first.shouldNotBeNull()
            second.shouldNotBeNull()
            parsedUris[0] shouldNotBe parsedUris[1]
            parsedUris.all { it.startsWith("content://$packageName.widgetimages/") } shouldBe true
            storedFiles() shouldHaveSize 2
        }

        @Test
        fun `treats a zero-byte file as a miss and renders it again`() = runTest {
            // ----- Arrange -----
            store.uriFor("cover-1") { bitmap() }
            storedFiles().single().writeBytes(ByteArray(0))
            var renders = 0

            // ----- Act -----
            val result = store.uriFor("cover-1") {
                renders++
                bitmap()
            }

            // ----- Assert -----
            result.shouldNotBeNull()
            renders shouldBe 1
            storedFiles().single().length() shouldBeExactly 3L
        }

        @Test
        fun `returns null and leaves no file when compress reports failure`() = runTest {
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
            val result = store.uriFor("cover-1") { failing }

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
            store.uriFor("old") { bitmap() }
            val old = storedFiles().single()
            store.uriFor("fresh") { bitmap() }
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
            store.uriFor("a") { bitmap() }
            store.uriFor("b") { bitmap() }

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
            store.uriFor("a") { bitmap() }

            // ----- Act -----
            store.clear()

            // ----- Assert -----
            storedFiles().toSet() shouldBe setOf(temp, foreign)
        }
    }
}
