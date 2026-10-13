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
    }

    @Nested
    inner class ApiValueRoundTrip {
        @Test
        fun `every category round-trips via apiValue`() {
            // ----- Arrange -----
            val categories = TagCategory.entries

            // ----- Act -----
            val result = categories.map { TagCategory.fromApiString(it.apiValue) }

            // ----- Assert -----
            result shouldBe categories
        }
    }

    @Nested
    inner class FromName {
        @Test
        fun `CONTENT_WARNING maps to CONTENT_WARNING`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = TagCategory.fromName("CONTENT_WARNING")

            // ----- Assert -----
            result shouldBe TagCategory.CONTENT_WARNING
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
