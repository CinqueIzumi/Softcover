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
            // ----- Act -----
            val tile = tag(
                id = 5,
                label = "cosy mystery",
            ).toMoodTile(index = 0)

            // ----- Assert -----
            tile.key shouldBe 5
            tile.title shouldBe "Cosy Mystery"
        }

        @Test
        fun `formats the book count with grouping`() {
            // ----- Act -----
            val tile = tag(bookCount = 1234567).toMoodTile(index = 0)

            // ----- Assert -----
            tile.countLabel shouldBe "${formatGroupedNumber(1234567)} books"
        }

        @Test
        fun `cycles the ink Cosy, Dread, Neutral, HeartWrench by index`() {
            // ----- Act -----
            val inks = (0..3).map { tag().toMoodTile(index = it).ink }

            // ----- Assert -----
            inks shouldBe listOf(
                MoodTileInk.Cosy,
                MoodTileInk.Dread,
                MoodTileInk.Neutral,
                MoodTileInk.HeartWrench,
            )
        }

        @Test
        fun `repeats the ink cycle from the fifth tile onward`() {
            // ----- Act -----
            val inks = (4..7).map { tag().toMoodTile(index = it).ink }

            // ----- Assert -----
            inks shouldBe listOf(
                MoodTileInk.Cosy,
                MoodTileInk.Dread,
                MoodTileInk.Neutral,
                MoodTileInk.HeartWrench,
            )
        }
    }
}
