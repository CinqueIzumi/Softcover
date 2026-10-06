package nl.rhaydus.softcover.core.uibinding.masthead

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.header.MastheadRefreshUiModel
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MastheadRefreshMapperTest {
    @Nested
    inner class MastheadRefreshFor {
        @Test
        fun `reads Refreshing and is disabled while loading`() {
            // ----- Arrange -----
            val isLoading = true

            // ----- Act -----
            val refresh = mastheadRefreshFor(isLoading = isLoading)

            // ----- Assert -----
            refresh shouldBe MastheadRefreshUiModel(
                label = "Refreshing…",
                enabled = false,
            )
        }

        @Test
        fun `reads Refresh and is enabled when not loading`() {
            // ----- Arrange -----
            val isLoading = false

            // ----- Act -----
            val refresh = mastheadRefreshFor(isLoading = isLoading)

            // ----- Assert -----
            refresh shouldBe MastheadRefreshUiModel(
                label = "Refresh",
                enabled = true,
            )
        }
    }
}
