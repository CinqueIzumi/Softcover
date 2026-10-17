package nl.rhaydus.softcover.feature.settings.presentation.screen

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.feature.settings.presentation.model.DocumentPage
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class DocumentPageScreenSerializationTest {
    @Nested
    inner class BackStackSave {
        @Test
        fun `survives a back stack save keeping its page`() {
            // ----- Arrange -----
            val screen = DocumentPageScreen(page = DocumentPage.WHATS_NEW)

            // ----- Act -----
            val bytes = ByteArrayOutputStream().also { sink ->
                ObjectOutputStream(sink).use {
                    it.writeObject(screen)
                }
            }.toByteArray()
            val restored = ObjectInputStream(ByteArrayInputStream(bytes)).use {
                it.readObject()
            }

            // ----- Assert -----
            val page = restored.javaClass.getDeclaredField("page").apply { isAccessible = true }.get(restored)
            page shouldBe DocumentPage.WHATS_NEW
        }
    }
}
