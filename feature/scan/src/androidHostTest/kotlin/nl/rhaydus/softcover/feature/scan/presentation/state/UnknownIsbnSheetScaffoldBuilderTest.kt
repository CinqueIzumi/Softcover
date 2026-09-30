package nl.rhaydus.softcover.feature.scan.presentation.state

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UnknownIsbnSheetScaffoldBuilderTest {
    @Nested
    inner class UnknownIsbnSheetScaffoldFor {
        @Test
        fun `primary is loading when adding`() {
            // ----- Arrange -----
            val isAdding = true

            // ----- Act -----
            val result = unknownIsbnSheetScaffoldFor(isAdding = isAdding)

            // ----- Assert -----
            result.footer.shouldNotBeNull().primary.shouldNotBeNull().loading shouldBe true
        }

        @Test
        fun `primary is not loading when idle`() {
            // ----- Arrange -----
            val isAdding = false

            // ----- Act -----
            val result = unknownIsbnSheetScaffoldFor(isAdding = isAdding)

            // ----- Assert -----
            result.footer.shouldNotBeNull().primary.shouldNotBeNull().loading shouldBe false
        }

        @Test
        fun `primary label differs between adding and idle`() {
            // ----- Arrange -----
            val addingInput = true
            val idleInput = false

            // ----- Act -----
            val adding = unknownIsbnSheetScaffoldFor(isAdding = addingInput)
            val idle = unknownIsbnSheetScaffoldFor(isAdding = idleInput)

            // ----- Assert -----
            val addingLabel = adding.footer.shouldNotBeNull().primary.shouldNotBeNull().label
            val idleLabel = idle.footer.shouldNotBeNull().primary.shouldNotBeNull().label
            addingLabel shouldNotBe idleLabel
        }

        @Test
        fun `is not dismissible when adding`() {
            // ----- Arrange -----
            val isAdding = true

            // ----- Act -----
            val result = unknownIsbnSheetScaffoldFor(isAdding = isAdding)

            // ----- Assert -----
            result.dismissible shouldBe false
        }

        @Test
        fun `is dismissible when idle`() {
            // ----- Arrange -----
            val isAdding = false

            // ----- Act -----
            val result = unknownIsbnSheetScaffoldFor(isAdding = isAdding)

            // ----- Assert -----
            result.dismissible shouldBe true
        }

        @Test
        fun `secondary is disabled when adding`() {
            // ----- Arrange -----
            val isAdding = true

            // ----- Act -----
            val result = unknownIsbnSheetScaffoldFor(isAdding = isAdding)

            // ----- Assert -----
            result.footer.shouldNotBeNull().secondary.shouldNotBeNull().enabled shouldBe false
        }

        @Test
        fun `secondary is enabled when idle`() {
            // ----- Arrange -----
            val isAdding = false

            // ----- Act -----
            val result = unknownIsbnSheetScaffoldFor(isAdding = isAdding)

            // ----- Assert -----
            result.footer.shouldNotBeNull().secondary.shouldNotBeNull().enabled shouldBe true
        }
    }
}
