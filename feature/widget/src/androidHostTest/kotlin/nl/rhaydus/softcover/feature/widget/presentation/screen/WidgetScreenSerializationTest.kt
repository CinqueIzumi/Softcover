package nl.rhaydus.softcover.feature.widget.presentation.screen

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class WidgetScreenSerializationTest {
    private fun Any.roundTrip(): Any {
        val bytes = ByteArrayOutputStream().also { sink ->
            ObjectOutputStream(sink).use {
                it.writeObject(this)
            }
        }.toByteArray()

        return ObjectInputStream(ByteArrayInputStream(bytes)).use {
            it.readObject()
        }
    }

    private fun Any.field(name: String): Any? = javaClass.getDeclaredField(name).apply { isAccessible = true }.get(this)

    @Nested
    inner class UpdateProgress {
        @Test
        fun `survives a back stack save keeping its book id`() {
            // ----- Arrange -----
            val screen = UpdateProgressScreen(bookId = 77)

            // ----- Act -----
            val restored = screen.roundTrip()

            // ----- Assert -----
            restored.field("bookId") shouldBe 77
        }
    }

    @Nested
    inner class TrendingSettings {
        @Test
        fun `survives a back stack save keeping its widget id`() {
            // ----- Arrange -----
            val screen = TrendingSettingsScreen(appWidgetId = 12)

            // ----- Act -----
            val restored = screen.roundTrip()

            // ----- Assert -----
            restored.field("appWidgetId") shouldBe 12
        }
    }

    @Nested
    inner class RandomPickSettings {
        @Test
        fun `survives a back stack save keeping its widget id`() {
            // ----- Arrange -----
            val screen = RandomPickSettingsScreen(appWidgetId = 34)

            // ----- Act -----
            val restored = screen.roundTrip()

            // ----- Assert -----
            restored.field("appWidgetId") shouldBe 34
        }
    }
}
