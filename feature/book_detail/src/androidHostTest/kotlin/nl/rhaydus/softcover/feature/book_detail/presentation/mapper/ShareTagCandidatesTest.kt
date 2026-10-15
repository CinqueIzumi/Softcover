package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ShareTagCandidatesTest {
    private fun bookWith(tags: List<Tag>): Book = mockk {
        every {
            this@mockk.tags
        } returns tags
    }

    private fun tag(
        name: String,
        category: TagCategory,
        count: Int = 1,
    ): Tag = Tag(
        id = name.hashCode(),
        name = name,
        category = category,
        count = count,
    )

    @Nested
    inner class BookCandidates {
        @Test
        fun `keeps the five most used tags of a category ordered by count`() {
            // ----- Arrange -----
            val book = bookWith(
                listOf(
                    tag(
                        name = "a",
                        category = TagCategory.GENRE,
                        count = 1,
                    ),
                    tag(
                        name = "b",
                        category = TagCategory.GENRE,
                        count = 6,
                    ),
                    tag(
                        name = "c",
                        category = TagCategory.GENRE,
                        count = 3,
                    ),
                    tag(
                        name = "d",
                        category = TagCategory.GENRE,
                        count = 5,
                    ),
                    tag(
                        name = "e",
                        category = TagCategory.GENRE,
                        count = 2,
                    ),
                    tag(
                        name = "f",
                        category = TagCategory.GENRE,
                        count = 4,
                    ),
                ),
            )

            // ----- Act -----
            val result = book.shareTagCandidates()

            // ----- Assert -----
            result shouldBe mapOf(TagCategory.GENRE to listOf("b", "d", "f", "c", "e"))
        }

        @Test
        fun `keeps every tag of a category that has five or fewer`() {
            // ----- Arrange -----
            val book = bookWith(
                listOf(
                    tag(
                        name = "a",
                        category = TagCategory.MOOD,
                        count = 1,
                    ),
                    tag(
                        name = "b",
                        category = TagCategory.MOOD,
                        count = 2,
                    ),
                ),
            )

            // ----- Act -----
            val result = book.shareTagCandidates()

            // ----- Assert -----
            result[TagCategory.MOOD] shouldBe listOf("b", "a")
        }

        @Test
        fun `lists the categories in tag category order whatever order the tags arrive in`() {
            // ----- Arrange -----
            val book = bookWith(
                listOf(
                    tag(
                        name = "warning",
                        category = TagCategory.CONTENT_WARNING,
                    ),
                    tag(
                        name = "tag",
                        category = TagCategory.TAG,
                    ),
                    tag(
                        name = "mood",
                        category = TagCategory.MOOD,
                    ),
                    tag(
                        name = "genre",
                        category = TagCategory.GENRE,
                    ),
                ),
            )

            // ----- Act -----
            val result = book.shareTagCandidates()

            // ----- Assert -----
            result.keys.toList() shouldBe listOf(
                TagCategory.GENRE,
                TagCategory.MOOD,
                TagCategory.TAG,
                TagCategory.CONTENT_WARNING,
            )
        }

        @Test
        fun `skips the other category when another category has tags`() {
            // ----- Arrange -----
            val book = bookWith(
                listOf(
                    tag(
                        name = "misc",
                        category = TagCategory.OTHER,
                    ),
                    tag(
                        name = "genre",
                        category = TagCategory.GENRE,
                    ),
                ),
            )

            // ----- Act -----
            val result = book.shareTagCandidates()

            // ----- Assert -----
            result shouldBe mapOf(TagCategory.GENRE to listOf("genre"))
        }

        @Test
        fun `keeps the other category when it is the only data`() {
            // ----- Arrange -----
            val book = bookWith(
                listOf(
                    tag(
                        name = "misc",
                        category = TagCategory.OTHER,
                    ),
                ),
            )

            // ----- Act -----
            val result = book.shareTagCandidates()

            // ----- Assert -----
            result shouldBe mapOf(TagCategory.OTHER to listOf("misc"))
        }

        @Test
        fun `offers nothing for a book without tags`() {
            // ----- Arrange -----
            val book = bookWith(emptyList())

            // ----- Act -----
            val result = book.shareTagCandidates()

            // ----- Assert -----
            result shouldBe emptyMap()
        }

        @Test
        fun `lists a name once when it repeats within a category`() {
            // ----- Arrange -----
            val book = bookWith(
                listOf(
                    tag(
                        name = "cozy",
                        category = TagCategory.MOOD,
                        count = 3,
                    ),
                    tag(
                        name = "cozy",
                        category = TagCategory.MOOD,
                        count = 2,
                    ),
                ),
            )

            // ----- Act -----
            val result = book.shareTagCandidates()

            // ----- Assert -----
            result[TagCategory.MOOD] shouldBe listOf("cozy")
        }

        @Test
        fun `lists a name once under the first category in order when it spans categories`() {
            // ----- Arrange -----
            val book = bookWith(
                listOf(
                    tag(
                        name = "cozy",
                        category = TagCategory.TAG,
                    ),
                    tag(
                        name = "slow",
                        category = TagCategory.TAG,
                    ),
                    tag(
                        name = "cozy",
                        category = TagCategory.MOOD,
                    ),
                ),
            )

            // ----- Act -----
            val result = book.shareTagCandidates()

            // ----- Assert -----
            result shouldBe mapOf(
                TagCategory.MOOD to listOf("cozy"),
                TagCategory.TAG to listOf("slow"),
            )
        }

        @Test
        fun `drops a category whose only tags were listed under an earlier category`() {
            // ----- Arrange -----
            val book = bookWith(
                listOf(
                    tag(
                        name = "cozy",
                        category = TagCategory.MOOD,
                    ),
                    tag(
                        name = "cozy",
                        category = TagCategory.TAG,
                    ),
                ),
            )

            // ----- Act -----
            val result = book.shareTagCandidates()

            // ----- Assert -----
            result shouldBe mapOf(TagCategory.MOOD to listOf("cozy"))
        }
    }

    @Nested
    inner class UserCandidates {
        @Test
        fun `excludes spoiler flagged tags`() {
            // ----- Arrange -----
            val userTags = listOf(
                UserTag(
                    name = "twist",
                    category = TagCategory.TAG,
                    spoiler = true,
                ),
                UserTag(
                    name = "cozy",
                    category = TagCategory.MOOD,
                ),
            )

            // ----- Act -----
            val result = userTags.shareTagCandidates()

            // ----- Assert -----
            result shouldBe mapOf(TagCategory.MOOD to listOf("cozy"))
        }

        @Test
        fun `offers nothing when every tag is a spoiler`() {
            // ----- Arrange -----
            val userTags = listOf(
                UserTag(
                    name = "twist",
                    category = TagCategory.TAG,
                    spoiler = true,
                ),
            )

            // ----- Act -----
            val result = userTags.shareTagCandidates()

            // ----- Assert -----
            result shouldBe emptyMap()
        }

        @Test
        fun `keeps every tag of a category without a cap`() {
            // ----- Arrange -----
            val userTags = (1..7).map { index ->
                UserTag(
                    name = "tag$index",
                    category = TagCategory.TAG,
                )
            }

            // ----- Act -----
            val result = userTags.shareTagCandidates()

            // ----- Assert -----
            result[TagCategory.TAG]?.size shouldBe 7
        }

        @Test
        fun `orders categories and skips other unless it is the only data`() {
            // ----- Arrange -----
            val mixed = listOf(
                UserTag(
                    name = "misc",
                    category = TagCategory.OTHER,
                ),
                UserTag(
                    name = "mood",
                    category = TagCategory.MOOD,
                ),
                UserTag(
                    name = "genre",
                    category = TagCategory.GENRE,
                ),
            )
            val onlyOther = mixed.filter { it.category == TagCategory.OTHER }

            // ----- Act -----
            val mixedResult = mixed.shareTagCandidates()
            val otherResult = onlyOther.shareTagCandidates()

            // ----- Assert -----
            mixedResult.keys.toList() shouldBe listOf(TagCategory.GENRE, TagCategory.MOOD)
            otherResult.keys.toList() shouldBe listOf(TagCategory.OTHER)
        }
    }
}
