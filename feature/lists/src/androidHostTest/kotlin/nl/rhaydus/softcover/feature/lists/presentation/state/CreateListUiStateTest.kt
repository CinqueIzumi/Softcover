package nl.rhaydus.softcover.feature.lists.presentation.state

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CreateListUiStateTest {
    @Nested
    inner class WithSubmitting {
        @Test
        fun `disables the name field while submitting and re-enables it afterwards`() {
            // ----- Arrange -----
            val state = CreateListUiState()

            // ----- Act -----
            val submitting = state.withSubmitting(submitting = true)
            val finished = submitting.withSubmitting(submitting = false)

            // ----- Assert -----
            submitting.isSubmitting shouldBe true
            submitting.nameField.enabled shouldBe false
            finished.isSubmitting shouldBe false
            finished.nameField.enabled shouldBe true
        }
    }

    @Nested
    inner class CanSubmit {
        @Test
        fun `is false when name is empty or whitespace only`() {
            // ----- Arrange -----
            val empty = CreateListUiState().withName(newName = "")
            val blank = CreateListUiState().withName(newName = "   ")

            // ----- Act & Assert -----
            empty.canSubmit shouldBe false
            blank.canSubmit shouldBe false
        }

        @Test
        fun `is false when isSubmitting is true`() {
            // ----- Arrange -----
            val state = CreateListUiState()
                .withName(newName = "My List")
                .withSubmitting(submitting = true)

            // ----- Act & Assert -----
            state.canSubmit shouldBe false
        }

        @Test
        fun `is true when name is non-blank and not submitting`() {
            // ----- Arrange -----
            val state = CreateListUiState().withName(newName = "My List")

            // ----- Act & Assert -----
            state.canSubmit shouldBe true
        }
    }
}
