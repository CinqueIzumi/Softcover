package nl.rhaydus.softcover.core.book.data.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.SaveTagsMutation
import nl.rhaydus.softcover.core.database.model.BookUserTagEntity
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserTagMapperTest {
    @Nested
    inner class ToBasicTag {
        @Test
        fun `sends the category as its API string, not the display label`() {
            // ----- Arrange -----
            val tag = UserTag(
                name = "Gore",
                category = TagCategory.CONTENT_WARNING,
                spoiler = true,
            )

            // ----- Act -----
            val result = tag.toBasicTag()

            // ----- Assert -----
            result.category shouldBe "Content Warning"
            result.tag shouldBe "Gore"
            result.spoiler shouldBe true
        }
    }

    @Nested
    inner class FromSaveTagsEcho {
        @Test
        fun `maps the echoed tag and treats a missing count as zero`() {
            // ----- Arrange -----
            val echoed = SaveTagsMutation.Data.UpsertTags.Tag(
                __typename = "tags",
                category = "Content Warning",
                tag = "Gore",
                count = null,
                spoiler = true,
            )

            // ----- Act -----
            val result = echoed.toUserTag()

            // ----- Assert -----
            result shouldBe UserTag(
                name = "Gore",
                category = TagCategory.CONTENT_WARNING,
                count = 0,
                spoiler = true,
            )
        }

        @Test
        fun `an unrecognised category falls back to other`() {
            // ----- Arrange -----
            val echoed = SaveTagsMutation.Data.UpsertTags.Tag(
                __typename = "tags",
                category = "Something new",
                tag = "Odd",
                count = 3,
                spoiler = false,
            )

            // ----- Act -----
            val result = echoed.toUserTag()

            // ----- Assert -----
            result.category shouldBe TagCategory.OTHER
        }
    }

    @Nested
    inner class EntityRoundTrip {
        @Test
        fun `a stored row restores category, name and spoiler flag and drops the global count`() {
            // ----- Arrange -----
            val tag = UserTag(
                name = "Slow burn",
                category = TagCategory.MOOD,
                count = 120,
                spoiler = true,
            )

            // ----- Act -----
            val result = tag.toEntity(bookId = 5).toUserTag()

            // ----- Assert -----
            result shouldBe tag.copy(count = 0)
        }

        @Test
        fun `a row with an unknown category name falls back to other`() {
            // ----- Arrange -----
            val entity = BookUserTagEntity(
                bookId = 5,
                category = "REMOVED_CATEGORY",
                name = "Legacy",
                spoiler = false,
            )

            // ----- Act -----
            val result = entity.toUserTag()

            // ----- Assert -----
            result.category shouldBe TagCategory.OTHER
        }
    }
}
