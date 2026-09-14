package nl.rhaydus.softcover.core.uibinding.cover

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import nl.rhaydus.softcover.core.component.cover.CoverSource
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.preview.PreviewData

class CoverSourceResolverTest {
    @TempDir
    lateinit var tempDir: Path

    private fun localFile(name: String): String {
        val file = tempDir.resolve(name).toFile()
        file.writeText("cover-bytes")
        return file.absolutePath
    }

    private fun edition(
        id: Int = 1,
        url: String? = null,
        localImagePath: String? = null,
    ): BookEdition = PreviewData.baseEdition.copy(
        id = id,
        url = url,
        localImagePath = localImagePath,
    )

    @Nested
    inner class LocalFileRung {
        @Test
        fun `a local file on edition wins over defaultEdition's url and the fallback`() {
            // ----- Arrange -----
            val editionLocalPath = localFile("edition-cover.jpg")
            val subject = edition(
                id = 1,
                url = null,
                localImagePath = editionLocalPath,
            )
            val defaultEdition = edition(
                id = 2,
                url = "https://example.com/default.jpg",
            )

            // ----- Act -----
            val result = resolveCoverSource(
                edition = subject,
                defaultEdition = defaultEdition,
                fallbackCoverUrl = "https://example.com/fallback.jpg",
            )

            // ----- Assert -----
            result.shouldBe(
                CoverSource.Local(
                    path = editionLocalPath,
                    cacheKey = null,
                ),
            )
        }

        @Test
        fun `when edition has no local file, falls through to defaultEdition's local file`() {
            // ----- Arrange -----
            val subject = edition(
                id = 1,
                url = null,
                localImagePath = null,
            )
            val defaultLocalPath = localFile("default-cover.jpg")
            val defaultEdition = edition(
                id = 2,
                localImagePath = defaultLocalPath,
            )

            // ----- Act -----
            val result = resolveCoverSource(
                edition = subject,
                defaultEdition = defaultEdition,
                fallbackCoverUrl = null,
            )

            // ----- Assert -----
            result.shouldBe(
                CoverSource.Local(
                    path = defaultLocalPath,
                    cacheKey = null,
                ),
            )
        }

        @Test
        fun `the local result's path and cacheKey are keyed off the winning edition`() {
            // ----- Arrange -----
            val localPath = localFile("keyed-cover.jpg")
            val subject = edition(
                id = 1,
                url = "https://example.com/edition.jpg",
                localImagePath = localPath,
            )

            // ----- Act -----
            val result = resolveCoverSource(
                edition = subject,
                defaultEdition = null,
                fallbackCoverUrl = null,
            )

            // ----- Assert -----
            // CoverSource.Local structurally carries no persistEditionId field at all — only the
            // remote-URL rung below persists an edition id. Assert both Local fields explicitly.
            result.shouldBe(
                CoverSource.Local(
                    path = localPath,
                    cacheKey = "https://example.com/edition.jpg",
                ),
            )
        }
    }

    @Nested
    inner class RemoteUrlRung {
        @Test
        fun `when neither edition has a local file, edition's url resolves to Remote keyed off edition`() {
            // ----- Arrange -----
            val subject = edition(
                id = 7,
                url = "https://example.com/edition.jpg",
                localImagePath = null,
            )
            val defaultEdition = edition(
                id = 8,
                url = "https://example.com/default.jpg",
            )

            // ----- Act -----
            val result = resolveCoverSource(
                edition = subject,
                defaultEdition = defaultEdition,
                fallbackCoverUrl = "https://example.com/fallback.jpg",
            )

            // ----- Assert -----
            result.shouldBe(
                CoverSource.Remote(
                    url = "https://example.com/edition.jpg",
                    persistEditionId = 7,
                ),
            )
        }

        @Test
        fun `when edition's url is null, falls through to defaultEdition's url`() {
            // ----- Arrange -----
            val subject = edition(
                id = 7,
                url = null,
                localImagePath = null,
            )
            val defaultEdition = edition(
                id = 8,
                url = "https://example.com/default.jpg",
            )

            // ----- Act -----
            val result = resolveCoverSource(
                edition = subject,
                defaultEdition = defaultEdition,
                fallbackCoverUrl = null,
            )

            // ----- Assert -----
            result.shouldBe(
                CoverSource.Remote(
                    url = "https://example.com/default.jpg",
                    persistEditionId = 8,
                ),
            )
        }
    }

    @Nested
    inner class FallbackRung {
        @Test
        fun `when both editions have no local file and no url, fallbackCoverUrl resolves without persisting`() {
            // ----- Arrange -----
            val subject = edition(
                id = 1,
                url = null,
                localImagePath = null,
            )
            val defaultEdition = edition(
                id = 2,
                url = null,
                localImagePath = null,
            )

            // ----- Act -----
            val result = resolveCoverSource(
                edition = subject,
                defaultEdition = defaultEdition,
                fallbackCoverUrl = "https://example.com/fallback.jpg",
            )

            // ----- Assert -----
            // The fallback URL must never persist — a regression here would silently start writing
            // a fallback cover to disk against an edition it doesn't actually belong to.
            result.shouldBe(
                CoverSource.Remote(
                    url = "https://example.com/fallback.jpg",
                    persistEditionId = null,
                ),
            )
        }

        @Test
        fun `when both editions are null, fallbackCoverUrl still resolves`() {
            // ----- Act -----
            val result = resolveCoverSource(
                edition = null,
                defaultEdition = null,
                fallbackCoverUrl = "https://example.com/fallback.jpg",
            )

            // ----- Assert -----
            result.shouldBe(
                CoverSource.Remote(
                    url = "https://example.com/fallback.jpg",
                    persistEditionId = null,
                ),
            )
        }
    }

    @Nested
    inner class NullRung {
        @Test
        fun `when edition, defaultEdition and fallbackCoverUrl are all null, result is null`() {
            // ----- Act -----
            val result = resolveCoverSource(
                edition = null,
                defaultEdition = null,
                fallbackCoverUrl = null,
            )

            // ----- Assert -----
            result shouldBe null
        }
    }
}
