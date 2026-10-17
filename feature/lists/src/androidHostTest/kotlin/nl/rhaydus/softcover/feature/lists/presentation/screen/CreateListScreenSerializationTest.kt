package nl.rhaydus.softcover.feature.lists.presentation.screen

import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class CreateListScreenSerializationTest {
    @Nested
    inner class BackStackSave {
        @Test
        fun `survives a back stack save`() {
            // ----- Arrange -----
            val screen = CreateListScreen()

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
            restored.shouldNotBeNull()
        }
    }
}
