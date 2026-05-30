package nl.rhaydus.softcover.feature.books.data.mapper

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReviewSlateMapperTest {

    @Nested
    inner class ReviewSlateFromBody {

        @Test
        fun `single-line body produces exactly one block with correct nested structure`() {
            // ----- Arrange -----
            val line = "This book changed my life."

            // ----- Act -----
            val result = reviewSlateFromBody(line)

            // ----- Assert -----
            result.size shouldBe 1

            val block = result[0]

            block["data"] shouldBe emptyMap<String, Any?>()
            block["object"] shouldBe "block"
            block["type"] shouldBe "paragraph"
            block["children"] shouldBe listOf(
                mapOf("object" to "text", "text" to line),
            )
        }

        @Test
        fun `multi-line body splits on newline and produces one block per line in order`() {
            // ----- Arrange -----
            val line1 = "First paragraph."
            val line2 = "Second paragraph."
            val line3 = "Third paragraph."
            val body = "$line1\n$line2\n$line3"

            // ----- Act -----
            val result = reviewSlateFromBody(body)

            // ----- Assert -----
            result.size shouldBe 3

            result[0]["children"] shouldBe listOf(mapOf("object" to "text", "text" to line1))
            result[1]["children"] shouldBe listOf(mapOf("object" to "text", "text" to line2))
            result[2]["children"] shouldBe listOf(mapOf("object" to "text", "text" to line3))
        }

        @Test
        fun `multi-line body preserves each line's text and fixed block fields on every block`() {
            // ----- Arrange -----
            val line1 = "Alpha"
            val line2 = "Beta"
            val body = "$line1\n$line2"

            // ----- Act -----
            val result = reviewSlateFromBody(body)

            // ----- Assert -----
            for (block in result) {
                block["data"] shouldBe emptyMap<String, Any?>()
                block["object"] shouldBe "block"
                block["type"] shouldBe "paragraph"
            }

            result[0]["children"] shouldBe listOf(mapOf("object" to "text", "text" to line1))
            result[1]["children"] shouldBe listOf(mapOf("object" to "text", "text" to line2))
        }

        @Test
        fun `empty string produces exactly one block whose text is an empty string`() {
            // ----- Arrange -----
            val body = ""

            // ----- Act -----
            val result = reviewSlateFromBody(body)

            // ----- Assert -----
            result.size shouldBe 1

            val block = result[0]

            block["data"] shouldBe emptyMap<String, Any?>()
            block["object"] shouldBe "block"
            block["type"] shouldBe "paragraph"
            block["children"] shouldBe listOf(
                mapOf("object" to "text", "text" to ""),
            )
        }
    }
}
