package nl.rhaydus.softcover.feature.book_detail.data.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.FindUserTagVocabularyQuery
import nl.rhaydus.softcover.core.database.model.UserTagVocabularyEntity
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserTagVocabularyMapperTest {
    private fun stubVocabularyTag(
        tag: String = "emotional",
        tagCategoryValue: String? = "Mood",
        aggregateCount: Int? = 111,
    ): FindUserTagVocabularyQuery.Data.Tag =
        FindUserTagVocabularyQuery.Data.Tag(
            __typename = "tags",
            id = 1L,
            tag = tag,
            tag_category = FindUserTagVocabularyQuery.Data.Tag.Tag_category(
                __typename = "tag_categories",
                category = tagCategoryValue,
            ),
            taggings_aggregate = FindUserTagVocabularyQuery.Data.Tag.Taggings_aggregate(
                __typename = "taggings_aggregate",
                aggregate = aggregateCount?.let {
                    FindUserTagVocabularyQuery.Data.Tag.Taggings_aggregate.Aggregate(
                        __typename = "taggings_aggregate_fields",
                        count = it,
                    )
                },
            ),
        )

    @Nested
    inner class EntityToUserTag {
        @Test
        fun `maps all fields correctly`() {
            // ----- Arrange -----
            val entity = UserTagVocabularyEntity(
                userId = 7,
                category = TagCategory.MOOD.name,
                name = "Cozy",
                usageCount = 4,
            )

            // ----- Act -----
            val result = entity.toUserTag()

            // ----- Assert -----
            result shouldBe UserTag(
                name = "Cozy",
                category = TagCategory.MOOD,
                count = 4,
                spoiler = false,
            )
        }

        @Test
        fun `falls back to OTHER when category name is unknown`() {
            // ----- Arrange -----
            val entity = UserTagVocabularyEntity(
                userId = 7,
                category = "NOT_A_REAL_CATEGORY",
                name = "Cozy",
                usageCount = 1,
            )

            // ----- Act -----
            val result = entity.toUserTag()

            // ----- Assert -----
            result.category shouldBe TagCategory.OTHER
        }
    }

    @Nested
    inner class UserTagToVocabularyEntity {
        @Test
        fun `maps all fields correctly`() {
            // ----- Arrange -----
            val userTag = UserTag(
                name = "Cozy",
                category = TagCategory.MOOD,
                count = 4,
                spoiler = false,
            )

            // ----- Act -----
            val result = userTag.toVocabularyEntity(userId = 7)

            // ----- Assert -----
            result shouldBe UserTagVocabularyEntity(
                userId = 7,
                category = "MOOD",
                name = "Cozy",
                usageCount = 4,
            )
        }

        @Test
        fun `maps category using the enum name rather than the api value`() {
            // ----- Arrange -----
            val userTag = UserTag(
                name = "Violence",
                category = TagCategory.CONTENT_WARNING,
                count = 1,
            )

            // ----- Act -----
            val result = userTag.toVocabularyEntity(userId = 3)

            // ----- Assert -----
            result.category shouldBe "CONTENT_WARNING"
        }
    }

    @Nested
    inner class QueryTagToUserTag {
        @Test
        fun `maps the tag name, category and the personal aggregate count`() {
            // ----- Arrange -----
            val tag = stubVocabularyTag(
                tag = "emotional",
                tagCategoryValue = "Mood",
                aggregateCount = 111,
            )

            // ----- Act -----
            val result = tag.toUserTag()

            // ----- Assert -----
            result shouldBe UserTag(
                name = "emotional",
                category = TagCategory.MOOD,
                count = 111,
                spoiler = false,
            )
        }

        @Test
        fun `defaults count to 0 when the taggings aggregate is null`() {
            // ----- Arrange -----
            val tag = stubVocabularyTag(aggregateCount = null)

            // ----- Act -----
            val result = tag.toUserTag()

            // ----- Assert -----
            result.count shouldBe 0
        }
    }
}
