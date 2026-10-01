package nl.rhaydus.softcover.feature.profile.presentation.state

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProfileSheetScaffoldBuilderTest {
    @Nested
    inner class LogOutSheetScaffold {
        @Test
        fun `has a stacked footer with both a confirm and a stay button`() {
            // ----- Act -----
            val footer = logOutSheetScaffold().footer.shouldNotBeNull()

            // ----- Assert -----
            (footer is SheetFooterUiModel.Stacked) shouldBe true
            footer.primary.shouldNotBeNull()
            footer.secondary.shouldNotBeNull()
        }

        @Test
        fun `both buttons dismiss the sheet through the scaffold so no handler closes it`() {
            // ----- Act -----
            val footer = logOutSheetScaffold().footer.shouldNotBeNull()

            // ----- Assert -----
            footer.primary.shouldNotBeNull().dismissesSheet shouldBe true
            footer.secondary.shouldNotBeNull().dismissesSheet shouldBe true
        }

        @Test
        fun `is idle and has no pending close request`() {
            // ----- Act -----
            val model = logOutSheetScaffold()

            // ----- Assert -----
            model.busy shouldBe false
            model.closeRequested shouldBe false
        }
    }

    @Nested
    inner class ProfileShareSheetScaffold {
        @Test
        fun `has no footer and no dismiss label`() {
            // ----- Act -----
            val model = profileShareSheetScaffold()

            // ----- Assert -----
            model.footer.shouldBeNull()
            model.dismissLabel.shouldBeNull()
        }
    }
}
