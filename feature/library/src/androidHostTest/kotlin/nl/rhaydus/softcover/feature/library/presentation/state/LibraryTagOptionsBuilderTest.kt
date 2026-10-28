package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryTagOptionsBuilderTest {
    // region Fixtures
    private val rare = Tag(
        id = 1,
        name = "Rare",
        category = TagCategory.GENRE,
    )
    private val common = Tag(
        id = 2,
        name = "Common",
        category = TagCategory.GENRE,
    )
    private val banana = Tag(
        id = 3,
        name = "banana",
        category = TagCategory.GENRE,
    )
    private val apple = Tag(
        id = 4,
        name = "Apple",
        category = TagCategory.GENRE,
    )
    private val hopeful = Tag(
        id = 5,
        name = "Hopeful",
        category = TagCategory.MOOD,
    )
    private val violence = Tag(
        id = 6,
        name = "Violence",
        category = TagCategory.CONTENT_WARNING,
    )

    private fun userTag(
        name: String,
        category: TagCategory = TagCategory.MOOD,
    ) = UserTag(
        name = name,
        category = category,
    )

    private fun buildBook(tags: List<Tag>) = Book(
        id = 1,
        title = "A Book",
        editions = emptyList(),
        defaultEdition = null,
        rating = 0.0,
        description = "",
        releaseYear = 2020,
        coverUrl = "",
        authors = emptyList(),
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = null,
        positionsInSeries = emptyList(),
        isCompilation = false,
        tags = tags,
        userBook = null,
    )
    // endregion
    @Nested
    inner class BuildHardcoverTagOptions {
        @Test
        fun `sorts by count descending`() {
            // ----- Arrange -----
            val books = listOf(
                buildBook(tags = listOf(rare)),
                buildBook(tags = listOf(common)),
                buildBook(tags = listOf(common)),
            )

            // ----- Act -----
            val result = buildHardcoverTagOptions(books = books)

            // ----- Assert -----
            result.map { it.key.name to it.count } shouldBe listOf(
                "Common" to 2,
                "Rare" to 1,
            )
        }

        @Test
        fun `breaks count ties by name ignoring case`() {
            // ----- Arrange -----
            val books = listOf(buildBook(tags = listOf(banana, apple, common)))

            // ----- Act -----
            val result = buildHardcoverTagOptions(books = books)

            // ----- Assert -----
            result.map { it.key.name } shouldBe listOf("Apple", "banana", "Common")
        }

        @Test
        fun `counts a tag repeated on one book once`() {
            // ----- Arrange -----
            val books = listOf(buildBook(tags = listOf(rare, rare)))

            // ----- Act -----
            val result = buildHardcoverTagOptions(books = books)

            // ----- Assert -----
            result.single().count shouldBe 1
        }

        @Test
        fun `keeps tags of every category`() {
            // ----- Arrange -----
            val books = listOf(buildBook(tags = listOf(rare, hopeful, violence)))

            // ----- Act -----
            val result = buildHardcoverTagOptions(books = books)

            // ----- Assert -----
            result.map { it.category }.toSet() shouldBe setOf(
                TagCategory.GENRE,
                TagCategory.MOOD,
                TagCategory.CONTENT_WARNING,
            )
        }

        @Test
        fun `is empty when no book has tags`() {
            // ----- Arrange -----
            val books = listOf(buildBook(tags = emptyList()))

            // ----- Act -----
            val result = buildHardcoverTagOptions(books = books)

            // ----- Assert -----
            result shouldBe emptyList()
        }
    }

    @Nested
    inner class BuildYourTagOptions {
        @Test
        fun `counts books per tag and sorts by count then name ignoring case`() {
            // ----- Arrange -----
            val perBook = listOf(
                listOf(userTag("slow"), userTag("Cozy")),
                listOf(userTag("Cozy")),
                listOf(userTag("alpha")),
            )

            // ----- Act -----
            val result = buildYourTagOptions(userTagsPerBook = perBook)

            // ----- Assert -----
            result.map { it.key.name to it.count } shouldBe listOf(
                "Cozy" to 2,
                "alpha" to 1,
                "slow" to 1,
            )
        }

        @Test
        fun `keeps a same-named tag of another category as a separate option`() {
            // ----- Arrange -----
            val mood = userTag("cozy")
            val genre = UserTag(
                name = "cozy",
                category = TagCategory.GENRE,
            )
            val perBook = listOf(listOf(mood, genre))

            // ----- Act -----
            val result = buildYourTagOptions(userTagsPerBook = perBook)

            // ----- Assert -----
            result.map { it.key.category }.toSet() shouldBe setOf(TagCategory.MOOD, TagCategory.GENRE)
        }

        @Test
        fun `counts a tag repeated on one book once`() {
            // ----- Arrange -----
            val perBook = listOf(listOf(userTag("cozy"), userTag("cozy")))

            // ----- Act -----
            val result = buildYourTagOptions(userTagsPerBook = perBook)

            // ----- Assert -----
            result.single().count shouldBe 1
        }

        @Test
        fun `is empty when no book has own tags`() {
            // ----- Arrange -----
            val perBook = listOf(emptyList<UserTag>(), emptyList())

            // ----- Act -----
            val result = buildYourTagOptions(userTagsPerBook = perBook)

            // ----- Assert -----
            result shouldBe emptyList()
        }
    }
}
