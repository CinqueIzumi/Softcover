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
            result.busy shouldBe true
        }

        @Test
        fun `primary is not loading when idle`() {
            // ----- Arrange -----
            val isAdding = false

            // ----- Act -----
            val result = unknownIsbnSheetScaffoldFor(isAdding = isAdding)

            // ----- Assert -----
            result.busy shouldBe false
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
        fun `Cancel dismisses the sheet and Add does not, in both states`() {
            // ----- Act -----
            val adding = unknownIsbnSheetScaffoldFor(isAdding = true).footer.shouldNotBeNull()
            val idle = unknownIsbnSheetScaffoldFor(isAdding = false).footer.shouldNotBeNull()

            // ----- Assert -----
            adding.secondary.shouldNotBeNull().dismissesSheet shouldBe true
            idle.secondary.shouldNotBeNull().dismissesSheet shouldBe true
            adding.primary.shouldNotBeNull().dismissesSheet shouldBe false
            idle.primary.shouldNotBeNull().dismissesSheet shouldBe false
        }

        @Test
        fun `no close is requested by the builder`() {
            // ----- Act -----
            val result = unknownIsbnSheetScaffoldFor(isAdding = false)

            // ----- Assert -----
            result.closeRequested shouldBe false
        }

        @Test
        fun `buttons are left interactive while adding because busy covers them`() {
            // ----- Act -----
            val footer = unknownIsbnSheetScaffoldFor(isAdding = true).footer.shouldNotBeNull()

            // ----- Assert -----
            footer.primary.shouldNotBeNull().enabled shouldBe true
            footer.secondary.shouldNotBeNull().enabled shouldBe true
        }
    }
}
