package nl.rhaydus.softcover.feature.lists.presentation.state

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.isClosing
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.component.sheet.requestClose
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CreateListUiStateTest {
    private fun CreateListUiState.footerPrimary() =
        sheet.modelOrNull.shouldNotBeNull().footer.shouldBeInstanceOf<SheetFooterUiModel.Stacked>().primary.shouldNotBeNull()

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

            // ----- Act -----
            val results = listOf(empty.canSubmit, blank.canSubmit)

            // ----- Assert -----
            results shouldBe listOf(false, false)
        }

        @Test
        fun `is false when isSubmitting is true`() {
            // ----- Arrange -----
            val state = CreateListUiState()
                .withName(newName = "My List")
                .withSubmitting(submitting = true)

            // ----- Act -----
            val result = state.canSubmit

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `is true when name is non-blank and not submitting`() {
            // ----- Arrange -----
            val state = CreateListUiState().withName(newName = "My List")

            // ----- Act -----
            val result = state.canSubmit

            // ----- Assert -----
            result shouldBe true
        }
    }

    @Nested
    inner class SheetFooter {
        @Test
        fun `starts enabled only once a name is typed`() {
            // ----- Arrange -----
            val empty = CreateListUiState()

            // ----- Act -----
            val named = empty.withName(newName = "My List")

            // ----- Assert -----
            empty.footerPrimary().enabled shouldBe false
            named.footerPrimary().enabled shouldBe true
        }

        @Test
        fun `disables the button again when the name is cleared`() {
            // ----- Arrange -----
            val named = CreateListUiState().withName(newName = "My List")

            // ----- Act -----
            val cleared = named.withName(newName = "  ")

            // ----- Assert -----
            cleared.footerPrimary().enabled shouldBe false
        }

        @Test
        fun `runs the button and disables it while submitting`() {
            // ----- Arrange -----
            val named = CreateListUiState().withName(newName = "My List")

            // ----- Act -----
            val submitting = named.withSubmitting(submitting = true)
            val finished = submitting.withSubmitting(submitting = false)

            // ----- Assert -----
            submitting.footerPrimary().running shouldBe true
            submitting.footerPrimary().enabled shouldBe false
            finished.footerPrimary().running shouldBe false
            finished.footerPrimary().enabled shouldBe true
        }

        @Test
        fun `keeps a closing sheet closing while the footer follows the state`() {
            // ----- Arrange -----
            val closing = CreateListUiState()
                .withName(newName = "My List")
                .let { it.copy(sheet = it.sheet.requestClose()) }

            // ----- Act -----
            val result = closing.withSubmitting(submitting = true)

            // ----- Assert -----
            result.sheet.isClosing shouldBe true
            result.footerPrimary().running shouldBe true
        }

        @Test
        fun `does not reopen a closed sheet`() {
            // ----- Arrange -----
            val closed = CreateListUiState().copy(sheet = SheetSession.Closed)

            // ----- Act -----
            val result = closed.withName(newName = "My List")

            // ----- Assert -----
            result.sheet shouldBe SheetSession.Closed
        }
    }
}
