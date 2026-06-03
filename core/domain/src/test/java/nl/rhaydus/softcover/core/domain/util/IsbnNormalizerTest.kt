package nl.rhaydus.softcover.core.domain.util

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class IsbnNormalizerTest {
    @Nested
    inner class Normalize {
        @Test
        fun `hyphenated ISBN-13 strips hyphens and returns canonical digits`() {
            // ----- Arrange -----
            // ----- Act -----
            val result = IsbnNormalizer.normalize("978-0-374-71078-1")

            // ----- Assert -----
            result shouldBe "9780374710781"
        }

        @Test
        fun `already clean ISBN-13 is returned unchanged`() {
            // ----- Arrange -----
            // ----- Act -----
            val result = IsbnNormalizer.normalize("9780374710781")

            // ----- Assert -----
            result shouldBe "9780374710781"
        }

        @Test
        fun `clean ISBN-10 with only digits is returned unchanged`() {
            // ----- Arrange -----
            // ----- Act -----
            val result = IsbnNormalizer.normalize("0374710783")

            // ----- Assert -----
            result shouldBe "0374710783"
        }

        @Test
        fun `lowercase ISBN-10 X check digit is uppercased`() {
            // ----- Arrange -----
            // ----- Act -----
            val result = IsbnNormalizer.normalize("037471078x")

            // ----- Assert -----
            result shouldBe "037471078X"
        }

        @Test
        fun `ISBN-13 with surrounding spaces strips whitespace and returns canonical digits`() {
            // ----- Arrange -----
            // ----- Act -----
            val result = IsbnNormalizer.normalize(" 978 0374710781 ")

            // ----- Assert -----
            result shouldBe "9780374710781"
        }

        @Test
        fun `7-digit input returns null`() {
            // ----- Arrange -----
            // ----- Act -----
            val result = IsbnNormalizer.normalize("1234567")

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `8-digit EAN-8 barcode returns null`() {
            // ----- Arrange -----
            // ----- Act -----
            val result = IsbnNormalizer.normalize("12345678")

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `10-letter non-digit string returns null`() {
            // ----- Arrange -----
            // ----- Act -----
            val result = IsbnNormalizer.normalize("abcdefghij")

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `empty string returns null`() {
            // ----- Arrange -----
            // ----- Act -----
            val result = IsbnNormalizer.normalize("")

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `normalize is idempotent for a valid ISBN-13`() {
            // ----- Arrange -----
            val first = IsbnNormalizer.normalize("9780374710781")

            // ----- Act -----
            val second = IsbnNormalizer.normalize(first!!)

            // ----- Assert -----
            second shouldBe first
        }
    }
}
