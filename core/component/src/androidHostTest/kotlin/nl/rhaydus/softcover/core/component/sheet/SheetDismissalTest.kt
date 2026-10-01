package nl.rhaydus.softcover.core.component.sheet

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SheetDismissalTest {
    @Nested
    inner class Start {
        @Test
        fun `a new dismissal has not started`() {
            // ----- Act -----
            val dismissal = SheetDismissal(hide = {})

            // ----- Assert -----
            dismissal.started shouldBe false
        }

        @Test
        fun `start latches and hides once`() {
            // ----- Arrange -----
            var hides = 0
            val dismissal = SheetDismissal(hide = { hides++ })

            // ----- Act -----
            dismissal.start()

            // ----- Assert -----
            dismissal.started shouldBe true
            hides shouldBe 1
        }

        @Test
        fun `repeated starts hide only once`() {
            // ----- Arrange -----
            var hides = 0
            val dismissal = SheetDismissal(hide = { hides++ })

            // ----- Act -----
            dismissal.start()
            dismissal.start()
            dismissal.start()

            // ----- Assert -----
            hides shouldBe 1
        }
    }
}
