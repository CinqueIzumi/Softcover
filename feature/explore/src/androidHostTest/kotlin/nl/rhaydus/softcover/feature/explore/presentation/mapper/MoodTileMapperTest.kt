package nl.rhaydus.softcover.feature.explore.presentation.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.softcover.core.component.mood.MoodTileInk
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MoodTileMapperTest {
    private fun tag(
        id: Int = 5,
        label: String = "cosy mystery",
        bookCount: Int = 1234,
    ): MoodTag = MoodTag(
        id = id,
        label = label,
        slug = "slug",
        bookCount = bookCount,
    )

    @Nested
    inner class ToMoodTile {
        @Test
        fun `keys by tag id and title-cases the label`() {
            // ----- Arrange -----
            val input = tag(
                id = 5,
                label = "cosy mystery",
            )

            // ----- Act -----
            val tile = input.toMoodTile(index = 0)

            // ----- Assert -----
            tile.key shouldBe 5
            tile.title shouldBe "Cosy Mystery"
        }

        @Test
        fun `formats the book count with grouping`() {
            // ----- Arrange -----
            val bookCount = 1234567
            val input = tag(bookCount = bookCount)

            // ----- Act -----
            val tile = input.toMoodTile(index = 0)

            // ----- Assert -----
            tile.countLabel shouldBe "${formatGroupedNumber(bookCount)} books"
        }

        @Test
        fun `cycles the ink Cosy, Dread, Neutral, HeartWrench by index and repeats it from the fifth tile`() {
            // ----- Arrange -----
            val input = tag()
            val indices = 0..7

            // ----- Act -----
            val inks = indices.map { index -> input.toMoodTile(index = index).ink }

            // ----- Assert -----
            inks shouldBe listOf(
                MoodTileInk.Cosy,
                MoodTileInk.Dread,
                MoodTileInk.Neutral,
                MoodTileInk.HeartWrench,
                MoodTileInk.Cosy,
                MoodTileInk.Dread,
                MoodTileInk.Neutral,
                MoodTileInk.HeartWrench,
            )
        }
    }
}
