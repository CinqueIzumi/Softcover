package nl.rhaydus.softcover.feature.session.presentation.mapper

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FocusPageEditorMapperTest {
    private fun stubEdition(pages: Int?): BookEdition = mockk {
        every {
            this@mockk.pages
        } returns pages
    }

    private fun stubBook(
        currentPages: Int? = null,
        defaultPages: Int? = null,
        hasCurrentEdition: Boolean = currentPages != null,
        hasDefaultEdition: Boolean = defaultPages != null,
        storedProgress: Float? = null,
    ): Book = mockk {
        every {
            currentEdition
        } returns if (hasCurrentEdition) stubEdition(currentPages) else null

        every {
            defaultEdition
        } returns if (hasDefaultEdition) stubEdition(defaultPages) else null

        every {
            userBookRead
        } returns storedProgress?.let { progress ->
            mockk<UserBookRead> {
                every {
                    this@mockk.progress
                } returns progress
            }
        }
    }

    @Nested
    inner class PageCount {
        @Test
        fun `prefers the current edition's pages over the default edition's`() {
            // ----- Arrange -----
            val book = stubBook(
                currentPages = 300,
                defaultPages = 200,
            )

            // ----- Act & Assert -----
            book.pageCount() shouldBe 300
        }

        @Test
        fun `falls back to the default edition when the current edition has no pages`() {
            // ----- Arrange -----
            val book = stubBook(
                hasCurrentEdition = true,
                currentPages = null,
                defaultPages = 200,
            )

            // ----- Act & Assert -----
            book.pageCount() shouldBe 200
        }

        @Test
        fun `falls back to the default edition when there is no current edition`() {
            // ----- Arrange -----
            val book = stubBook(defaultPages = 200)

            // ----- Act & Assert -----
            book.pageCount() shouldBe 200
        }

        @Test
        fun `is zero when neither edition has pages`() {
            // ----- Arrange -----
            val book = stubBook()

            // ----- Act & Assert -----
            book.pageCount() shouldBe 0
        }
    }

    @Nested
    inner class ToFocusPageEditor {
        @Test
        fun `is editable with an of-total caption and a bar from the draft`() {
            // ----- Arrange -----
            val book = stubBook(currentPages = 200)

            // ----- Act -----
            val editor = book.toFocusPageEditor(draft = "50")

            // ----- Assert -----
            editor.caption shouldBe "of 200 pages"
            editor.isEditable shouldBe true
            editor.bar.fraction shouldBe 0.25f
        }

        @Test
        fun `uses the default edition's total when the current edition has none`() {
            // ----- Arrange -----
            val book = stubBook(defaultPages = 100)

            // ----- Act -----
            val editor = book.toFocusPageEditor(draft = "10")

            // ----- Assert -----
            editor.caption shouldBe "of 100 pages"
            editor.bar.fraction shouldBe 0.1f
        }

        @Test
        fun `bar is empty for an unparseable draft`() {
            // ----- Arrange -----
            val book = stubBook(currentPages = 200)

            // ----- Act -----
            val editor = book.toFocusPageEditor(draft = "abc")

            // ----- Assert -----
            editor.bar.fraction shouldBe 0f
            editor.isEditable shouldBe true
        }

        @Test
        fun `bar is empty for an empty draft`() {
            // ----- Arrange -----
            val book = stubBook(currentPages = 200)

            // ----- Act -----
            val editor = book.toFocusPageEditor(draft = "")

            // ----- Assert -----
            editor.bar.fraction shouldBe 0f
        }

        @Test
        fun `bar passes an over-total draft through unclamped`() {
            // ----- Arrange -----
            val book = stubBook(currentPages = 200)

            // ----- Act -----
            val editor = book.toFocusPageEditor(draft = "500")

            // ----- Assert -----
            editor.bar.fraction shouldBe 2.5f
        }

        @Test
        fun `bar passes a negative draft through unclamped`() {
            // ----- Arrange -----
            val book = stubBook(currentPages = 200)

            // ----- Act -----
            val editor = book.toFocusPageEditor(draft = "-5")

            // ----- Assert -----
            editor.bar.fraction shouldBe -0.025f
        }

        @Test
        fun `without a page count the caption is the rounded stored percentage and the field is not editable`() {
            // ----- Arrange -----
            val book = stubBook(storedProgress = 42.6f)

            // ----- Act -----
            val editor = book.toFocusPageEditor(draft = "99")

            // ----- Assert -----
            editor.caption shouldBe "43% complete"
            editor.isEditable shouldBe false
            editor.bar.fraction shouldBe 0.426f
        }

        @Test
        fun `without a page count the bar ignores the draft`() {
            // ----- Arrange -----
            val book = stubBook(storedProgress = 10f)

            // ----- Act -----
            val editor = book.toFocusPageEditor(draft = "150")

            // ----- Assert -----
            editor.bar.fraction shouldBe 0.1f
        }

        @Test
        fun `without a page count or stored progress reads zero percent`() {
            // ----- Arrange -----
            val book = stubBook()

            // ----- Act -----
            val editor = book.toFocusPageEditor(draft = "")

            // ----- Assert -----
            editor.caption shouldBe "0% complete"
            editor.bar.fraction shouldBe 0f
        }

        @Test
        fun `without a page count the stored percentage passes through unclamped`() {
            // ----- Arrange -----
            val book = stubBook(storedProgress = 140f)

            // ----- Act -----
            val editor = book.toFocusPageEditor(draft = "")

            // ----- Assert -----
            editor.bar.fraction shouldBe 1.4f
        }
    }
}
