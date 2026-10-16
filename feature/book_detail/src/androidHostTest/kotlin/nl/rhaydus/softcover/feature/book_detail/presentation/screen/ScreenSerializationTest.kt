package nl.rhaydus.softcover.feature.book_detail.presentation.screen

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.core.presentation.model.BookInitialCover
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class ScreenSerializationTest {
    private fun <T : Any> T.roundTrip(): T {
        val bytes = ByteArrayOutputStream().also { sink ->
            ObjectOutputStream(sink).use {
                it.writeObject(this)
            }
        }.toByteArray()

        @Suppress("UNCHECKED_CAST")
        return ObjectInputStream(ByteArrayInputStream(bytes)).use {
            it.readObject()
        } as T
    }

    private fun Any.field(name: String): Any? = javaClass.getDeclaredField(name).apply { isAccessible = true }.get(this)

    @Nested
    inner class FullScreenCover {
        @Test
        fun `survives a back stack save with the cover dropped`() {
            // ----- Arrange -----
            val cover = CoverUiModel.previews.getValue("With cover art")
            val screen = FullScreenCoverScreen(cover = cover)

            // ----- Act -----
            val restored = screen.roundTrip()

            // ----- Assert -----
            restored.field("cover").shouldBeNull()
        }
    }

    @Nested
    inner class BookDetail {
        @Test
        fun `survives a back stack save keeping its ids and dropping the transient args`() {
            // ----- Arrange -----
            val screen = BookDetailScreen(
                id = 42,
                initialCover = BookInitialCover.fromBook(book = PreviewData.baseBook),
                transitionSurface = "library",
                showVerdictSheet = true,
            )

            // ----- Act -----
            val restored = screen.roundTrip()

            // ----- Assert -----
            restored.id shouldBe 42
            restored.key shouldBe screen.key
            restored.field("transitionSurface") shouldBe "library"
            restored.field("initialCover").shouldBeNull()
            restored.field("showVerdictSheet") shouldBe false
        }
    }
}
