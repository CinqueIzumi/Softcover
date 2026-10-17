package nl.rhaydus.softcover.feature.scan.presentation.screen

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class BarcodeScannerScreenSerializationTest {
    private fun Any.field(name: String): Any? = javaClass.getDeclaredField(name).apply { isAccessible = true }.get(this)

    @Nested
    inner class BackStackSave {
        @Test
        fun `survives a back stack save keeping its entry args and dropping the callback`() {
            // ----- Arrange -----
            val screen = BarcodeScannerScreen(
                startInManualEntry = true,
                prefilledIsbn = "9780593135204",
                onResolved = { _, _ -> },
            )

            // ----- Act -----
            val bytes = ByteArrayOutputStream().also { sink ->
                ObjectOutputStream(sink).use {
                    it.writeObject(screen)
                }
            }.toByteArray()
            val restored = ObjectInputStream(ByteArrayInputStream(bytes)).use {
                it.readObject()
            } as BarcodeScannerScreen

            // ----- Assert -----
            restored.key shouldBe screen.key
            restored.field("startInManualEntry") shouldBe true
            restored.field("prefilledIsbn") shouldBe "9780593135204"
            restored.field("onResolved").shouldBeNull()
        }
    }
}
