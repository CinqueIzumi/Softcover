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
            // ----- Act & Assert -----
            shouldThrow<FileNotFoundException> { provider.openFile(
                uriOf(validName),
                "w",
            ) }
            shouldThrow<FileNotFoundException> { provider.openFile(
                uriOf(validName),
                "rw",
            ) }
        }

        @Test
        fun `rejects a name that does not match the pattern`() {
            // ----- Act & Assert -----
            shouldThrow<FileNotFoundException> { provider.openFile(
                uriOf("cover.png"),
                "r",
            ) }
            shouldThrow<FileNotFoundException> { provider.openFile(
                uriOf("A".repeat(64) + ".png"),
                "r",
            ) }
        }

        @Test
        fun `rejects a traversal segment`() {
            // ----- Act & Assert -----
            shouldThrow<FileNotFoundException> { provider.openFile(
                uriOf(
                    "..",
                    "x",
                ),
                "r",
            ) }
            shouldThrow<FileNotFoundException> { provider.openFile(
                uriOf("../x"),
                "r",
            ) }
        }

        @Test
        fun `rejects a path with more than one segment`() {
            // ----- Act & Assert -----
            shouldThrow<FileNotFoundException> { provider.openFile(
                uriOf(
                    "sub",
                    validName,
                ),
                "r",
            ) }
        }
    }
}
