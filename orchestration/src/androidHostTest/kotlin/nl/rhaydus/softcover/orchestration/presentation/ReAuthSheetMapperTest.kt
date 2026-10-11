package nl.rhaydus.softcover.orchestration.presentation

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.presentation.state.ReAuthState
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReAuthSheetMapperTest {
    @Nested
    inner class ToSheetUiModel {
        @Test
        fun `primary is disabled when apiKey is only whitespace`() {
            // ----- Arrange -----
            val session = SheetSession.Open<ReAuthState, ReAuthLogOut>(model = ReAuthState(apiKey = "   "))

            // ----- Act -----
            val result = session.toSheetUiModel().shouldNotBeNull()

            // ----- Assert -----
            result.scaffold.footer?.primary?.enabled shouldBe false
        }

        @Test
        fun `primary is enabled when apiKey is not blank`() {
            // ----- Arrange -----
            val session = SheetSession.Open<ReAuthState, ReAuthLogOut>(model = ReAuthState(apiKey = "token"))

            // ----- Act -----
            val result = session.toSheetUiModel().shouldNotBeNull()

            // ----- Assert -----
            result.scaffold.footer?.primary?.enabled shouldBe true
        }

        @Test
        fun `submitting marks the sheet busy and disables the field`() {
            // ----- Arrange -----
            val session = SheetSession.Open<ReAuthState, ReAuthLogOut>(
                model = ReAuthState(
                    apiKey = "token",
                    isSubmitting = true,
                ),
            )

            // ----- Act -----
            val result = session.toSheetUiModel().shouldNotBeNull()

            // ----- Assert -----
            result.scaffold.busy shouldBe true
            result.apiKeyField.enabled shouldBe false
        }

        @Test
        fun `apiKey and errorMessage are carried through`() {
            // ----- Arrange -----
            val session = SheetSession.Open<ReAuthState, ReAuthLogOut>(
                model = ReAuthState(
                    apiKey = "abc",
                    errorMessage = "bad",
                ),
            )

            // ----- Act -----
            val result = session.toSheetUiModel().shouldNotBeNull()

            // ----- Assert -----
            result.apiKeyField.text shouldBe "abc"
            result.errorMessage shouldBe "bad"
        }

        @Test
        fun `a requested close is carried onto the scaffold`() {
            // ----- Arrange -----
            val session = SheetSession.Open<ReAuthState, ReAuthLogOut>(
                model = ReAuthState(closeRequested = true),
            )

            // ----- Act -----
            val result = session.toSheetUiModel().shouldNotBeNull()

            // ----- Assert -----
            result.scaffold.closeRequested shouldBe true
        }

        @Test
        fun `an open session without a requested close leaves the scaffold open`() {
            // ----- Arrange -----
            val session = SheetSession.Open<ReAuthState, ReAuthLogOut>(model = ReAuthState())

            // ----- Act -----
            val result = session.toSheetUiModel().shouldNotBeNull()

            // ----- Assert -----
            result.scaffold.closeRequested shouldBe false
        }

        @Test
        fun `a closed session has no sheet`() {
            // ----- Arrange -----
            val session: SheetSession<ReAuthState, ReAuthLogOut> = SheetSession.Closed

            // ----- Act -----
            val result = session.toSheetUiModel()

            // ----- Assert -----
            result.shouldBeNull()
        }
    }
}
