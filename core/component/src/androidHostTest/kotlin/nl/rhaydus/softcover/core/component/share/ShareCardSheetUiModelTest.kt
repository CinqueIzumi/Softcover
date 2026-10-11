package nl.rhaydus.softcover.core.component.share

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ShareCardSheetUiModelTest {
    private val model = ShareCardSheetUiModel(
        card = mockk(),
        scaffold = SheetScaffoldUiModel(header = null),
        displayName = "piranesi",
    )

    @Nested
    inner class RequestingClose {
        @Test
        fun `delegates the close flag to the scaffold and keeps the rest of the model`() {
            // ----- Arrange -----
            model.closeRequested shouldBe false

            // ----- Act -----
            val result = model.requestingClose()

            // ----- Assert -----
            result.closeRequested shouldBe true
            result.scaffold.closeRequested shouldBe true
            result.displayName shouldBe "piranesi"
            result.card shouldBe model.card
        }
    }
}
