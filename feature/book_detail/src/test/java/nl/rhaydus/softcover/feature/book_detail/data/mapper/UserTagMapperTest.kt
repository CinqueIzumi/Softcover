package nl.rhaydus.softcover.feature.book_detail.data.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.FindTagsByUserAndTaggableQuery
import nl.rhaydus.softcover.SaveTagsMutation
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.type.BasicTag
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserTagMapperTest {
    // ----- FindTagsByUserAndTaggableQuery.Data.Tagging helpers -----

    private fun stubTaggingTag(
        tag: String = "Dark",
        count: Int = 5,
        tagCategoryValue: String? = "Mood",
    ): FindTagsByUserAndTaggableQuery.Data.Tagging.Tag =
        FindTagsByUserAndTaggableQuery.Data.Tagging.Tag(
            __typename = "tags",
            id = 1L,
            tag = tag,
            count = count,
            tag_category = FindTagsByUserAndTaggableQuery.Data.Tagging.Tag.Tag_category(
                __typename = "tag_categories",
                category = tagCategoryValue,
            ),
        )

    private fun stubTagging(
        tag: FindTagsByUserAndTaggableQuery.Data.Tagging.Tag = stubTaggingTag(),
        spoiler: Boolean = false,
    ): FindTagsByUserAndTaggableQuery.Data.Tagging =
        FindTagsByUserAndTaggableQuery.Data.Tagging(
            __typename = "taggings",
            id = 100L,
            spoiler = spoiler,
            tag = tag,
        )

    // ----- SaveTagsMutation.Data.UpsertTags.Tag helpers -----

    private fun stubMutationTag(
        tag: String = "Literary Fiction",
        category: String = "Genre",
        count: Int? = 7,
        spoiler: Boolean = false,
    ): SaveTagsMutation.Data.UpsertTags.Tag =
        SaveTagsMutation.Data.UpsertTags.Tag(
            __typename = "tags",
            category = category,
            tag = tag,
            count = count,
            spoiler = spoiler,
        )

    @Nested
    inner class TaggingToUserTag {
        @Test
        fun `maps all fields correctly`() {
            // ----- Arrange -----
            val tagging = stubTagging(
                tag = stubTaggingTag(
                    tag = "Cozy",
                    count = 3,
                    tagCategoryValue = "Mood",
                ),
                spoiler = false,
            )

            // ----- Act -----
            val result = tagging.toUserTag()

            // ----- Assert -----
            result shouldBe UserTag(
                name = "Cozy",
                category = TagCategory.MOOD,
                count = 3,
                spoiler = false,
            )
        }

        @Test
        fun `maps spoiler true`() {
            // ----- Arrange -----
            val tagging = stubTagging(spoiler = true)

            // ----- Act -----
            val result = tagging.toUserTag()

            // ----- Assert -----
            result.spoiler shouldBe true
        }

        @Test
        fun `falls back to OTHER when tag category is null`() {
            // ----- Arrange -----
            val tagging = stubTagging(tag = stubTaggingTag(tagCategoryValue = null))

            // ----- Act -----
            val result = tagging.toUserTag()

            // ----- Assert -----
            result.category shouldBe TagCategory.OTHER
        }
    }

    @Nested
    inner class MutationTagToUserTag {
        @Test
        fun `maps all fields correctly when count is non-null`() {
            // ----- Arrange -----
            val mutationTag = stubMutationTag(
                tag = "Gothic",
                category = "Genre",
                count = 12,
                spoiler = false,
            )

            // ----- Act -----
            val result = mutationTag.toUserTag()

            // ----- Assert -----
            result shouldBe UserTag(
                name = "Gothic",
                category = TagCategory.GENRE,
                count = 12,
                spoiler = false,
            )
        }

        @Test
        fun `defaults count to 0 when count is null`() {
            // ----- Arrange -----
            val mutationTag = stubMutationTag(count = null)

            // ----- Act -----
            val result = mutationTag.toUserTag()

            // ----- Assert -----
            result.count shouldBe 0
        }

        @Test
        fun `maps spoiler true`() {
            // ----- Arrange -----
            val mutationTag = stubMutationTag(spoiler = true)

            // ----- Act -----
            val result = mutationTag.toUserTag()

            // ----- Assert -----
            result.spoiler shouldBe true
        }

        @Test
        fun `falls back to OTHER when category is an unknown string`() {
            // ----- Arrange -----
            val mutationTag = stubMutationTag(category = "UnknownCategory")

            // ----- Act -----
            val result = mutationTag.toUserTag()

            // ----- Assert -----
            result.category shouldBe TagCategory.OTHER
        }
    }

    @Nested
    inner class UserTagToBasicTag {
        @Test
        fun `maps all fields correctly`() {
            // ----- Arrange -----
            val userTag = UserTag(
                name = "Thriller",
                category = TagCategory.GENRE,
                count = 5,
                spoiler = false,
            )

            // ----- Act -----
            val result = userTag.toBasicTag()

            // ----- Assert -----
            result shouldBe BasicTag(
                category = "Genre",
                spoiler = false,
                tag = "Thriller",
            )
        }

        @Test
        fun `maps Content Warning category as its apiValue`() {
            // ----- Arrange -----
            val userTag = UserTag(
                name = "Violence",
                category = TagCategory.CONTENT_WARNING,
                spoiler = true,
            )

            // ----- Act -----
            val result = userTag.toBasicTag()

            // ----- Assert -----
            result.category shouldBe "Content Warning"
        }

        @Test
        fun `maps spoiler true`() {
            // ----- Arrange -----
            val userTag = UserTag(
                name = "Death",
                category = TagCategory.CONTENT_WARNING,
                spoiler = true,
            )

            // ----- Act -----
            val result = userTag.toBasicTag()

            // ----- Assert -----
            result.spoiler shouldBe true
        }

        @Test
        fun `maps name to tag field`() {
            // ----- Arrange -----
            val userTag = UserTag(
                name = "Post-Apocalyptic",
                category = TagCategory.TAG,
            )

            // ----- Act -----
            val result = userTag.toBasicTag()

            // ----- Assert -----
            result.tag shouldBe "Post-Apocalyptic"
        }
    }
}
