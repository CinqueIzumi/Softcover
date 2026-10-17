package nl.rhaydus.softcover.feature.explore.data.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.GetMoodTagsQuery
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MoodTagMapperTest {
    @Nested
    inner class ToMoodTag {
        @Test
        fun `maps every field and narrows the Long id to an Int`() {
            // ----- Arrange -----
            val tag = GetMoodTagsQuery.Data.Tag(
                __typename = "tags",
                id = 987654321L,
                tag = "Dark",
                slug = "dark-slug",
                count = 128,
            )

            // ----- Act -----
            val result = tag.toMoodTag()

            // ----- Assert -----
            result shouldBe MoodTag(
                id = 987654321,
                label = "Dark",
                slug = "dark-slug",
                bookCount = 128,
            )
        }
    }
}
