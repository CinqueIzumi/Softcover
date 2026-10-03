package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import nl.rhaydus.softcover.core.component.callout.CalloutActionUiModel
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ScanEditionCalloutMapperTest {
    @Nested
    inner class ScanEditionCallout {
        @Test
        fun `returns a dismissible callout with a Button action for a different shelved edition`() {
            // ----- Arrange -----
            val scannedEditionId = 42
            val shelvedEditionId = 10

            // ----- Act -----
            val result = scanEditionCallout(
                scannedEditionId = scannedEditionId,
                shelvedEditionId = shelvedEditionId,
                dismissed = false,
                isUpdating = false,
            )

            // ----- Assert -----
            val callout = result.shouldNotBeNull()
            callout.dismissible shouldBe true
            callout.action.shouldBeInstanceOf<CalloutActionUiModel.Button>()
        }

        @Test
        fun `returns a Busy action while the scanned edition is being applied`() {
            // ----- Arrange -----
            val scannedEditionId = 42
            val shelvedEditionId = 10

            // ----- Act -----
            val result = scanEditionCallout(
                scannedEditionId = scannedEditionId,
                shelvedEditionId = shelvedEditionId,
                dismissed = false,
                isUpdating = true,
            )

            // ----- Assert -----
            result.shouldNotBeNull().action.shouldBeInstanceOf<CalloutActionUiModel.Busy>()
        }

        @Test
        fun `returns null when the callout is dismissed`() {
            // ----- Arrange -----
            val scannedEditionId = 42
            val shelvedEditionId = 10

            // ----- Act -----
            val result = scanEditionCallout(
                scannedEditionId = scannedEditionId,
                shelvedEditionId = shelvedEditionId,
                dismissed = true,
                isUpdating = false,
            )

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `returns null when the book has no user book edition`() {
            // ----- Arrange -----
            val scannedEditionId = 42

            // ----- Act -----
            val result = scanEditionCallout(
                scannedEditionId = scannedEditionId,
                shelvedEditionId = null,
                dismissed = false,
                isUpdating = false,
            )

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `returns null when the shelved edition matches the scanned edition`() {
            // ----- Arrange -----
            val editionId = 42

            // ----- Act -----
            val result = scanEditionCallout(
                scannedEditionId = editionId,
                shelvedEditionId = editionId,
                dismissed = false,
                isUpdating = false,
            )

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `returns null when nothing was scanned`() {
            // ----- Arrange -----
            val shelvedEditionId = 10

            // ----- Act -----
            val result = scanEditionCallout(
                scannedEditionId = null,
                shelvedEditionId = shelvedEditionId,
                dismissed = false,
                isUpdating = false,
            )

            // ----- Assert -----
            result.shouldBeNull()
        }
    }
}
