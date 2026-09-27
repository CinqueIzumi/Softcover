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
        fun `maps all fields including non-null x and y to domain model`() {
            // ----- Arrange -----
            val entity = DesktopWindowStateEntity(
                width = 1440,
                height = 900,
                x = 100,
                y = 200,
                placement = WindowPlacement.FLOATING,
            )

            // ----- Act -----
            val model = entity.toModel()

            // ----- Assert -----
            model shouldBe DesktopWindowState(
                width = 1440,
                height = 900,
                x = 100,
                y = 200,
                placement = WindowPlacement.FLOATING,
            )
        }

        @Test
        fun `maps null x and y to domain model`() {
            // ----- Arrange -----
            val entity = DesktopWindowStateEntity(
                width = DesktopWindowState.DEFAULT_WIDTH,
                height = DesktopWindowState.DEFAULT_HEIGHT,
                x = null,
                y = null,
                placement = WindowPlacement.FLOATING,
            )

            // ----- Act -----
            val model = entity.toModel()

            // ----- Assert -----
            model.x shouldBe null
            model.y shouldBe null
            model.width shouldBe DesktopWindowState.DEFAULT_WIDTH
            model.height shouldBe DesktopWindowState.DEFAULT_HEIGHT
            model.placement shouldBe WindowPlacement.FLOATING
        }

        @Test
        fun `maps maximized placement correctly`() {
            // ----- Arrange -----
            val entity = DesktopWindowStateEntity(placement = WindowPlacement.MAXIMIZED)

            // ----- Act -----
            val model = entity.toModel()

            // ----- Assert -----
            model.placement shouldBe WindowPlacement.MAXIMIZED
        }

        @Test
        fun `maps fullscreen placement correctly`() {
            // ----- Arrange -----
            val entity = DesktopWindowStateEntity(placement = WindowPlacement.FULLSCREEN)

            // ----- Act -----
            val model = entity.toModel()

            // ----- Assert -----
            model.placement shouldBe WindowPlacement.FULLSCREEN
        }
    }

    @Nested
    inner class ToEntity {
        @Test
        fun `maps all fields including non-null x and y to entity`() {
            // ----- Arrange -----
            val model = DesktopWindowState(
                width = 1920,
                height = 1080,
                x = 50,
                y = 80,
                placement = WindowPlacement.FLOATING,
            )

            // ----- Act -----
            val entity = model.toEntity()

            // ----- Assert -----
            entity shouldBe DesktopWindowStateEntity(
                width = 1920,
                height = 1080,
                x = 50,
                y = 80,
                placement = WindowPlacement.FLOATING,
            )
        }

        @Test
        fun `maps null x and y to entity`() {
            // ----- Arrange -----
            val model = DesktopWindowState(
                width = DesktopWindowState.DEFAULT_WIDTH,
                height = DesktopWindowState.DEFAULT_HEIGHT,
                x = null,
                y = null,
                placement = WindowPlacement.FLOATING,
            )

            // ----- Act -----
            val entity = model.toEntity()

            // ----- Assert -----
            entity.x shouldBe null
            entity.y shouldBe null
            entity.width shouldBe DesktopWindowState.DEFAULT_WIDTH
            entity.height shouldBe DesktopWindowState.DEFAULT_HEIGHT
            entity.placement shouldBe WindowPlacement.FLOATING
        }
    }

    @Nested
    inner class RoundTrip {
        @Test
        fun `entity to model to entity round-trip preserves all fields with non-null x and y`() {
            // ----- Arrange -----
            val original = DesktopWindowStateEntity(
                width = 1440,
                height = 900,
                x = 300,
                y = 150,
                placement = WindowPlacement.FLOATING,
            )

            // ----- Act -----
            val result = original.toModel().toEntity()

            // ----- Assert -----
            result shouldBe original
        }

        @Test
        fun `entity to model to entity round-trip preserves null x and y`() {
            // ----- Arrange -----
            val original = DesktopWindowStateEntity(
                width = DesktopWindowState.DEFAULT_WIDTH,
                height = DesktopWindowState.DEFAULT_HEIGHT,
                x = null,
                y = null,
                placement = WindowPlacement.FLOATING,
            )

            // ----- Act -----
            val result = original.toModel().toEntity()

            // ----- Assert -----
            result shouldBe original
        }

        @Test
        fun `model to entity to model round-trip preserves all fields with non-null x and y`() {
            // ----- Arrange -----
            val original = DesktopWindowState(
                width = 2560,
                height = 1440,
                x = 10,
                y = 20,
                placement = WindowPlacement.MAXIMIZED,
            )

            // ----- Act -----
            val result = original.toEntity().toModel()

            // ----- Assert -----
            result shouldBe original
        }

        @Test
        fun `model to entity to model round-trip preserves null x and y`() {
            // ----- Arrange -----
            val original = DesktopWindowState(
                width = DesktopWindowState.DEFAULT_WIDTH,
                height = DesktopWindowState.DEFAULT_HEIGHT,
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
