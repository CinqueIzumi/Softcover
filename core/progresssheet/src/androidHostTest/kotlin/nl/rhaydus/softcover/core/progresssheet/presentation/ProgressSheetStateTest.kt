package nl.rhaydus.softcover.core.progresssheet.presentation

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDateTime
import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProgressSheetStateTest {
    @Nested
    inner class Opened {
        @Test
        fun `requests the sheet and clears a stale backdate`() {
            // ----- Arrange -----
            val state = ProgressSheetState(
                readAt = LocalDateTime(
                    2020,
                    1,
                    2,
                    9,
                    15,
                ),
            )

            // ----- Act -----
            val opened = state.opened()

            // ----- Assert -----
            opened.requested shouldBe true
            opened.readAt.shouldBeNull()
        }

        @Test
        fun `keeps the selected tab`() {
            // ----- Arrange -----
            val state = ProgressSheetState(tab = ProgressSheetTab.TIME)

            // ----- Act -----
            val opened = state.opened()

            // ----- Assert -----
            opened.tab shouldBe ProgressSheetTab.TIME
        }
    }
}
