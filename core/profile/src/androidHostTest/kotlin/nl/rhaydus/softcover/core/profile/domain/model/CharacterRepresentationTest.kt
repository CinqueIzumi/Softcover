package nl.rhaydus.softcover.core.profile.domain.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CharacterRepresentationTest {
    @Nested
    inner class ExcludingNotAnswered {
        @Test
        fun `drops the not answered books from each trait and keeps the answered ones`() {
            // ----- Arrange -----
            val original = CharacterRepresentation(
                bipoc = CharacterBreakdown(
                    yesCount = 2,
                    complicatedCount = 1,
                    noCount = 3,
                    notAnsweredCount = 4,
                ),
                lgbtq = CharacterBreakdown(
                    yesCount = 1,
                    complicatedCount = 0,
                    noCount = 5,
                    notAnsweredCount = 4,
                ),
            )

            // ----- Act -----
            val result = original.excludingNotAnswered()

            // ----- Assert -----
            result.bipoc shouldBe CharacterBreakdown(
                yesCount = 2,
                complicatedCount = 1,
                noCount = 3,
            )

            result.lgbtq shouldBe CharacterBreakdown(
                yesCount = 1,
                noCount = 5,
            )

            result.bipoc.total shouldBe 6
            result.lgbtq.total shouldBe 6
        }

        @Test
        fun `a representation nobody answered collapses to total zero`() {
            // ----- Arrange -----
            val original = CharacterRepresentation(
                bipoc = CharacterBreakdown(notAnsweredCount = 7),
                lgbtq = CharacterBreakdown(notAnsweredCount = 7),
            )

            // ----- Act -----
            val result = original.excludingNotAnswered()

            // ----- Assert -----
            result.bipoc.total shouldBe 0
            result.lgbtq.total shouldBe 0
        }
    }
}
