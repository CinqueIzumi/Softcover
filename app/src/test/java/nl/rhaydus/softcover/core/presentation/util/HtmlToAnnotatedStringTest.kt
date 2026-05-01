package nl.rhaydus.softcover.core.presentation.util

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HtmlToAnnotatedStringTest {

    @Nested
    inner class HtmlToAnnotatedString {

        @Test
        fun `plain text passes through unchanged`() {
            // ----- Act -----
            val result = htmlToAnnotatedString("Hello, world!")

            // ----- Assert -----
            result.text shouldBe "Hello, world!"
        }

        @Test
        fun `empty string produces empty result`() {
            // ----- Act -----
            val result = htmlToAnnotatedString("")

            // ----- Assert -----
            result.text shouldBe ""
        }

        @Test
        fun `br tag becomes newline`() {
            // ----- Act -----
            val result = htmlToAnnotatedString("Line1<br>Line2")

            // ----- Assert -----
            result.text shouldBe "Line1\nLine2"
        }

        @Test
        fun `self-closing br tag becomes newline`() {
            // ----- Act -----
            val result = htmlToAnnotatedString("Line1<br/>Line2")

            // ----- Assert -----
            result.text shouldBe "Line1\nLine2"
        }

        @Test
        fun `p close tag appends double newline`() {
            // ----- Act -----
            val result = htmlToAnnotatedString("<p>First</p><p>Second</p>")

            // ----- Assert -----
            result.text shouldBe "First\n\nSecond"
        }

        @Test
        fun `trailing whitespace after last paragraph is trimmed`() {
            // ----- Act -----
            val result = htmlToAnnotatedString("<p>Text</p>")

            // ----- Assert -----
            result.text shouldBe "Text"
        }

        @Test
        fun `paragraph with br produces paragraph break then blank line`() {
            // ----- Act -----
            val result = htmlToAnnotatedString("<p>First</p><p><br></p><p>Second</p>")

            // ----- Assert -----
            result.text shouldBe "First\n\n\n\n\nSecond"
        }

        @Test
        fun `strong tag applies Bold FontWeight span over correct range`() {
            // ----- Act -----
            val result = htmlToAnnotatedString("Hello <strong>world</strong>!")

            // ----- Assert -----
            result.text shouldBe "Hello world!"
            result.spanStyles.size shouldBe 1
            val span = result.spanStyles[0]
            span.item shouldBe SpanStyle(fontWeight = FontWeight.Bold)
            span.start shouldBe 6
            span.end shouldBe 11
        }

        @Test
        fun `b tag applies Bold FontWeight span over correct range`() {
            // ----- Act -----
            val result = htmlToAnnotatedString("<b>bold</b> text")

            // ----- Assert -----
            result.text shouldBe "bold text"
            result.spanStyles.size shouldBe 1
            val span = result.spanStyles[0]
            span.item shouldBe SpanStyle(fontWeight = FontWeight.Bold)
            span.start shouldBe 0
            span.end shouldBe 4
        }

        @Test
        fun `em tag applies Italic FontStyle span over correct range`() {
            // ----- Act -----
            val result = htmlToAnnotatedString("Hello <em>world</em>!")

            // ----- Assert -----
            result.text shouldBe "Hello world!"
            result.spanStyles.size shouldBe 1
            val span = result.spanStyles[0]
            span.item shouldBe SpanStyle(fontStyle = FontStyle.Italic)
            span.start shouldBe 6
            span.end shouldBe 11
        }

        @Test
        fun `i tag applies Italic FontStyle span over correct range`() {
            // ----- Act -----
            val result = htmlToAnnotatedString("<i>italic</i>")

            // ----- Assert -----
            result.text shouldBe "italic"
            result.spanStyles.size shouldBe 1
            val span = result.spanStyles[0]
            span.item shouldBe SpanStyle(fontStyle = FontStyle.Italic)
            span.start shouldBe 0
            span.end shouldBe 6
        }

        @Test
        fun `amp entity decodes to ampersand`() {
            // ----- Act -----
            val result = htmlToAnnotatedString("Tom &amp; Jerry")

            // ----- Assert -----
            result.text shouldBe "Tom & Jerry"
        }

        @Test
        fun `lt entity decodes to less-than`() {
            // ----- Act -----
            val result = htmlToAnnotatedString("a &lt; b")

            // ----- Assert -----
            result.text shouldBe "a < b"
        }

        @Test
        fun `gt entity decodes to greater-than`() {
            // ----- Act -----
            val result = htmlToAnnotatedString("a &gt; b")

            // ----- Assert -----
            result.text shouldBe "a > b"
        }

        @Test
        fun `quot entity decodes to double quote`() {
            // ----- Act -----
            val result = htmlToAnnotatedString("say &quot;hello&quot;")

            // ----- Assert -----
            result.text shouldBe "say \"hello\""
        }

        @Test
        fun `apos entity decodes to apostrophe`() {
            // ----- Act -----
            val result = htmlToAnnotatedString("it&apos;s")

            // ----- Assert -----
            result.text shouldBe "it's"
        }

        @Test
        fun `nbsp entity decodes to space`() {
            // ----- Act -----
            val result = htmlToAnnotatedString("a&nbsp;b")

            // ----- Assert -----
            result.text shouldBe "a b"
        }

        @Test
        fun `decimal numeric entity decodes apostrophe`() {
            // ----- Act -----
            val result = htmlToAnnotatedString("it&#39;s")

            // ----- Assert -----
            result.text shouldBe "it's"
        }

        @Test
        fun `hex numeric entity decodes apostrophe`() {
            // ----- Act -----
            val result = htmlToAnnotatedString("it&#x27;s")

            // ----- Assert -----
            result.text shouldBe "it's"
        }

        @Test
        fun `hex numeric entity with uppercase X decodes correctly`() {
            // ----- Act -----
            val result = htmlToAnnotatedString("it&#X27;s")

            // ----- Assert -----
            result.text shouldBe "it's"
        }

        @Test
        fun `unknown tag is stripped silently`() {
            // ----- Act -----
            val result = htmlToAnnotatedString("<span>text</span>")

            // ----- Assert -----
            result.text shouldBe "text"
        }

        @Test
        fun `multiple unknown tags are all stripped silently`() {
            // ----- Act -----
            val result = htmlToAnnotatedString("<div><article>text</article></div>")

            // ----- Assert -----
            result.text shouldBe "text"
        }

        @Test
        fun `stray less-than with no closing bracket is appended as literal`() {
            // ----- Act -----
            val result = htmlToAnnotatedString("a < b")

            // ----- Assert -----
            result.text shouldBe "a < b"
        }

        @Test
        fun `plain text has no span styles`() {
            // ----- Act -----
            val result = htmlToAnnotatedString("plain")

            // ----- Assert -----
            result.spanStyles shouldBe emptyList()
        }

        @Test
        fun `sample review round-trips correctly`() {
            // ----- Arrange -----
            val html = "<p>Absolutely brilliant!!</p><p><br></p>" +
                "<p>This was such a unique book and I absolutely loved everything about it.</p>" +
                "<p><br></p>" +
                "<p>The audiobook is the way to go with this one, the narration and voices done by Jeff Hays made this such a great experience and I&#39;ll be getting to the next one ASAP. </p>"

            // ----- Act -----
            val result = htmlToAnnotatedString(html)

            // ----- Assert -----
            result.text.contains("I'll") shouldBe true
            result.text.contains("Absolutely brilliant!!") shouldBe true
            result.text.contains("This was such a unique book") shouldBe true
            result.text.contains("\n\n") shouldBe true
            result.text.last().isWhitespace() shouldBe false
        }
    }
}
