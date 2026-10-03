package nl.rhaydus.softcover.feature.library.presentation.screen.section

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryStandInsTest {
    @Nested
    inner class StandInCard {
        @Test
        fun `every layout gets a stand-in that fits it`() {
            // ----- Arrange & Act & Assert -----
            LibraryGridLayout.entries.forEach { layout ->
                layout.standInCard().fits(layout) shouldBe true
            }
        }
    }
}
