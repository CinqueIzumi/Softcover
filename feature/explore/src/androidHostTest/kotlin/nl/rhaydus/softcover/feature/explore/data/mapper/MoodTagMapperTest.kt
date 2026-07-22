package nl.rhaydus.softcover.feature.explore.data.mapper

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.GetMoodTagsQuery
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag

class MoodTagMapperTest {
    private fun stubTag(
        id: Long = 1L,
        tag: String = "Cozy",
        slug: String = "cozy",
        count: Int = 42,
    ): GetMoodTagsQuery.Data.Tag = GetMoodTagsQuery.Data.Tag(
        __typename = "tags",
        id = id,
        tag = tag,
        slug = slug,
        count = count,
    )

    @Nested
    inner class ToMoodTag {
        @Test
        fun `maps all fields correctly`() {
            // ----- Arrange -----
            val tag = stubTag(
                id = 7L,
                tag = "Dark",
                slug = "dark",
                count = 128,
            )

            // ----- Act -----
            val result = tag.toMoodTag()

            // ----- Assert -----
            result shouldBe MoodTag(
                id = 7,
                label = "Dark",
                slug = "dark",
                bookCount = 128,
            )
        }

        @Test
        fun `converts the Long id to Int`() {
            // ----- Arrange -----
            val tag = stubTag(id = 987654321L)

            // ----- Act -----
            val result = tag.toMoodTag()

            // ----- Assert -----
            result.id shouldBe 987654321
        }

        @Test
        fun `maps tag field to label`() {
            // ----- Arrange -----
            val tag = stubTag(tag = "Heartwarming")

            // ----- Act -----
            val result = tag.toMoodTag()

            // ----- Assert -----
            result.label shouldBe "Heartwarming"
        }

        @Test
        fun `maps count to bookCount`() {
            // ----- Arrange -----
            val tag = stubTag(count = 0)

            // ----- Act -----
            val result = tag.toMoodTag()

            // ----- Assert -----
            result.bookCount shouldBe 0
        }
    }
}
