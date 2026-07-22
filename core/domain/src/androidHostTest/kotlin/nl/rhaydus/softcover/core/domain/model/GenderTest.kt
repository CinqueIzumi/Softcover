package nl.rhaydus.softcover.core.domain.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GenderTest {
    @Nested
    inner class FromId {
        @Test
        fun `1 maps to Male`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = Gender.fromId(1)

            // ----- Assert -----
            result shouldBe Gender.Male
        }

        @Test
        fun `2 maps to Female`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = Gender.fromId(2)

            // ----- Assert -----
            result shouldBe Gender.Female
        }

        @Test
        fun `3 maps to Other`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = Gender.fromId(3)

            // ----- Assert -----
            result shouldBe Gender.Other
        }

        @Test
        fun `null maps to Unknown`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = Gender.fromId(null)

            // ----- Assert -----
            result shouldBe Gender.Unknown
        }

        @Test
        fun `unrecognized id 4 maps to Unknown, not Other`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = Gender.fromId(4)

            // ----- Assert -----
            result shouldBe Gender.Unknown
        }

        @Test
        fun `unrecognized id 0 maps to Unknown`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = Gender.fromId(0)

            // ----- Assert -----
            result shouldBe Gender.Unknown
        }
    }

    @Nested
    inner class IdProperty {
        @Test
        fun `Male id is 1`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = Gender.Male.id

            // ----- Assert -----
            result shouldBe 1
        }

        @Test
        fun `Female id is 2`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = Gender.Female.id

            // ----- Assert -----
            result shouldBe 2
        }

        @Test
        fun `Other id is 3`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = Gender.Other.id

            // ----- Assert -----
            result shouldBe 3
        }

        @Test
        fun `Unknown id is null`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = Gender.Unknown.id

            // ----- Assert -----
            result shouldBe null
        }
    }
}
