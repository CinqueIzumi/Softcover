package nl.rhaydus.softcover.feature.book_detail.data.mapper

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.database.model.UserTagVocabularyEntity
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag

class UserTagVocabularyMapperTest {
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

        @Test
        fun `always maps spoiler to false`() {
            // ----- Arrange -----
            val entity = UserTagVocabularyEntity(
                userId = 7,
                category = TagCategory.CONTENT_WARNING.name,
                name = "Violence",
                usageCount = 2,
            )

            // ----- Act -----
            val result = entity.toUserTag()

            // ----- Assert -----
            result.spoiler shouldBe false
        }

        @Test
        fun `maps usageCount to count`() {
            // ----- Arrange -----
            val entity = UserTagVocabularyEntity(
                userId = 7,
                category = TagCategory.GENRE.name,
                name = "Gothic",
                usageCount = 11,
            )

            // ----- Act -----
            val result = entity.toUserTag()

            // ----- Assert -----
            result.count shouldBe 11
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

        @Test
        fun `maps count to usageCount`() {
            // ----- Arrange -----
            val userTag = UserTag(
                name = "Gothic",
                category = TagCategory.GENRE,
                count = 9,
            )

            // ----- Act -----
            val result = userTag.toVocabularyEntity(userId = 3)

            // ----- Assert -----
            result.usageCount shouldBe 9
        }
    }
}
