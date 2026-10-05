package nl.rhaydus.softcover.core.preferences.data.model

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.DesktopWindowState
import nl.rhaydus.softcover.core.domain.model.WindowPlacement
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DesktopWindowStateEntityTest {
    @Nested
    inner class ToModel {
        @Test
        fun `maps every field to the domain model`() {
            // ----- Arrange -----
            val entity = DesktopWindowStateEntity(
                width = 1440,
                height = 900,
                x = 100,
                y = 200,
                placement = WindowPlacement.MAXIMIZED,
            )

            // ----- Act -----
            val model = entity.toModel()

            // ----- Assert -----
            model shouldBe DesktopWindowState(
                width = 1440,
                height = 900,
                x = 100,
                y = 200,
                placement = WindowPlacement.MAXIMIZED,
            )
        }
    }

    @Nested
    inner class ToEntity {
        @Test
        fun `maps every field to the entity`() {
            // ----- Arrange -----
            val model = DesktopWindowState(
                width = 1920,
                height = 1080,
                x = 50,
                y = 80,
                placement = WindowPlacement.FULLSCREEN,
            )

            // ----- Act -----
            val entity = model.toEntity()

            // ----- Assert -----
            entity shouldBe DesktopWindowStateEntity(
                width = 1920,
                height = 1080,
                x = 50,
                y = 80,
                placement = WindowPlacement.FULLSCREEN,
            )
        }
    }

    @Nested
    inner class RoundTrip {
        @Test
        fun `preserves an unset window position`() {
            // ----- Arrange -----
            val original = DesktopWindowState(
                width = 1280,
                height = 800,
                x = null,
                y = null,
                placement = WindowPlacement.FLOATING,
            )

            // ----- Act -----
            val result = original.toEntity().toModel()

            // ----- Assert -----
            result shouldBe original
        }
    }
}
