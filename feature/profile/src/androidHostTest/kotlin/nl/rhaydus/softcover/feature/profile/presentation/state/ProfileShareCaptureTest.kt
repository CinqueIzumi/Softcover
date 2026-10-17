package nl.rhaydus.softcover.feature.profile.presentation.state

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.share.ShareCaptureProgress
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProfileShareCaptureTest {
    @Nested
    inner class ProfileShareSheetFor {
        @Test
        fun `names the download after the reader`() {
            // ----- Arrange -----
            val card = buildShareCard(name = "Jane Doe")

            // ----- Act -----
            val sheet = profileShareSheetFor(
                card = card,
                progress = ShareCaptureProgress.Idle,
            )

            // ----- Assert -----
            sheet.displayName shouldBe "Jane Doe-reading-life"
            sheet.card shouldBe card
        }

        @Test
        fun `carries the progress into the scaffold footer`() {
            // ----- Arrange -----
            val card = buildShareCard()

            // ----- Act -----
            val sheet = profileShareSheetFor(
                card = card,
                progress = ShareCaptureProgress.Saving,
            )

            // ----- Assert -----
            sheet.scaffold.busy shouldBe true
            sheet.scaffold.footer.shouldNotBeNull().secondary.shouldNotBeNull().running shouldBe true
        }
    }
}
