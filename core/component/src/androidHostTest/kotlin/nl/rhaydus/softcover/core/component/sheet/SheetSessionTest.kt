package nl.rhaydus.softcover.core.component.sheet

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SheetSessionTest {
    private val sheet = SheetScaffoldUiModel(
        header = null,
        dismissLabel = "sheet",
    )

    private fun openSession(): SheetSession<SheetScaffoldUiModel, Int> = SheetSession.Open(model = sheet)

    private fun closedSession(): SheetSession<SheetScaffoldUiModel, Int> = SheetSession.Closed

    @Nested
    inner class Open {
        @Test
        fun `opens a closed session on the model`() {
            // ----- Arrange -----
            val session = closedSession()

            // ----- Act -----
            val result = session.open(model = sheet)

            // ----- Assert -----
            result shouldBe SheetSession.Open<SheetScaffoldUiModel, Int>(model = sheet)
        }

        @Test
        fun `replaces a session that is still closing with a fresh one`() {
            // ----- Arrange -----
            val closing = openSession().requestClose(pending = 7)
            val fresh = sheet.copy(dismissLabel = "new")

            // ----- Act -----
            val result = closing.open(model = fresh)

            // ----- Assert -----
            result shouldBe SheetSession.Open<SheetScaffoldUiModel, Int>(model = fresh)
            result.isClosing shouldBe false
            result.dismissed().shouldBeNull()
        }
    }

    @Nested
    inner class OpenOrUpdate {
        @Test
        fun `opens when the session is closed`() {
            // ----- Arrange -----
            val session = closedSession()

            // ----- Act -----
            val result = session.openOrUpdate(model = sheet)

            // ----- Assert -----
            result shouldBe SheetSession.Open<SheetScaffoldUiModel, Int>(model = sheet)
        }

        @Test
        fun `opens a fresh sheet when the session is closing`() {
            // ----- Arrange -----
            val closing = openSession().requestClose(pending = 4)
            val fresh = sheet.copy(dismissLabel = "new")

            // ----- Act -----
            val result = closing.openOrUpdate(model = fresh)

            // ----- Assert -----
            result shouldBe SheetSession.Open<SheetScaffoldUiModel, Int>(model = fresh)
            result.isClosing shouldBe false
            result.dismissed().shouldBeNull()
        }

        @Test
        fun `updates the model and keeps the pending intent when the session is open`() {
            // ----- Arrange -----
            val session: SheetSession<SheetScaffoldUiModel, Int> = SheetSession.Open(
                model = sheet,
                pending = 6,
            )
            val updated = sheet.copy(dismissLabel = "updated")

            // ----- Act -----
            val result = session.openOrUpdate(model = updated)

            // ----- Assert -----
            result shouldBe SheetSession.Open(
                model = updated,
                pending = 6,
            )
        }
    }

    @Nested
    inner class RequestClose {
        @Test
        fun `writes the close flag on the model and records the pending intent`() {
            // ----- Arrange -----
            val session = openSession()

            // ----- Act -----
            val result = session.requestClose(pending = 3)

            // ----- Assert -----
            result shouldBe SheetSession.Open(
                model = sheet.copy(closeRequested = true),
                pending = 3,
            )
        }

        @Test
        fun `records no pending intent when none is given`() {
            // ----- Arrange -----
            val session = openSession()

            // ----- Act -----
            val result = session.requestClose()

            // ----- Assert -----
            result.isClosing shouldBe true
            result.dismissed().shouldBeNull()
        }

        @Test
        fun `requestClose with a value replaces an existing pending`() {
            // ----- Arrange -----
            val session = openSession().requestClose(pending = 1)

            // ----- Act -----
            val result = session.requestClose(pending = 2)

            // ----- Assert -----
            result.dismissed() shouldBe 2
        }

        @Test
        fun `requestClose with null keeps an existing pending`() {
            // ----- Arrange -----
            val session = openSession().requestClose(pending = 1)

            // ----- Act -----
            val result = session.requestClose()

            // ----- Assert -----
            result.dismissed() shouldBe 1
            result.isClosing shouldBe true
        }

        @Test
        fun `does nothing on a closed session`() {
            // ----- Arrange -----
            val session = closedSession()

            // ----- Act -----
            val result = session.requestClose(pending = 3)

            // ----- Assert -----
            result shouldBe SheetSession.Closed
        }
    }

    @Nested
    inner class WithModel {
        @Test
        fun `swaps the model and keeps the pending intent`() {
            // ----- Arrange -----
            val session: SheetSession<SheetScaffoldUiModel, Int> = SheetSession.Open(
                model = sheet,
                pending = 5,
            )

            // ----- Act -----
            val result = session.withModel { it.copy(dismissLabel = "updated") }

            // ----- Assert -----
            result shouldBe SheetSession.Open(
                model = sheet.copy(dismissLabel = "updated"),
                pending = 5,
            )
        }

        @Test
        fun `keeps a requested close across a rebuilt model whose flag starts false`() {
            // ----- Arrange -----
            val session = openSession().requestClose(pending = 5)

            // ----- Act -----
            val result = session.withModel {
                SheetScaffoldUiModel(
                    header = null,
                    dismissLabel = "rebuilt",
                )
            }

            // ----- Assert -----
            result.isClosing shouldBe true
            result.modelOrNull.shouldNotBeNull().dismissLabel shouldBe "rebuilt"
            result.dismissed() shouldBe 5
        }

        @Test
        fun `does not mark an open session as closing`() {
            // ----- Arrange -----
            val session = openSession()

            // ----- Act -----
            val result = session.withModel { it.copy(dismissLabel = "updated") }

            // ----- Assert -----
            result.isClosing shouldBe false
        }

        @Test
        fun `does not open a closed session`() {
            // ----- Arrange -----
            val session = closedSession()

            // ----- Act -----
            val result = session.withModel { it.copy(dismissLabel = "other") }

            // ----- Assert -----
            result shouldBe SheetSession.Closed
        }
    }

    @Nested
    inner class Dismissed {
        @Test
        fun `hands back the pending intent recorded by requestClose`() {
            // ----- Arrange -----
            val session = openSession().requestClose(pending = 9)

            // ----- Act -----
            val result = session.dismissed()

            // ----- Assert -----
            result shouldBe 9
        }

        @Test
        fun `is null when no intent was recorded`() {
            // ----- Arrange -----
            val session = openSession().requestClose()

            // ----- Act -----
            val result = session.dismissed()

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `is null when the session is closed`() {
            // ----- Arrange -----
            val session = closedSession()

            // ----- Act -----
            val result = session.dismissed()

            // ----- Assert -----
            result.shouldBeNull()
        }
    }

    @Nested
    inner class Accessors {
        @Test
        fun `an open session reports open, its model, and its closing state`() {
            // ----- Arrange -----
            val open = openSession()

            // ----- Act -----
            val closing = open.requestClose()

            // ----- Assert -----
            open.isOpen shouldBe true
            open.isClosing shouldBe false
            open.modelOrNull shouldBe sheet
            closing.isOpen shouldBe true
            closing.isClosing shouldBe true
        }

        @Test
        fun `a model that already asks to close makes the open session closing`() {
            // ----- Arrange -----
            val session: SheetSession<SheetScaffoldUiModel, Int> = SheetSession.Open(
                model = sheet.copy(closeRequested = true),
            )

            // ----- Act -----
            val result = session.isClosing

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `a closed session is neither open nor closing and has no model`() {
            // ----- Arrange -----
            val session = closedSession()

            // ----- Act -----
            val result = session.modelOrNull

            // ----- Assert -----
            session.isOpen shouldBe false
            session.isClosing shouldBe false
            result.shouldBeNull()
        }
    }
}
