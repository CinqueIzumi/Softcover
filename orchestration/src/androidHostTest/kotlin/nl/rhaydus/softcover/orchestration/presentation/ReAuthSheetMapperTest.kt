package nl.rhaydus.softcover.orchestration.presentation

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.presentation.state.ReAuthState
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReAuthSheetMapperTest {
    @Nested
    inner class ToSheetUiModel {
        @Test
        fun `primary is disabled when apiKey is only whitespace`() {
            // ----- Arrange -----
            val state = ReAuthState(apiKey = "   ")

            // ----- Act -----
            val result = state.toSheetUiModel()

            // ----- Assert -----
            result.scaffold.footer?.primary?.enabled shouldBe false
        }

        @Test
        fun `primary is enabled when apiKey is not blank`() {
            // ----- Arrange -----
            val state = ReAuthState(apiKey = "token")

            // ----- Act -----
            val result = state.toSheetUiModel()

            // ----- Assert -----
            result.scaffold.footer?.primary?.enabled shouldBe true
        }

        @Test
        fun `submitting marks the sheet busy and disables the field`() {
            // ----- Arrange -----
            val state = ReAuthState(
                apiKey = "token",
                isSubmitting = true,
            )

            // ----- Act -----
            val result = state.toSheetUiModel()

            // ----- Assert -----
            result.scaffold.busy shouldBe true
            result.apiKeyField.enabled shouldBe false
        }

        @Test
        fun `apiKey and errorMessage are carried through`() {
            // ----- Arrange -----
            val state = ReAuthState(
                apiKey = "abc",
                errorMessage = "bad",
            )

            // ----- Act -----
            val result = state.toSheetUiModel()

            // ----- Assert -----
            result.apiKeyField.text shouldBe "abc"
            result.errorMessage shouldBe "bad"
        }

        @Test
        fun `a close request on the state is carried onto the scaffold`() {
            // ----- Arrange -----
            val state = ReAuthState(closeRequested = true)

            // ----- Act -----
            val result = state.toSheetUiModel()

            // ----- Assert -----
            result.scaffold.closeRequested shouldBe true
        }
    }
}
