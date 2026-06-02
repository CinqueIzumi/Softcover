package nl.rhaydus.softcover.core.database.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReviewSlateMapperTest {

    // ----- Shared fixture — the golden document from the spec -----

    private val goldenDocument = ReviewDocument(
        paragraphs = listOf(
            ReviewParagraph(runs = listOf(
                ReviewRun(text = "Loved", bold = true),
                ReviewRun(text = " it a ton. "),
            )),
            ReviewParagraph(runs = listOf(
                ReviewRun(text = "Will be reading more by "),
                ReviewRun(text = "Kylie Lee Baker", italic = true),
                ReviewRun(text = "."),
            )),
            ReviewParagraph(runs = listOf(
                ReviewRun(text = "Test", spoiler = true),
            )),
        ),
    )

    private val goldenSlate: List<Map<String, Any?>> = listOf(
        mapOf(
            "data" to emptyMap<String, Any?>(),
            "type" to "paragraph",
            "object" to "block",
            "children" to listOf(
                mapOf("object" to "text", "text" to "Loved", "bold" to true),
                mapOf("object" to "text", "text" to " it a ton. "),
            ),
        ),
        mapOf(
            "data" to emptyMap<String, Any?>(),
            "type" to "paragraph",
            "object" to "block",
            "children" to listOf(
                mapOf("object" to "text", "text" to "Will be reading more by "),
                mapOf("object" to "text", "text" to "Kylie Lee Baker", "italic" to true),
                mapOf("object" to "text", "text" to "."),
            ),
        ),
        mapOf(
            "data" to emptyMap<String, Any?>(),
            "type" to "paragraph",
            "object" to "block",
            "children" to listOf(
                mapOf("object" to "text", "text" to "Test", "spoiler" to true),
            ),
        ),
    )

    // ----- reviewSlateFromDocument -----

    @Nested
    inner class ReviewSlateFromDocument {

        @Test
        fun `golden document produces the exact golden slate`() {
            // ----- Act & Assert -----
            reviewSlateFromDocument(goldenDocument) shouldBe goldenSlate
        }

        @Test
        fun `block data key is an empty map`() {
            // ----- Arrange -----
            val doc = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("Hello")))))

            // ----- Act -----
            val block = reviewSlateFromDocument(doc)[0]

            // ----- Assert -----
            block["data"] shouldBe emptyMap<String, Any?>()
        }

        @Test
        fun `block type is paragraph and object is block`() {
            // ----- Arrange -----
            val doc = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("Hello")))))

            // ----- Act -----
            val block = reviewSlateFromDocument(doc)[0]

            // ----- Assert -----
            block["type"] shouldBe "paragraph"
            block["object"] shouldBe "block"
        }

        @Test
        fun `bold flag is only emitted when true`() {
            // ----- Arrange -----
            val doc = ReviewDocument(listOf(
                ReviewParagraph(listOf(
                    ReviewRun(text = "Bold", bold = true),
                    ReviewRun(text = "Plain"),
                )),
            ))

            // ----- Act -----
            val children = reviewSlateFromDocument(doc)[0]["children"] as List<*>
            val boldChild = children[0] as Map<*, *>
            val plainChild = children[1] as Map<*, *>

            // ----- Assert -----
            boldChild["bold"] shouldBe true
            plainChild.containsKey("bold") shouldBe false
        }

        @Test
        fun `italic flag is only emitted when true`() {
            // ----- Arrange -----
            val doc = ReviewDocument(listOf(
                ReviewParagraph(listOf(
                    ReviewRun(text = "Italic", italic = true),
                    ReviewRun(text = "Plain"),
                )),
            ))

            // ----- Act -----
            val children = reviewSlateFromDocument(doc)[0]["children"] as List<*>
            val italicChild = children[0] as Map<*, *>
            val plainChild = children[1] as Map<*, *>

            // ----- Assert -----
            italicChild["italic"] shouldBe true
            plainChild.containsKey("italic") shouldBe false
        }

        @Test
        fun `spoiler flag is only emitted when true`() {
            // ----- Arrange -----
            val doc = ReviewDocument(listOf(
                ReviewParagraph(listOf(
                    ReviewRun(text = "Spoiler", spoiler = true),
                    ReviewRun(text = "Plain"),
                )),
            ))

            // ----- Act -----
            val children = reviewSlateFromDocument(doc)[0]["children"] as List<*>
            val spoilerChild = children[0] as Map<*, *>
            val plainChild = children[1] as Map<*, *>

            // ----- Assert -----
            spoilerChild["spoiler"] shouldBe true
            plainChild.containsKey("spoiler") shouldBe false
        }

        @Test
        fun `empty paragraph emits a single child with empty text`() {
            // ----- Arrange -----
            val doc = ReviewDocument(listOf(ReviewParagraph(runs = emptyList())))

            // ----- Act -----
            val children = reviewSlateFromDocument(doc)[0]["children"] as List<*>

            // ----- Assert -----
            children.size shouldBe 1

            val child = children[0] as Map<*, *>

            child["text"] shouldBe ""
            child["object"] shouldBe "text"
        }

        @Test
        fun `each child node carries object=text`() {
            // ----- Arrange -----
            val doc = ReviewDocument(listOf(
                ReviewParagraph(listOf(ReviewRun("Hello"), ReviewRun("World"))),
            ))

            // ----- Act -----
            val children = reviewSlateFromDocument(doc)[0]["children"] as List<*>

            // ----- Assert -----
            (children[0] as Map<*, *>)["object"] shouldBe "text"
            (children[1] as Map<*, *>)["object"] shouldBe "text"
        }

        @Test
        fun `one block is emitted per paragraph`() {
            // ----- Arrange -----
            val doc = ReviewDocument(listOf(
                ReviewParagraph(listOf(ReviewRun("P1"))),
                ReviewParagraph(listOf(ReviewRun("P2"))),
                ReviewParagraph(listOf(ReviewRun("P3"))),
            ))

            // ----- Act -----
            val result = reviewSlateFromDocument(doc)

            // ----- Assert -----
            result.size shouldBe 3
        }

        @Test
        fun `empty document produces empty list`() {
            // ----- Act & Assert -----
            reviewSlateFromDocument(ReviewDocument.EMPTY) shouldBe emptyList()
        }
    }

    // ----- reviewDocumentFromSlate -----

    @Nested
    inner class ReviewDocumentFromSlate {

        @Test
        fun `parses golden slate back to the expected canonical document`() {
            // ----- Act & Assert -----
            reviewDocumentFromSlate(goldenSlate) shouldBe goldenDocument
        }

        @Test
        fun `null slate returns EMPTY`() {
            // ----- Act & Assert -----
            reviewDocumentFromSlate(null) shouldBe ReviewDocument.EMPTY
        }

        @Test
        fun `non-list slate returns EMPTY`() {
            // ----- Act & Assert -----
            reviewDocumentFromSlate("not a list") shouldBe ReviewDocument.EMPTY
        }

        @Test
        fun `empty slate list returns EMPTY`() {
            // ----- Act & Assert -----
            reviewDocumentFromSlate(emptyList<Any?>()) shouldBe ReviewDocument.EMPTY
        }

        @Test
        fun `block with missing children key produces paragraph with no runs`() {
            // ----- Arrange -----
            val slate = listOf(
                mapOf("data" to emptyMap<String, Any?>(), "type" to "paragraph", "object" to "block"),
            )

            // ----- Act -----
            val result = reviewDocumentFromSlate(slate)

            // ----- Assert -----
            result.paragraphs.size shouldBe 1
            result.paragraphs[0].runs shouldBe emptyList()
        }

        @Test
        fun `child with missing text key is skipped`() {
            // ----- Arrange -----
            val slate = listOf(
                mapOf(
                    "data" to emptyMap<String, Any?>(),
                    "type" to "paragraph",
                    "object" to "block",
                    "children" to listOf(
                        mapOf("object" to "text"),
                        mapOf("object" to "text", "text" to "Hi"),
                    ),
                ),
            )

            // ----- Act -----
            val result = reviewDocumentFromSlate(slate)

            // ----- Assert -----
            result.paragraphs[0].runs.size shouldBe 1
            result.paragraphs[0].runs[0].text shouldBe "Hi"
        }

        @Test
        fun `null element in block list is skipped`() {
            // ----- Arrange -----
            val slate = listOf(
                null,
                mapOf(
                    "data" to emptyMap<String, Any?>(),
                    "type" to "paragraph",
                    "object" to "block",
                    "children" to listOf(mapOf("object" to "text", "text" to "OK")),
                ),
            )

            // ----- Act -----
            val result = reviewDocumentFromSlate(slate)

            // ----- Assert -----
            result.paragraphs.size shouldBe 1
            result.paragraphs[0].runs[0].text shouldBe "OK"
        }

        @Test
        fun `marks default to false when absent from child`() {
            // ----- Arrange -----
            val slate = listOf(
                mapOf(
                    "type" to "paragraph",
                    "object" to "block",
                    "children" to listOf(mapOf("object" to "text", "text" to "Plain")),
                ),
            )

            // ----- Act -----
            val result = reviewDocumentFromSlate(slate)
            val run = result.paragraphs[0].runs[0]

            // ----- Assert -----
            run.bold shouldBe false
            run.italic shouldBe false
            run.spoiler shouldBe false
        }

        @Test
        fun `parses wrapped document shape into the same document as the bare-list golden`() {
            // ----- Arrange -----
            val wrapped: Any? = mapOf(
                "document" to mapOf(
                    "object" to "document",
                    "children" to goldenSlate,
                ),
            )

            // ----- Act -----
            val result = reviewDocumentFromSlate(wrapped)

            // ----- Assert -----
            result shouldBe goldenDocument
        }

        @Test
        fun `parses children-only map shape into the same document as the bare-list golden`() {
            // ----- Arrange -----
            val childrenOnly: Any? = mapOf("children" to goldenSlate)

            // ----- Act -----
            val result = reviewDocumentFromSlate(childrenOnly)

            // ----- Assert -----
            result shouldBe goldenDocument
        }

        @Test
        fun `map with neither document nor children key returns EMPTY`() {
            // ----- Arrange -----
            val noBlocks: Any? = mapOf("object" to "document")

            // ----- Act -----
            val result = reviewDocumentFromSlate(noBlocks)

            // ----- Assert -----
            result shouldBe ReviewDocument.EMPTY
        }

        @Test
        fun `map with document key but no children returns EMPTY`() {
            // ----- Arrange -----
            val missingChildren: Any? = mapOf("document" to mapOf("object" to "document"))

            // ----- Act -----
            val result = reviewDocumentFromSlate(missingChildren)

            // ----- Assert -----
            result shouldBe ReviewDocument.EMPTY
        }
    }

    // ----- Round-trip: document → slate → document -----

    @Nested
    inner class RoundTrip {

        @Test
        fun `golden document round-trips through slate without loss`() {
            // ----- Act & Assert -----
            reviewDocumentFromSlate(reviewSlateFromDocument(goldenDocument)) shouldBe goldenDocument
        }

        @Test
        fun `plain single-paragraph document round-trips`() {
            // ----- Arrange -----
            val doc = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("Simple text.")))))

            // ----- Act -----
            val result = reviewDocumentFromSlate(reviewSlateFromDocument(doc))

            // ----- Assert -----
            result shouldBe doc
        }

        @Test
        fun `empty document round-trips`() {
            // ----- Act & Assert -----
            reviewDocumentFromSlate(reviewSlateFromDocument(ReviewDocument.EMPTY)) shouldBe ReviewDocument.EMPTY
        }
    }

    // ----- toJson / reviewDocumentFromJson -----

    @Nested
    inner class JsonSerialization {

        @Test
        fun `toJson then reviewDocumentFromJson round-trips the golden document`() {
            // ----- Act & Assert -----
            reviewDocumentFromJson(goldenDocument.toJson()) shouldBe goldenDocument
        }

        @Test
        fun `toJson then reviewDocumentFromJson round-trips an empty document`() {
            // ----- Act & Assert -----
            reviewDocumentFromJson(ReviewDocument.EMPTY.toJson()) shouldBe ReviewDocument.EMPTY
        }

        @Test
        fun `reviewDocumentFromJson with null returns null`() {
            // ----- Act & Assert -----
            reviewDocumentFromJson(null) shouldBe null
        }

        @Test
        fun `reviewDocumentFromJson with invalid JSON returns null`() {
            // ----- Act & Assert -----
            reviewDocumentFromJson("{not valid json{{") shouldBe null
        }
    }
}
