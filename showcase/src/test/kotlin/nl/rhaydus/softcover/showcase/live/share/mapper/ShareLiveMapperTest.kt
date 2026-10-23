package nl.rhaydus.softcover.showcase.live.share.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.share.ShareCardSheetEvent
import nl.rhaydus.softcover.core.component.share.ShareCardSheetUiModel
import nl.rhaydus.softcover.showcase.live.share.model.ShareDemo
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ShareLiveMapperTest {
    private fun running(model: ShareCardSheetUiModel): Pair<Boolean?, Boolean?> = Pair(
        model.scaffold.footer?.primary?.running,
        model.scaffold.footer?.secondary?.running,
    )

    @Nested
    inner class Reducing {
        @Test
        fun `runs the share button when sharing starts`() {
            // ----- Arrange -----
            val model = shareDemoModel(demo = ShareDemo.BOOK)

            // ----- Act -----
            val result = model.reducing(event = ShareCardSheetEvent.ShareStarted)

            // ----- Assert -----
            running(result) shouldBe Pair(
                true,
                false,
            )
        }

        @Test
        fun `runs the save button when saving starts`() {
            // ----- Arrange -----
            val model = shareDemoModel(demo = ShareDemo.BOOK)

            // ----- Act -----
            val result = model.reducing(event = ShareCardSheetEvent.SaveStarted)

            // ----- Assert -----
            running(result) shouldBe Pair(
                false,
                true,
            )
        }

        @Test
        fun `stops running once the capture finishes`() {
            // ----- Arrange -----
            val model = shareDemoModel(demo = ShareDemo.BOOK).reducing(event = ShareCardSheetEvent.ShareStarted)

            // ----- Act -----
            val result = model.reducing(event = ShareCardSheetEvent.CaptureFinished)

            // ----- Assert -----
            running(result) shouldBe Pair(
                false,
                false,
            )
        }

        @Test
        fun `stops running once the card is shared`() {
            // ----- Arrange -----
            val model = shareDemoModel(demo = ShareDemo.BOOK).reducing(event = ShareCardSheetEvent.ShareStarted)

            // ----- Act -----
            val result = model.reducing(event = ShareCardSheetEvent.Shared)

            // ----- Assert -----
            running(result) shouldBe Pair(
                false,
                false,
            )
        }

        @Test
        fun `stops running when the sheet is dismissed mid-save`() {
            // ----- Arrange -----
            val model = shareDemoModel(demo = ShareDemo.QUOTE).reducing(event = ShareCardSheetEvent.SaveStarted)

            // ----- Act -----
            val result = model.reducing(event = ShareCardSheetEvent.Dismissed)

            // ----- Assert -----
            running(result) shouldBe Pair(
                false,
                false,
            )
        }

        @Test
        fun `leaves the card and file name as they were`() {
            // ----- Arrange -----
            val model = shareDemoModel(demo = ShareDemo.READING_UPDATE)

            // ----- Act -----
            val result = model.reducing(event = ShareCardSheetEvent.ShareStarted)

            // ----- Assert -----
            result.card shouldBe model.card
            result.displayName shouldBe model.displayName
        }
    }
}
