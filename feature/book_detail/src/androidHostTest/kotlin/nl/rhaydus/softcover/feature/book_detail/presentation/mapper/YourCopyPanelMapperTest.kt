package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import nl.rhaydus.softcover.core.component.yourcopy.YourCopyValue
import nl.rhaydus.softcover.core.domain.model.AcquisitionKind
import nl.rhaydus.softcover.core.domain.model.AcquisitionSource
import nl.rhaydus.softcover.core.domain.model.BookCopy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class YourCopyPanelMapperTest {
    @Nested
    inner class ToYourCopyPanel {
        @Test
        fun `shows an empty row for a copy without a source`() {
            // ----- Arrange -----
            val copy = BookCopy(
                bookId = 1,
                source = null,
            )

            // ----- Act -----
            val result = copy.toYourCopyPanel()

            // ----- Assert -----
            result.rows.single().key shouldBe WHERE_YOU_GOT_IT_ROW_KEY
            result.rows.single().value.shouldBeInstanceOf<YourCopyValue.Empty>()
        }

        @Test
        fun `shows the preset's label for a preset source`() {
            // ----- Arrange -----
            val copy = BookCopy(
                bookId = 1,
                source = AcquisitionSource.Preset(kind = AcquisitionKind.BOUGHT_SECOND_HAND),
            )

            // ----- Act -----
            val result = copy.toYourCopyPanel()

            // ----- Assert -----
            result.rows.single().value shouldBe YourCopyValue.Set(text = AcquisitionKind.BOUGHT_SECOND_HAND.toLabel())
        }

        @Test
        fun `shows the custom text for another source`() {
            // ----- Arrange -----
            val copy = BookCopy(
                bookId = 1,
                source = AcquisitionSource.Other(text = "A street stall"),
            )

            // ----- Act -----
            val result = copy.toYourCopyPanel()

            // ----- Assert -----
            result.rows.single().value shouldBe YourCopyValue.Set(text = "A street stall")
        }
    }
}
