package nl.rhaydus.softcover.feature.settings.presentation.screen

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.callout.CalloutEvent
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HandleAppUpdateCalloutEventTest {
    @Nested
    inner class HandleAppUpdateCalloutEvent {
        @Test
        fun `starts the app update once on ActionClicked`() {
            // ----- Arrange -----
            var startCount = 0

            // ----- Act -----
            handleAppUpdateCalloutEvent(
                event = CalloutEvent.ActionClicked,
                onStartAppUpdate = { startCount++ },
            )

            // ----- Assert -----
            startCount shouldBe 1
        }

        @Test
        fun `does not start the app update on DismissClicked`() {
            // ----- Arrange -----
            var startCount = 0

            // ----- Act -----
            handleAppUpdateCalloutEvent(
                event = CalloutEvent.DismissClicked,
                onStartAppUpdate = { startCount++ },
            )

            // ----- Assert -----
            startCount shouldBe 0
        }
    }
}
