package nl.rhaydus.softcover.core.profile.domain.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.AcquisitionKind
import nl.rhaydus.softcover.core.domain.model.AcquisitionSource
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookCopy
import nl.rhaydus.softcover.core.profile.domain.model.LanguageCount
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PublicationStatsMapperTest {
    private fun bookIn(
        id: Int,
        language: String?,
    ): Book = readBook(
        id = id,
        editions = listOf(
            edition(
                id = id,
                pages = null,
            ).copy(language = language),
        ),
        editionId = id,
    )

    private fun bookFrom(
        id: Int,
        year: Int,
    ): Book = readBook(id = id).copy(releaseYear = year)

    @Nested
    inner class Languages {
        @Test
        fun `a qualified language groups with its plain name`() {
            // ----- Arrange -----
            val books = listOf(
                bookIn(
                    id = 1,
                    language = "Dutch; Flemish",
                ),
                bookIn(
                    id = 2,
                    language = "Dutch",
                ),
            )

            // ----- Act -----
            val split = books.toLanguageSplit()

            // ----- Assert -----
            split.languages shouldBe listOf(
                LanguageCount(
                    name = "Dutch",
                    books = 2,
                ),
            )
        }

        @Test
        fun `blank and null languages count as unknown`() {
            // ----- Arrange -----
            val books = listOf(
                bookIn(
                    id = 1,
                    language = "  ",
                ),
                bookIn(
                    id = 2,
                    language = null,
                ),
                bookIn(
                    id = 3,
                    language = " ; Flemish",
                ),
            )

            // ----- Act -----
            val split = books.toLanguageSplit()

            // ----- Assert -----
            split.unknown shouldBe 3
            split.languages shouldBe emptyList()
        }

        @Test
        fun `a book without a reader edition is unknown even when another edition has a language`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    editions = listOf(
                        edition(
                            id = 9,
                            pages = null,
                        ).copy(language = "French"),
                    ),
                ),
            )

            // ----- Act -----
            val split = books.toLanguageSplit()

            // ----- Assert -----
            split.unknown shouldBe 1
            split.languages shouldBe emptyList()
        }

        @Test
        fun `languages sort by count descending then by name`() {
            // ----- Arrange -----
            val books = listOf(
                bookIn(
                    id = 1,
                    language = "German",
                ),
                bookIn(
                    id = 2,
                    language = "French",
                ),
                bookIn(
                    id = 3,
                    language = "English",
                ),
                bookIn(
                    id = 4,
                    language = "English",
                ),
            )

            // ----- Act -----
            val split = books.toLanguageSplit()

            // ----- Assert -----
            split.languages shouldBe listOf(
                LanguageCount(
                    name = "English",
                    books = 2,
                ),
                LanguageCount(
                    name = "French",
                    books = 1,
                ),
                LanguageCount(
                    name = "German",
                    books = 1,
                ),
            )
        }
    }

    @Nested
    inner class Decades {
        @Test
        fun `the unknown year is set aside and excluded from the decades and oldest year`() {
            // ----- Arrange -----
            val books = listOf(
                bookFrom(
                    id = 1,
                    year = -1,
                ),
                bookFrom(
                    id = 2,
                    year = 1995,
                ),
            )

            // ----- Act -----
            val decades = books.toPublicationDecades()

            // ----- Assert -----
            decades.unknownYear shouldBe 1
            decades.booksByDecade shouldBe mapOf(1990 to 1)
            decades.oldestYear shouldBe 1995
        }

        @Test
        fun `negative years floor to the decade below`() {
            // ----- Arrange -----
            val books = listOf(
                bookFrom(
                    id = 1,
                    year = -5,
                ),
                bookFrom(
                    id = 2,
                    year = -10,
                ),
                bookFrom(
                    id = 3,
                    year = -11,
                ),
            )

            // ----- Act -----
            val decades = books.toPublicationDecades()

            // ----- Assert -----
            decades.booksByDecade shouldBe mapOf(-10 to 2, -20 to 1)
            decades.oldestYear shouldBe -11
        }

        @Test
        fun `only years from 2000 count as this century`() {
            // ----- Arrange -----
            val books = listOf(
                bookFrom(
                    id = 1,
                    year = 1999,
                ),
                bookFrom(
                    id = 2,
                    year = 2000,
                ),
                bookFrom(
                    id = 3,
                    year = 2024,
                ),
            )

            // ----- Act -----
            val decades = books.toPublicationDecades()

            // ----- Assert -----
            decades.booksThisCentury shouldBe 2
        }

        @Test
        fun `no books leaves no oldest year`() {
            // ----- Arrange -----
            val books = emptyList<Book>()

            // ----- Act -----
            val decades = books.toPublicationDecades()

            // ----- Assert -----
            decades.oldestYear shouldBe null
        }
    }

    @Nested
    inner class Acquisition {
        @Test
        fun `presets group by kind, free text pools into somewhere else and no copy is not noted`() {
            // ----- Arrange -----
            val books = listOf(1, 2, 3, 4, 5).map { readBook(id = it) }

            val copies = mapOf(
                1 to BookCopy(
                    bookId = 1,
                    source = AcquisitionSource.Preset(AcquisitionKind.LIBRARY),
                ),
                2 to BookCopy(
                    bookId = 2,
                    source = AcquisitionSource.Preset(AcquisitionKind.LIBRARY),
                ),
                3 to BookCopy(
                    bookId = 3,
                    source = AcquisitionSource.Other("A friend"),
                ),
                4 to BookCopy(
                    bookId = 4,
                    source = null,
                ),
            )

            // ----- Act -----
            val split = books.toAcquisitionSplit(copies)

            // ----- Assert -----
            split.byKind shouldBe mapOf(AcquisitionKind.LIBRARY to 2)
            split.somewhereElse shouldBe 1
            split.notNoted shouldBe 2
        }
    }
}
