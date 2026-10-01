package nl.rhaydus.softcover.core.component.sheet

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SheetChromeTest {
    private fun chrome(
        canDismiss: Boolean,
        dismissal: SheetDismissal,
    ) = SheetChrome(
        busy = false,
        busyDescription = "Loading",
        canDismiss = canDismiss,
        dismissal = dismissal,
    )

    @Nested
    inner class Dismiss {
        @Test
        fun `dismiss hides the sheet when it can be dismissed`() {
            // ----- Arrange -----
            var hides = 0
            val chrome = chrome(
                canDismiss = true,
                dismissal = SheetDismissal(hide = { hides++ }),
            )

            // ----- Act -----
            chrome.dismiss()

            // ----- Assert -----
            hides shouldBe 1
            chrome.unlocked shouldBe false
        }

        @Test
        fun `dismiss does nothing when the sheet cannot be dismissed`() {
            // ----- Arrange -----
            var hides = 0
            val chrome = chrome(
                canDismiss = false,
                dismissal = SheetDismissal(hide = { hides++ }),
            )

            // ----- Act -----
            chrome.dismiss()

            // ----- Assert -----
            hides shouldBe 0
            chrome.unlocked shouldBe true
        }

        @Test
        fun `a second dismiss during the hide does not hide again`() {
            // ----- Arrange -----
            var hides = 0
            val chrome = chrome(
                canDismiss = true,
                dismissal = SheetDismissal(hide = { hides++ }),
            )

            // ----- Act -----
            chrome.dismiss()
            chrome.dismiss()

            // ----- Assert -----
            hides shouldBe 1
        }
    }
}
