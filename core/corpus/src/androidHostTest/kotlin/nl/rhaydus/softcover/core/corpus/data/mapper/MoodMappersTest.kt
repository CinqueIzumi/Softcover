package nl.rhaydus.softcover.core.corpus.data.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.database.model.BookMoodEntity
import nl.rhaydus.softcover.core.database.model.MoodMomentEntity
import nl.rhaydus.softcover.core.domain.model.BookMood
import nl.rhaydus.softcover.core.domain.model.Mood
import nl.rhaydus.softcover.core.domain.model.MoodMoment
import nl.rhaydus.softcover.core.domain.model.MoodWord
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MoodMappersTest {
    @Nested
    inner class BookMoodToDomain {
        @Test
        fun `a preset key reads as the preset mood`() {
            // ----- Arrange -----
            val entity = BookMoodEntity(
                bookId = 1,
                word = "cosy",
                isOwn = false,
                strength = 40,
            )

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result shouldBe BookMood(
                word = MoodWord.Preset(mood = Mood.Cosy),
                strength = 40,
            )
        }

        @Test
        fun `an own word keeps its text even when it matches a preset key`() {
            // ----- Arrange -----
            val entity = BookMoodEntity(
                bookId = 1,
                word = "cosy",
                isOwn = true,
            )

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result shouldBe BookMood(word = MoodWord.Own(text = "cosy"))
        }

        @Test
        fun `an unknown preset key is dropped`() {
            // ----- Arrange -----
            val entity = BookMoodEntity(
                bookId = 1,
                word = "retired-mood",
                isOwn = false,
            )

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.shouldBeNull()
        }
    }

    @Nested
    inner class BookMoodToEntity {
        @Test
        fun `a preset is stored by key and not flagged own`() {
            // ----- Arrange -----
            val mood = BookMood(
                word = MoodWord.Preset(mood = Mood.Sad),
                strength = 10,
            )

            // ----- Act -----
            val result = mood.toEntity(bookId = 7)

            // ----- Assert -----
            result shouldBe BookMoodEntity(
                bookId = 7,
                word = "sad",
                isOwn = false,
                strength = 10,
            )
        }

        @Test
        fun `an own word is stored as typed and flagged own`() {
            // ----- Arrange -----
            val mood = BookMood(word = MoodWord.Own(text = "Windswept"))

            // ----- Act -----
            val result = mood.toEntity(bookId = 7)

            // ----- Assert -----
            result shouldBe BookMoodEntity(
                bookId = 7,
                word = "Windswept",
                isOwn = true,
                strength = null,
            )
        }
    }

    @Nested
    inner class MomentMapping {
        @Test
        fun `a moment survives a round trip through the entity`() {
            // ----- Arrange -----
            val moment = MoodMoment(
                id = 5,
                bookId = 2,
                word = MoodWord.Own(text = "Giddy"),
                strength = 80,
                percent = 12,
                page = 30,
                seconds = 600,
                recordedAt = 999L,
            )

            // ----- Act -----
            val result = moment.toEntity().toDomain()

            // ----- Assert -----
            result shouldBe moment
        }

        @Test
        fun `a moment with an unknown preset key is dropped`() {
            // ----- Arrange -----
            val entity = MoodMomentEntity(
                bookId = 2,
                word = "nope",
                isOwn = false,
                recordedAt = 1L,
            )

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.shouldBeNull()
        }
    }
}
