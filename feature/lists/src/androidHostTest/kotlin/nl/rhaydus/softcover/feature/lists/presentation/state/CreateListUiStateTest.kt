package nl.rhaydus.softcover.feature.lists.presentation.state

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CreateListUiStateTest {
    @Nested
    inner class WithName {
        @Test
        fun `stores the name and mirrors it into the name field`() {
            // ----- Arrange -----
            val state = CreateListUiState()

            // ----- Act -----
            val result = state.withName(newName = "Summer")

            // ----- Assert -----
            result.name shouldBe "Summer"
            result.nameField.text shouldBe "Summer"
        }

        @Test
        fun `keeps the field enabled when not submitting`() {
            // ----- Arrange -----
            val state = CreateListUiState()

            // ----- Act -----
            val result = state.withName(newName = "Summer")

            // ----- Assert -----
            result.nameField.enabled shouldBe true
        }

        @Test
        fun `keeps the field disabled while a submission is in flight`() {
            // ----- Arrange -----
            val state = CreateListUiState().withSubmitting(submitting = true)

            // ----- Act -----
            val result = state.withName(newName = "Summer")

            // ----- Assert -----
            result.nameField.enabled shouldBe false
        }
    }

    @Nested
    inner class WithSubmitting {
        @Test
        fun `disables the name field while submitting`() {
            // ----- Arrange -----
            val state = CreateListUiState()

            // ----- Act -----
            val result = state.withSubmitting(submitting = true)

            // ----- Assert -----
            result.isSubmitting shouldBe true
            result.nameField.enabled shouldBe false
        }

        @Test
        fun `re-enables the name field once submitting ends`() {
            // ----- Arrange -----
            val state = CreateListUiState().withSubmitting(submitting = true)

            // ----- Act -----
            val result = state.withSubmitting(submitting = false)

            // ----- Assert -----
            result.isSubmitting shouldBe false
            result.nameField.enabled shouldBe true
        }

        @Test
        fun `keeps the typed name in the field`() {
            // ----- Arrange -----
            val state = CreateListUiState().withName(newName = "Summer")

            // ----- Act -----
            val result = state.withSubmitting(submitting = true)

            // ----- Assert -----
            result.nameField.text shouldBe "Summer"
        }
    }
}
