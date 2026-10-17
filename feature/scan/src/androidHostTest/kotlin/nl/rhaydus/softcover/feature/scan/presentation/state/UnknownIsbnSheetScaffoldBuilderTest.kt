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
            val adding = unknownIsbnSheetScaffoldFor(
                isAdding = addingInput,
                isImportPending = false,
            )
            val idle = unknownIsbnSheetScaffoldFor(
                isAdding = idleInput,
                isImportPending = false,
            )

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
            val adding = unknownIsbnSheetScaffoldFor(
                isAdding = addingInput,
                isImportPending = false,
            )
            val idle = unknownIsbnSheetScaffoldFor(
                isAdding = idleInput,
                isImportPending = false,
            )

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
            val adding = unknownIsbnSheetScaffoldFor(
                isAdding = addingInput,
                isImportPending = false,
            ).footer.shouldNotBeNull()
            val idle = unknownIsbnSheetScaffoldFor(
                isAdding = idleInput,
                isImportPending = false,
            ).footer.shouldNotBeNull()

            // ----- Assert -----
            adding.secondary.shouldNotBeNull().dismissesSheet shouldBe true
            idle.secondary.shouldNotBeNull().dismissesSheet shouldBe true
            adding.primary.shouldNotBeNull().dismissesSheet shouldBe false
            idle.primary.shouldNotBeNull().dismissesSheet shouldBe false
        }
    

        @Test
        fun `adding wins over a pending import`() {
            // ----- Arrange -----
            val addingInput = true
            val pendingInput = true

            // ----- Act -----
            val scaffold = unknownIsbnSheetScaffoldFor(
                isAdding = addingInput,
                isImportPending = pendingInput,
            )

            // ----- Assert -----
            scaffold.busy shouldBe true
            scaffold.footer.shouldNotBeNull().primary.shouldNotBeNull().running shouldBe true
        }

        @Test
        fun `a pending import alone leaves the primary button idle and enabled`() {
            // ----- Arrange -----
            val addingInput = false
            val pendingInput = true

            // ----- Act -----
            val primary = unknownIsbnSheetScaffoldFor(
                isAdding = addingInput,
                isImportPending = pendingInput,
            ).footer.shouldNotBeNull().primary.shouldNotBeNull()

            // ----- Assert -----
            primary.running shouldBe false
            primary.enabled shouldBe true
            primary.dismissesSheet shouldBe false
        }
}
}
