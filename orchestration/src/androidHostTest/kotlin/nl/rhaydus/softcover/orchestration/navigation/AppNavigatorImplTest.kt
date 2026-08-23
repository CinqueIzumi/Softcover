package nl.rhaydus.softcover.orchestration.navigation

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.presentation.navigation.ScreenDestination
import nl.rhaydus.softcover.core.presentation.navigation.TabDestination
import nl.rhaydus.softcover.feature.book_detail.presentation.screen.BookDetailScreen
import nl.rhaydus.softcover.feature.explore.presentation.screen.ExploreTab
import nl.rhaydus.softcover.feature.library.presentation.screen.LibraryTab
import nl.rhaydus.softcover.feature.profile.presentation.screen.ProfileScreen
import nl.rhaydus.softcover.feature.reading.presentation.screen.ReadingTab
import nl.rhaydus.softcover.feature.scan.presentation.screen.BarcodeScannerScreen
import nl.rhaydus.softcover.feature.session.presentation.screen.FocusModeScreen
import nl.rhaydus.softcover.feature.settings.presentation.screen.LibraryVisibilitySettingsScreen
import nl.rhaydus.softcover.feature.settings.presentation.screen.SettingsTab

class AppNavigatorImplTest {
    private val navigator = AppNavigatorImpl()

    @Nested
    inner class Screen {
        @Test
        fun `BookDetail destination maps to BookDetailScreen with matching id`() {
            // ----- Arrange -----
            val destination = ScreenDestination.BookDetail(id = 42)

            // ----- Act -----
            val result = navigator.screen(destination)

            // ----- Assert -----
            result.shouldBeInstanceOf<BookDetailScreen>()
            (result as BookDetailScreen).id shouldBe 42
        }

        @Test
        fun `BarcodeScanner destination maps to BarcodeScannerScreen`() {
            // ----- Arrange -----
            val destination = ScreenDestination.BarcodeScanner

            // ----- Act -----
            val result = navigator.screen(destination)

            // ----- Assert -----
            result.shouldBeInstanceOf<BarcodeScannerScreen>()
        }

        @Test
        fun `LibraryVisibilitySettings destination maps to LibraryVisibilitySettingsScreen`() {
            // ----- Arrange -----
            val destination = ScreenDestination.LibraryVisibilitySettings

            // ----- Act -----
            val result = navigator.screen(destination)

            // ----- Assert -----
            result.shouldBeInstanceOf<LibraryVisibilitySettingsScreen>()
        }

        @Test
        fun `FocusMode destination maps to FocusModeScreen singleton`() {
            // ----- Arrange -----
            val destination = ScreenDestination.FocusMode

            // ----- Act -----
            val result = navigator.screen(destination)

            // ----- Assert -----
            result shouldBe FocusModeScreen
        }

        @Test
        fun `Profile destination maps to ProfileScreen`() {
            // ----- Arrange -----
            val destination = ScreenDestination.Profile

            // ----- Act -----
            val result = navigator.screen(destination)

            // ----- Assert -----
            result.shouldBeInstanceOf<ProfileScreen>()
        }
    }

    @Nested
    inner class Tab {
        @Test
        fun `READING tab destination maps to ReadingTab singleton`() {
            // ----- Act -----
            val result = navigator.tab(TabDestination.READING)

            // ----- Assert -----
            result shouldBe ReadingTab
        }

        @Test
        fun `LIBRARY tab destination maps to LibraryTab singleton`() {
            // ----- Act -----
            val result = navigator.tab(TabDestination.LIBRARY)

            // ----- Assert -----
            result shouldBe LibraryTab
        }

        @Test
        fun `EXPLORE tab destination maps to ExploreTab singleton`() {
            // ----- Act -----
            val result = navigator.tab(TabDestination.EXPLORE)

            // ----- Assert -----
            result shouldBe ExploreTab
        }

        @Test
        fun `SETTINGS tab destination maps to SettingsTab singleton`() {
            // ----- Act -----
            val result = navigator.tab(TabDestination.SETTINGS)

            // ----- Assert -----
            result shouldBe SettingsTab
        }
    }
}
