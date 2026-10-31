package nl.rhaydus.softcover.feature.widget.presentation.image

import android.net.Uri
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.every
import io.mockk.mockk
import java.io.FileNotFoundException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WidgetImageProviderTest {
    private lateinit var provider: WidgetImageProvider

    private val validName = "a".repeat(64) + ".png"

    @BeforeEach
    fun setUp() {
        provider = WidgetImageProvider()
    }

    private fun uriOf(vararg segments: String): Uri = mockk {
        every {
            pathSegments
        } returns segments.toList()
    }

    @Nested
    inner class OpenFile {
        @Test
        fun `rejects write modes`() {
            // ----- Arrange -----
            val uri = uriOf(validName)

            // ----- Act & Assert -----
            shouldThrow<FileNotFoundException> {
                provider.openFile(
                    uri,
                    "w",
                )
            }
            shouldThrow<FileNotFoundException> {
                provider.openFile(
                    uri,
                    "rw",
                )
            }
        }

        @Test
        fun `rejects a name that does not match the pattern`() {
            // ----- Arrange -----
            val lowercaseOnly = uriOf("cover.png")
            val uppercase = uriOf("A".repeat(64) + ".png")

            // ----- Act & Assert -----
            shouldThrow<FileNotFoundException> {
                provider.openFile(
                    lowercaseOnly,
                    "r",
                )
            }
            shouldThrow<FileNotFoundException> {
                provider.openFile(
                    uppercase,
                    "r",
                )
            }
        }

        @Test
        fun `rejects a traversal segment`() {
            // ----- Arrange -----
            val separateSegment = uriOf(
                "..",
                "x",
            )
            val embeddedSegment = uriOf("../x")

            // ----- Act & Assert -----
            shouldThrow<FileNotFoundException> {
                provider.openFile(
                    separateSegment,
                    "r",
                )
            }
            shouldThrow<FileNotFoundException> {
                provider.openFile(
                    embeddedSegment,
                    "r",
                )
            }
        }

        @Test
        fun `rejects a path with more than one segment`() {
            // ----- Arrange -----
            val uri = uriOf(
                "sub",
                validName,
            )

            // ----- Act & Assert -----
            shouldThrow<FileNotFoundException> {
                provider.openFile(
                    uri,
                    "r",
                )
            }
        }
    }
}
