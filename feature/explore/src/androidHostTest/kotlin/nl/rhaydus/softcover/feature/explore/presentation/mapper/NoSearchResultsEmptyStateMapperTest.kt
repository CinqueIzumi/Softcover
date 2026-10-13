package nl.rhaydus.softcover.feature.explore.presentation.mapper

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import nl.rhaydus.softcover.core.component.state.EmptyStateVariant
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class NoSearchResultsEmptyStateMapperTest {
    @Nested
    inner class NoSearchResultsEmptyState {
        @Test
        fun `offers a clear-search button in a centered empty state`() {
            // ----- Arrange -----
            // ----- Act -----
            val result = noSearchResultsEmptyState()

            // ----- Assert -----
            val variant = result.variant.shouldBeInstanceOf<EmptyStateVariant.Centered>()
            variant.button.shouldNotBeNull()
        }

        @Test
        fun `gives the title and body for the user to act on`() {
            // ----- Arrange -----
            // ----- Act -----
            val result = noSearchResultsEmptyState()

            // ----- Assert -----
            result.title.isNotBlank() shouldBe true
            result.body.shouldNotBeNull().isNotBlank() shouldBe true
        }
    }
}
