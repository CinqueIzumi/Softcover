package nl.rhaydus.softcover.core.database.mapper

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.database.model.EditionImageRef
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.ReadingFormat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class EditionCachePlannerTest {
    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    private fun stubBookEdition(
        id: Int = 10,
        canonicalId: Int? = null,
        bookId: Int = 1,
        publisher: String? = "Publisher",
        title: String? = "Edition Title",
        url: String? = "https://example.com/cover.jpg",
        localImagePath: String? = null,
        isbn10: String? = "1234567890",
        isbn13: String? = null,
        pages: Int? = 300,
        audioSeconds: Int? = null,
        releaseYear: Int = 2020,
        releaseDate: LocalDate? = null,
        format: String = "Paperback",
        readingFormat: ReadingFormat? = null,
    ): BookEdition = mockk {
        every {
            this@mockk.id
        } returns id
        every {
            this@mockk.canonicalId
        } returns canonicalId
        every {
            this@mockk.bookId
        } returns bookId
        every {
            this@mockk.publisher
        } returns publisher
        every {
            this@mockk.title
        } returns title
        every {
            this@mockk.url
        } returns url
        every {
            this@mockk.localImagePath
        } returns localImagePath
        every {
            this@mockk.isbn10
        } returns isbn10
        every {
            this@mockk.isbn13
        } returns isbn13
        every {
            this@mockk.pages
        } returns pages
        every {
            this@mockk.audioSeconds
        } returns audioSeconds
        every {
            this@mockk.authors
        } returns emptyList()
        every {
            this@mockk.releaseYear
        } returns releaseYear
        every {
            this@mockk.releaseDate
        } returns releaseDate
        every {
            this@mockk.format
        } returns format
        every {
            this@mockk.readingFormat
        } returns readingFormat
        every {
            this@mockk.owned
        } returns false
    }

    @Nested
    inner class NoCachedPath {
        @Test
        fun `edition with no existing ref produces entity with null path and url`() {
            // ----- Arrange -----
            val edition = stubBookEdition(
                id = 1,
                url = "https://example.com/cover.jpg",
            )

            // ----- Act -----
            val result = planEditionCaching(
                editions = listOf(edition),
                existingRefs = emptyList(),
            )

            // ----- Assert -----
            result.entities.single().localImagePath shouldBe null
            result.entities.single().localImageUrl shouldBe null
            result.stalePaths shouldBe emptyList()
        }

        @Test
        fun `edition whose existing ref has null localImagePath produces entity with null path and url`() {
            // ----- Arrange -----
            val edition = stubBookEdition(
                id = 1,
                url = "https://example.com/cover.jpg",
            )
            val ref = EditionImageRef(
                id = 1,
                localImagePath = null,
                localImageUrl = null,
            )

            // ----- Act -----
            val result = planEditionCaching(
                editions = listOf(edition),
                existingRefs = listOf(ref),
            )

            // ----- Assert -----
            result.entities.single().localImagePath shouldBe null
            result.entities.single().localImageUrl shouldBe null
            result.stalePaths shouldBe emptyList()
        }

        @Test
        fun `edition with null localImagePath in ref is not added to stalePaths`() {
            // ----- Arrange -----
            val edition = stubBookEdition(
                id = 1,
                url = "https://example.com/cover.jpg",
            )
            val ref = EditionImageRef(
                id = 1,
                localImagePath = null,
                localImageUrl = "https://example.com/cover.jpg",
            )

            // ----- Act -----
            val result = planEditionCaching(
                editions = listOf(edition),
                existingRefs = listOf(ref),
            )

            // ----- Assert -----
            result.stalePaths shouldBe emptyList()
        }
    }

    @Nested
    inner class MatchingUrl {
        @Test
        fun `matching url preserves existing path and url on entity`() {
            // ----- Arrange -----
            val url = "https://example.com/cover.jpg"
            val edition = stubBookEdition(
                id = 1,
                url = url,
            )
            val ref = EditionImageRef(
                id = 1,
                localImagePath = "/cache/cover.jpg",
                localImageUrl = url,
            )

            // ----- Act -----
            val result = planEditionCaching(
                editions = listOf(edition),
                existingRefs = listOf(ref),
            )

            // ----- Assert -----
            result.entities.single().localImagePath shouldBe "/cache/cover.jpg"
            result.entities.single().localImageUrl shouldBe url
            result.stalePaths shouldBe emptyList()
        }

        @Test
        fun `both edition url and existing localImageUrl null with a path — preserved, not stale`() {
            // ----- Arrange -----
            val edition = stubBookEdition(
                id = 1,
                url = null,
            )
            val ref = EditionImageRef(
                id = 1,
                localImagePath = "/cache/cover.jpg",
                localImageUrl = null,
            )

            // ----- Act -----
            val result = planEditionCaching(
                editions = listOf(edition),
                existingRefs = listOf(ref),
            )

            // ----- Assert -----
            result.entities.single().localImagePath shouldBe "/cache/cover.jpg"
            result.entities.single().localImageUrl shouldBe null
            result.stalePaths shouldBe emptyList()
        }
    }

    @Nested
    inner class StaleUrl {
        @Test
        fun `changed url evicts existing path — entity cleared, old path in stalePaths`() {
            // ----- Arrange -----
            val edition = stubBookEdition(
                id = 1,
                url = "https://example.com/new.jpg",
            )
            val ref = EditionImageRef(
                id = 1,
                localImagePath = "/cache/old.jpg",
                localImageUrl = "https://example.com/old.jpg",
            )

            // ----- Act -----
            val result = planEditionCaching(
                editions = listOf(edition),
                existingRefs = listOf(ref),
            )

            // ----- Assert -----
            result.entities.single().localImagePath shouldBe null
            result.entities.single().localImageUrl shouldBe null
            result.stalePaths shouldBe listOf("/cache/old.jpg")
        }

        @Test
        fun `legacy case — existing path with null provenance url but edition has a url — stale`() {
            // ----- Arrange -----
            val edition = stubBookEdition(
                id = 1,
                url = "https://example.com/cover.jpg",
            )
            val ref = EditionImageRef(
                id = 1,
                localImagePath = "/cache/legacy.jpg",
                localImageUrl = null,
            )

            // ----- Act -----
            val result = planEditionCaching(
                editions = listOf(edition),
                existingRefs = listOf(ref),
            )

            // ----- Assert -----
            result.entities.single().localImagePath shouldBe null
            result.entities.single().localImageUrl shouldBe null
            result.stalePaths shouldBe listOf("/cache/legacy.jpg")
        }
    }

    @Nested
    inner class MixedBatch {
        @Test
        fun `mixed batch — stalePaths contains only stale paths in order, entities count matches editions`() {
            // ----- Arrange -----
            val url = "https://example.com/cover.jpg"

            val editionNew = stubBookEdition(
                id = 1,
                url = url,
            )
            val editionPreserved = stubBookEdition(
                id = 2,
                url = url,
            )
            val editionStale = stubBookEdition(
                id = 3,
                url = "https://example.com/new.jpg",
            )
            val editionLegacyStale = stubBookEdition(
                id = 4,
                url = "https://example.com/cover.jpg",
            )
            val editionNoRef = stubBookEdition(
                id = 5,
                url = url,
            )

            val refs = listOf(
                EditionImageRef(
                    id = 1,
                    localImagePath = null,
                    localImageUrl = null,
                ),
                EditionImageRef(
                    id = 2,
                    localImagePath = "/cache/kept.jpg",
                    localImageUrl = url,
                ),
                EditionImageRef(
                    id = 3,
                    localImagePath = "/cache/stale.jpg",
                    localImageUrl = "https://example.com/old.jpg",
                ),
                EditionImageRef(
                    id = 4,
                    localImagePath = "/cache/legacy.jpg",
                    localImageUrl = null,
                ),
            )

            // ----- Act -----
            val result = planEditionCaching(
                editions = listOf(editionNew, editionPreserved, editionStale, editionLegacyStale, editionNoRef),
                existingRefs = refs,
            )

            // ----- Assert -----
            result.entities.size shouldBe 5
            result.stalePaths shouldBe listOf("/cache/stale.jpg", "/cache/legacy.jpg")
        }

        @Test
        fun `stale paths appear in the same order as the editions that produced them`() {
            // ----- Arrange -----
            val editionA = stubBookEdition(
                id = 1,
                url = "https://example.com/new-a.jpg",
            )
            val editionB = stubBookEdition(
                id = 2,
                url = "https://example.com/new-b.jpg",
            )

            val refs = listOf(
                EditionImageRef(
                    id = 1,
                    localImagePath = "/cache/a.jpg",
                    localImageUrl = "https://example.com/old-a.jpg",
                ),
                EditionImageRef(
                    id = 2,
                    localImagePath = "/cache/b.jpg",
                    localImageUrl = "https://example.com/old-b.jpg",
                ),
            )

            // ----- Act -----
            val result = planEditionCaching(
                editions = listOf(editionA, editionB),
                existingRefs = refs,
            )

            // ----- Assert -----
            result.stalePaths shouldBe listOf("/cache/a.jpg", "/cache/b.jpg")
        }

        @Test
        fun `empty editions list produces empty plan`() {
            // ----- Arrange -----
            // (nothing to arrange)

            // ----- Act -----
            val result = planEditionCaching(
                editions = emptyList(),
                existingRefs = listOf(
                    EditionImageRef(
                        id = 1,
                        localImagePath = "/cache/orphan.jpg",
                        localImageUrl = null,
                    ),
                ),
            )

            // ----- Assert -----
            result.entities shouldBe emptyList()
            result.stalePaths shouldBe emptyList()
        }
    }
}
