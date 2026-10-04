package nl.rhaydus.softcover.core.component.sheet

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SheetChromeTest {
    private fun chrome(
        canDismiss: Boolean,
        dismissStarted: Boolean = false,
        close: () -> Unit,
    ) = SheetChrome(
        busy = false,
        canDismiss = canDismiss,
        dismissStarted = dismissStarted,
        close = close,
    )

    @Nested
    inner class Dismiss {
        @Test
        fun `dismiss closes the sheet when it can be dismissed`() {
            // ----- Arrange -----
            var closes = 0
            val chrome = chrome(
                canDismiss = true,
                close = { closes++ },
            )

            // ----- Act -----
            chrome.dismiss()

            // ----- Assert -----
            closes shouldBe 1
        }

        @Test
        fun `dismiss does nothing when the sheet cannot be dismissed`() {
            // ----- Arrange -----
            var closes = 0
            val chrome = chrome(
                canDismiss = false,
                close = { closes++ },
            )

            // ----- Act -----
            chrome.dismiss()

            // ----- Assert -----
            closes shouldBe 0
            chrome.unlocked shouldBe true
        }

        @Test
        fun `the chrome is locked once a dismiss has started`() {
            // ----- Act -----
            val chrome = chrome(
                canDismiss = true,
                dismissStarted = true,
                close = {},
            )

            // ----- Assert -----
            chrome.unlocked shouldBe false
        }
    }
}
