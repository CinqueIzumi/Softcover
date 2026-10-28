package nl.rhaydus.softcover.core.corpus.data.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.database.model.BookTraitsEntity
import nl.rhaydus.softcover.core.domain.model.BookTraits
import nl.rhaydus.softcover.core.domain.model.CharacterTrait
import nl.rhaydus.softcover.core.domain.model.Pace
import nl.rhaydus.softcover.core.domain.model.TraitAnswer
import nl.rhaydus.softcover.core.domain.model.WritingFlag
import nl.rhaydus.softcover.core.domain.model.WritingStyle
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookTraitsMappersTest {
    private fun buildEntity(
        pace: String? = null,
        likeable: String? = null,
        writingStyle: String? = null,
        vivid: Boolean = false,
        poetic: Boolean = false,
        unconventional: Boolean = false,
    ) = BookTraitsEntity(
        bookId = 7,
        pace = pace,
        plotCharacterBalance = 40,
        likeable = likeable,
        writingStyle = writingStyle,
        vivid = vivid,
        poetic = poetic,
        unconventional = unconventional,
        updatedAt = 1L,
    )

    @Nested
    inner class RoundTrip {
        @Test
        fun `every pace value survives toEntity then toDomain`() {
            Pace.entries.forEach { pace ->
                // ----- Arrange -----
                val traits = BookTraits(
                    bookId = 7,
                    pace = pace,
                )

                // ----- Act -----
                val result = traits.toEntity(updatedAt = 5L).toDomain()

                // ----- Assert -----
                result shouldBe traits
            }
        }

        @Test
        fun `every writing style survives toEntity then toDomain`() {
            WritingStyle.entries.forEach { style ->
                // ----- Arrange -----
                val traits = BookTraits(
                    bookId = 7,
                    writingStyle = style,
                )

                // ----- Act -----
                val result = traits.toEntity(updatedAt = 5L).toDomain()

                // ----- Assert -----
                result shouldBe traits
            }
        }

        @Test
        fun `every trait answer survives for every character trait`() {
            TraitAnswer.entries.forEach { answer ->
                // ----- Arrange -----
                val traits = BookTraits(
                    bookId = 7,
                    characters = CharacterTrait.entries.associateWith { answer },
                )

                // ----- Act -----
                val result = traits.toEntity(updatedAt = 5L).toDomain()

                // ----- Assert -----
                result shouldBe traits
            }
        }

        @Test
        fun `a fully answered book survives with all flags and balance`() {
            // ----- Arrange -----
            val traits = BookTraits(
                bookId = 7,
                pace = Pace.VARIABLE,
                plotCharacterBalance = 65,
                characters = mapOf(
                    CharacterTrait.LIKEABLE to TraitAnswer.YES,
                    CharacterTrait.LGBTQ to TraitAnswer.COMPLICATED,
                ),
                writingStyle = WritingStyle.DEMANDING,
                writingFlags = WritingFlag.entries.toSet(),
            )

            // ----- Act -----
            val result = traits.toEntity(updatedAt = 5L).toDomain()

            // ----- Assert -----
            result shouldBe traits
        }
    }

    @Nested
    inner class ToEntity {
        @Test
        fun `every pace is stored under its fixed name`() {
            // ----- Arrange -----
            val expected = mapOf(
                Pace.SLOW to "slow",
                Pace.MODERATE to "moderate",
                Pace.FAST to "fast",
                Pace.VARIABLE to "variable",
            )

            // ----- Act -----
            val stored = Pace.entries.associateWith { pace ->
                BookTraits(
                    bookId = 7,
                    pace = pace,
                ).toEntity(updatedAt = 5L).pace
            }

            // ----- Assert -----
            stored shouldBe expected
        }

        @Test
        fun `every writing style is stored under its fixed name`() {
            // ----- Arrange -----
            val expected = mapOf(
                WritingStyle.SIMPLE to "simple",
                WritingStyle.MODERATE to "moderate",
                WritingStyle.DEMANDING to "demanding",
            )

            // ----- Act -----
            val stored = WritingStyle.entries.associateWith { style ->
                BookTraits(
                    bookId = 7,
                    writingStyle = style,
                ).toEntity(updatedAt = 5L).writingStyle
            }

            // ----- Assert -----
            stored shouldBe expected
        }

        @Test
        fun `every trait answer is stored under its fixed name`() {
            // ----- Arrange -----
            val expected = mapOf(
                TraitAnswer.YES to "yes",
                TraitAnswer.COMPLICATED to "complicated",
            )

            // ----- Act -----
            val stored = TraitAnswer.entries.associateWith { answer ->
                BookTraits(
                    bookId = 7,
                    characters = mapOf(CharacterTrait.LIKEABLE to answer),
                ).toEntity(updatedAt = 5L).likeable
            }

            // ----- Assert -----
            stored shouldBe expected
        }
    }

    @Nested
    inner class ToDomain {
        @Test
        fun `unknown stored pace reads as unanswered while other fields still map`() {
            // ----- Arrange -----
            val entity = buildEntity(
                pace = "warp",
                writingStyle = "simple",
            )

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.pace shouldBe null
            result.writingStyle shouldBe WritingStyle.SIMPLE
            result.plotCharacterBalance shouldBe 40
        }

        @Test
        fun `unknown stored writing style reads as unanswered while other fields still map`() {
            // ----- Arrange -----
            val entity = buildEntity(
                pace = "fast",
                writingStyle = "baroque",
            )

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.writingStyle shouldBe null
            result.pace shouldBe Pace.FAST
        }

        @Test
        fun `unknown stored character answer leaves the key absent`() {
            // ----- Arrange -----
            val entity = buildEntity(
                pace = "slow",
                likeable = "maybe",
            )

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.characters shouldBe emptyMap()
            result.pace shouldBe Pace.SLOW
        }
    }
}
