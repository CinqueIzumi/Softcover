package nl.rhaydus.softcover.core.book.data.mapper

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.exception.UnexpectedApiException
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RecommendationsMapperTest {
    @Nested
    inner class ParseCachedBookIds {
        @Test
        fun `reads Int, Long and Double elements as ids`() {
            // ----- Arrange -----
            val cached = listOf(1, 2L, 3.0)

            // ----- Act -----
            val result = parseCachedBookIds(
                cachedBookIds = cached,
                limit = 10,
            )

            // ----- Assert -----
            result shouldBe listOf(1, 2, 3)
        }

        @Test
        fun `reads numeric strings as ids`() {
            // ----- Arrange -----
            val cached = listOf("7", "8")

            // ----- Act -----
            val result = parseCachedBookIds(
                cachedBookIds = cached,
                limit = 10,
            )

            // ----- Assert -----
            result shouldBe listOf(7, 8)
        }

        @Test
        fun `skips elements that are not ids`() {
            // ----- Arrange -----
            val cached = listOf(1, "abc", null, 2)

            // ----- Act -----
            val result = parseCachedBookIds(
                cachedBookIds = cached,
                limit = 10,
            )

            // ----- Assert -----
            result shouldBe listOf(1, 2)
        }

        @Test
        fun `removes duplicates keeping the first rank`() {
            // ----- Arrange -----
            val cached = listOf(5, 3, 5, "3", 9)

            // ----- Act -----
            val result = parseCachedBookIds(
                cachedBookIds = cached,
                limit = 10,
            )

            // ----- Assert -----
            result shouldBe listOf(5, 3, 9)
        }

        @Test
        fun `caps the result at the limit keeping the order`() {
            // ----- Arrange -----
            val cached = listOf(4, 3, 2, 1)

            // ----- Act -----
            val result = parseCachedBookIds(
                cachedBookIds = cached,
                limit = 2,
            )

            // ----- Assert -----
            result shouldBe listOf(4, 3)
        }

        @Test
        fun `an empty list yields no ids`() {
            // ----- Arrange -----
            val cached = emptyList<Int>()

            // ----- Act -----
            val result = parseCachedBookIds(
                cachedBookIds = cached,
                limit = 10,
            )

            // ----- Assert -----
            result shouldBe emptyList()
        }

        @Test
        fun `throws when the input is not a list`() {
            // ----- Arrange -----
            val cached: Any? = "1,2,3"

            // ----- Act & Assert -----
            shouldThrow<UnexpectedApiException> {
                parseCachedBookIds(
                    cachedBookIds = cached,
                    limit = 10,
                )
            }
        }

        @Test
        fun `throws when the input is null`() {
            // ----- Arrange -----
            val cached: Any? = null

            // ----- Act & Assert -----
            shouldThrow<UnexpectedApiException> {
                parseCachedBookIds(
                    cachedBookIds = cached,
                    limit = 10,
                )
            }
        }
    }
}
