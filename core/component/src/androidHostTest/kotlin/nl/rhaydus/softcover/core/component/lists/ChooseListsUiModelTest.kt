package nl.rhaydus.softcover.core.component.lists

import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ChooseListsUiModelTest {
    private val model = ChooseListsUiModel(
        rows = persistentListOf(),
        scaffold = SheetScaffoldUiModel(header = null),
    )

    @Nested
    inner class RequestingClose {
        @Test
        fun `delegates the close flag to the scaffold and keeps the rows`() {
            // ----- Arrange -----
            model.closeRequested shouldBe false

            // ----- Act -----
            val result = model.requestingClose()

            // ----- Assert -----
            result.closeRequested shouldBe true
            result.scaffold.closeRequested shouldBe true
            result.rows shouldBe model.rows
        }
    }
}
