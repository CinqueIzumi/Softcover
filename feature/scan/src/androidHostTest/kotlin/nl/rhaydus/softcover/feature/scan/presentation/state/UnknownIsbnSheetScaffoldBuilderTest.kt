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
        fun `is busy only while adding`() {
            // ----- Arrange -----
            val addingInput = true
            val idleInput = false

            // ----- Act -----
            val adding = unknownIsbnSheetScaffoldFor(isAdding = addingInput)
            val idle = unknownIsbnSheetScaffoldFor(isAdding = idleInput)

            // ----- Assert -----
            adding.busy shouldBe true
            idle.busy shouldBe false
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
            // ----- Arrange -----
            val addingInput = true
            val idleInput = false

            // ----- Act -----
            val adding = unknownIsbnSheetScaffoldFor(isAdding = addingInput).footer.shouldNotBeNull()
            val idle = unknownIsbnSheetScaffoldFor(isAdding = idleInput).footer.shouldNotBeNull()

            // ----- Assert -----
            adding.secondary.shouldNotBeNull().dismissesSheet shouldBe true
            idle.secondary.shouldNotBeNull().dismissesSheet shouldBe true
            adding.primary.shouldNotBeNull().dismissesSheet shouldBe false
            idle.primary.shouldNotBeNull().dismissesSheet shouldBe false
        }
    }
}
