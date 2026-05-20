package nl.rhaydus.softcover.feature.settings.domain.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SortDirectionTest {

    @Nested
    inner class Flipped {

        @Test
        fun `flipped on ASCENDING returns DESCENDING`() {
            // ----- Arrange -----
            val direction = SortDirection.ASCENDING

            // ----- Act -----
            val result = direction.flipped()

            // ----- Assert -----
            result shouldBe SortDirection.DESCENDING
        }

        @Test
        fun `flipped on DESCENDING returns ASCENDING`() {
            // ----- Arrange -----
            val direction = SortDirection.DESCENDING

            // ----- Act -----
            val result = direction.flipped()

            // ----- Assert -----
            result shouldBe SortDirection.ASCENDING
        }
    }
}
