package nl.rhaydus.softcover.core.domain.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TagCategoryTest {
    @Nested
    inner class FromApiString {
        @Test
        fun `Genre maps to GENRE`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = TagCategory.fromApiString("Genre")

            // ----- Assert -----
            result shouldBe TagCategory.GENRE
        }

        @Test
        fun `Content Warning maps to CONTENT_WARNING`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = TagCategory.fromApiString("Content Warning")

            // ----- Assert -----
            result shouldBe TagCategory.CONTENT_WARNING
        }

        @Test
        fun `Mood maps to MOOD`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = TagCategory.fromApiString("Mood")

            // ----- Assert -----
            result shouldBe TagCategory.MOOD
        }

        @Test
        fun `null maps to OTHER`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = TagCategory.fromApiString(null)

            // ----- Assert -----
            result shouldBe TagCategory.OTHER
        }

        @Test
        fun `unknown string maps to OTHER`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = TagCategory.fromApiString("SomethingElse")

            // ----- Assert -----
            result shouldBe TagCategory.OTHER
        }

        @Test
        fun `empty string maps to OTHER`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = TagCategory.fromApiString("")

            // ----- Assert -----
            result shouldBe TagCategory.OTHER
        }

        @Test
        fun `Tag maps to TAG`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = TagCategory.fromApiString("Tag")

            // ----- Assert -----
            result shouldBe TagCategory.TAG
        }
    }

    @Nested
    inner class ApiValueRoundTrip {
        @Test
        fun `GENRE round-trips via apiValue`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = TagCategory.fromApiString(TagCategory.GENRE.apiValue)

            // ----- Assert -----
            result shouldBe TagCategory.GENRE
        }

        @Test
        fun `MOOD round-trips via apiValue`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = TagCategory.fromApiString(TagCategory.MOOD.apiValue)

            // ----- Assert -----
            result shouldBe TagCategory.MOOD
        }

        @Test
        fun `TAG round-trips via apiValue`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = TagCategory.fromApiString(TagCategory.TAG.apiValue)

            // ----- Assert -----
            result shouldBe TagCategory.TAG
        }

        @Test
        fun `CONTENT_WARNING round-trips via apiValue`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = TagCategory.fromApiString(TagCategory.CONTENT_WARNING.apiValue)

            // ----- Assert -----
            result shouldBe TagCategory.CONTENT_WARNING
        }

        @Test
        fun `OTHER round-trips via apiValue`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = TagCategory.fromApiString(TagCategory.OTHER.apiValue)

            // ----- Assert -----
            result shouldBe TagCategory.OTHER
        }
    }

    @Nested
    inner class FromName {
        @Test
        fun `GENRE maps to GENRE`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = TagCategory.fromName("GENRE")

            // ----- Assert -----
            result shouldBe TagCategory.GENRE
        }

        @Test
        fun `CONTENT_WARNING maps to CONTENT_WARNING`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = TagCategory.fromName("CONTENT_WARNING")

            // ----- Assert -----
            result shouldBe TagCategory.CONTENT_WARNING
        }

        @Test
        fun `MOOD maps to MOOD`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = TagCategory.fromName("MOOD")

            // ----- Assert -----
            result shouldBe TagCategory.MOOD
        }

        @Test
        fun `OTHER maps to OTHER`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = TagCategory.fromName("OTHER")

            // ----- Assert -----
            result shouldBe TagCategory.OTHER
        }

        @Test
        fun `null maps to OTHER`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = TagCategory.fromName(null)

            // ----- Assert -----
            result shouldBe TagCategory.OTHER
        }

        @Test
        fun `garbage string maps to OTHER`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = TagCategory.fromName("not_a_real_category")

            // ----- Assert -----
            result shouldBe TagCategory.OTHER
        }

        @Test
        fun `lowercase genre does not match and maps to OTHER`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = TagCategory.fromName("genre")

            // ----- Assert -----
            result shouldBe TagCategory.OTHER
        }
    }
}
