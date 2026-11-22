package nl.rhaydus.softcover.core.corpus.data.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.database.model.DidNotFinishReasonsEntity
import nl.rhaydus.softcover.core.domain.model.DidNotFinishReason
import nl.rhaydus.softcover.core.domain.model.DidNotFinishReasons
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DidNotFinishReasonsMappersTest {
    @Nested
    inner class ToDomain {
        @Test
        fun `each flag column reads as exactly its own reason`() {
            // ----- Arrange -----
            val entities = mapOf(
                DidNotFinishReason.NotTheRightTime to entity(notTheRightTime = true),
                DidNotFinishReason.NotMyKindOfWriting to entity(notMyKindOfWriting = true),
                DidNotFinishReason.TooSlow to entity(tooSlow = true),
                DidNotFinishReason.LostInterest to entity(lostInterest = true),
                DidNotFinishReason.UnwantedContent to entity(unwantedContent = true),
            )

            entities.forEach { (reason, entity) ->
                // ----- Act -----
                val result = entity.toDomain()

                // ----- Assert -----
                result.reasons shouldBe setOf(reason)
            }
        }

        @Test
        fun `no flag set reads as no reasons`() {
            // ----- Arrange -----
            val entity = entity()

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result shouldBe DidNotFinishReasons(bookId = 7)
        }

        @Test
        fun `all reasons with own words survive toEntity then toDomain`() {
            // ----- Arrange -----
            val reasons = DidNotFinishReasons(
                bookId = 7,
                reasons = DidNotFinishReason.entries.toSet(),
                ownWords = "Just not for me",
            )

            // ----- Act -----
            val result = reasons.toEntity(updatedAt = 5L).toDomain()

            // ----- Assert -----
            result shouldBe reasons
        }
    }

    @Nested
    inner class ToEntity {
        @Test
        fun `each reason sets exactly its own column`() {
            // ----- Arrange -----
            val expected = mapOf(
                DidNotFinishReason.NotTheRightTime to entity(notTheRightTime = true),
                DidNotFinishReason.NotMyKindOfWriting to entity(notMyKindOfWriting = true),
                DidNotFinishReason.TooSlow to entity(tooSlow = true),
                DidNotFinishReason.LostInterest to entity(lostInterest = true),
                DidNotFinishReason.UnwantedContent to entity(unwantedContent = true),
            )

            expected.forEach { (reason, entity) ->
                // ----- Act -----
                val result = DidNotFinishReasons(
                    bookId = 7,
                    reasons = setOf(reason),
                ).toEntity(updatedAt = 9L)

                // ----- Assert -----
                result shouldBe entity.copy(updatedAt = 9L)
            }
        }
    }

    private fun entity(
        notTheRightTime: Boolean = false,
        notMyKindOfWriting: Boolean = false,
        tooSlow: Boolean = false,
        lostInterest: Boolean = false,
        unwantedContent: Boolean = false,
        ownWords: String? = null,
    ) = DidNotFinishReasonsEntity(
        bookId = 7,
        notTheRightTime = notTheRightTime,
        notMyKindOfWriting = notMyKindOfWriting,
        tooSlow = tooSlow,
        lostInterest = lostInterest,
        unwantedContent = unwantedContent,
        ownWords = ownWords,
        updatedAt = 1L,
    )
}
