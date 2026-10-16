package nl.rhaydus.softcover.feature.widget.placed

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WidgetInstanceChangesTest {
    @Nested
    inner class NotifyChanged {
        @Test
        fun `a poke reaches a subscribed collector`() = runTest {
            // ----- Arrange -----
            val instanceChanges = WidgetInstanceChanges()

            instanceChanges.changes.test {
                // ----- Act -----
                instanceChanges.notifyChanged()

                // ----- Assert -----
                awaitItem()
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
