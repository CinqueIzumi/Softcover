package nl.rhaydus.softcover.orchestration.presentation

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.presentation.state.ReAuthState
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReAuthSheetMapperTest {
    @Nested
    inner class ToSheetUiModel {
        @Test
        fun `primary is disabled when apiKey is empty`() {
            // ----- Arrange -----
            val state = ReAuthState(apiKey = "")

            // ----- Act -----
            val result = state.toSheetUiModel()

            // ----- Assert -----
            result.scaffold.footer?.primary?.enabled shouldBe false
        }

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
            result.fieldEnabled shouldBe false
        }

        @Test
        fun `idle is not busy and enables the field`() {
            // ----- Arrange -----
            val state = ReAuthState(
                apiKey = "token",
                isSubmitting = false,
            )

            // ----- Act -----
            val result = state.toSheetUiModel()

            // ----- Assert -----
            result.scaffold.busy shouldBe false
            result.fieldEnabled shouldBe true
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
            result.apiKey shouldBe "abc"
            result.errorMessage shouldBe "bad"
        }

        @Test
        fun `errorMessage stays null when absent`() {
            // ----- Arrange -----
            val state = ReAuthState()

            // ----- Act -----
            val result = state.toSheetUiModel()

            // ----- Assert -----
            result.errorMessage shouldBe null
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

        @Test
        fun `no close is requested by default`() {
            // ----- Act -----
            val result = ReAuthState().toSheetUiModel()

            // ----- Assert -----
            result.scaffold.closeRequested shouldBe false
        }

        @Test
        fun `model leaves the secondary enabled while submitting and relies on busy for the scaffold to disable it`() {
            // ----- Arrange -----
            val state = ReAuthState(
                apiKey = "token",
                isSubmitting = true,
            )

            // ----- Act -----
            val result = state.toSheetUiModel()

            // ----- Assert -----
            result.scaffold.footer?.secondary?.enabled shouldBe true
            result.scaffold.footer?.secondary?.dismissesSheet shouldBe false
        }

        @Test
        fun `scaffold is not dismissible`() {
            // ----- Arrange -----
            val state = ReAuthState()

            // ----- Act -----
            val result = state.toSheetUiModel()

            // ----- Assert -----
            result.scaffold.dismissible shouldBe false
        }
    }
}
