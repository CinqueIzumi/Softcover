package nl.rhaydus.softcover.feature.profile.presentation.state

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HideBooksWithoutDetailToggleModelTest {
    @Nested
    inner class HideBooksWithoutDetailToggleFor {
        @Test
        fun `is checked exactly when hiding`() {
            // ----- Arrange -----
            val hidingValues = listOf(true, false)

            // ----- Act -----
            val checkedValues = hidingValues.map { hideBooksWithoutDetailToggleFor(hide = it).checked }

            // ----- Assert -----
            checkedValues shouldBe listOf(true, false)
        }

        @Test
        fun `keeps the same key whatever the state so the event can be routed`() {
            // ----- Arrange -----
            val hidingValues = listOf(true, false)

            // ----- Act -----
            val keys = hidingValues.map { hideBooksWithoutDetailToggleFor(hide = it).key }

            // ----- Assert -----
            keys.distinct().size shouldBe 1
        }
    }
}
