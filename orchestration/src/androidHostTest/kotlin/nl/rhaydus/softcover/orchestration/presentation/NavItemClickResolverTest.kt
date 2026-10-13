package nl.rhaydus.softcover.orchestration.presentation

import cafe.adriel.voyager.navigator.tab.Tab
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.presentation.util.LibraryNavPulseKey
import nl.rhaydus.softcover.feature.explore.presentation.screen.ExploreTab
import nl.rhaydus.softcover.feature.library.presentation.screen.LibraryTab
import nl.rhaydus.softcover.feature.reading.presentation.screen.ReadingTab
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class NavItemClickResolverTest {
    private val selectedTabs = mutableListOf<Tab>()
    private val reselectedKeys = mutableListOf<Any>()
    private val pulsedKeys = mutableListOf<Any>()

    private val launchedBlocks = mutableListOf<suspend () -> Unit>()

    private fun recordLaunch(block: suspend () -> Unit) {
        launchedBlocks += block
    }

    private fun resolver(reselectResult: Boolean) = NavItemClickResolver(
        selectTab = { selectedTabs += it },
        reselect = {
            reselectedKeys += it
            reselectResult
        },
        pulse = { pulsedKeys += it },
    )

    private fun item(
        tab: Tab,
        selected: Boolean,
    ) = NavItemUi(
        tab = tab,
        selected = selected,
        showBadge = false,
        iconScale = 1f,
    )

    @Nested
    inner class OnClick {
        @Test
        fun `unselected item selects its tab without reselect or pulse`() = runTest {
            // ----- Arrange -----
            val resolver = resolver(reselectResult = true)
            val item = item(
                ExploreTab,
                selected = false,
            )

            // ----- Act -----
            resolver.onClick(
                item = item,
                launchReselect = ::recordLaunch,
            )
            launchedBlocks.forEach { it() }

            // ----- Assert -----
            selectedTabs shouldBe listOf(ExploreTab)
            reselectedKeys shouldBe emptyList()
            pulsedKeys shouldBe emptyList()
        }

        @Test
        fun `selected item whose reselect is consumed neither pulses nor selects`() = runTest {
            // ----- Arrange -----
            val resolver = resolver(reselectResult = true)
            val item = item(
                ReadingTab,
                selected = true,
            )

            // ----- Act -----
            resolver.onClick(
                item = item,
                launchReselect = ::recordLaunch,
            )
            launchedBlocks.forEach { it() }

            // ----- Assert -----
            reselectedKeys shouldBe listOf(ReadingTab)
            pulsedKeys shouldBe emptyList()
            selectedTabs shouldBe emptyList()
        }

        @Test
        fun `selected item whose reselect is not consumed pulses its key`() = runTest {
            // ----- Arrange -----
            val resolver = resolver(reselectResult = false)
            val item = item(
                ExploreTab,
                selected = true,
            )

            // ----- Act -----
            resolver.onClick(
                item = item,
                launchReselect = ::recordLaunch,
            )
            launchedBlocks.forEach { it() }

            // ----- Assert -----
            pulsedKeys shouldBe listOf(ExploreTab)
            selectedTabs shouldBe emptyList()
        }

        @Test
        fun `selected library item pulses the shared library key`() = runTest {
            // ----- Arrange -----
            val resolver = resolver(reselectResult = false)
            val item = item(
                LibraryTab,
                selected = true,
            )

            // ----- Act -----
            resolver.onClick(
                item = item,
                launchReselect = ::recordLaunch,
            )
            launchedBlocks.forEach { it() }

            // ----- Assert -----
            pulsedKeys shouldBe listOf(LibraryNavPulseKey)
        }
    }

    @Nested
    inner class Launch {
        @Test
        fun `unselected never calls launch`() {
            // ----- Arrange -----
            val resolver = resolver(reselectResult = false)
            val item = item(
                ExploreTab,
                selected = false,
            )

            // ----- Act -----
            resolver.onClick(
                item = item,
                launchReselect = ::recordLaunch,
            )

            // ----- Assert -----
            launchedBlocks shouldBe emptyList()
        }

        @Test
        fun `selected defers reselect until the launched block runs`() = runTest {
            // ----- Arrange -----
            val resolver = resolver(reselectResult = false)
            val item = item(
                ReadingTab,
                selected = true,
            )

            // ----- Act -----
            resolver.onClick(
                item = item,
                launchReselect = ::recordLaunch,
            )
            val reselectedBeforeRun = reselectedKeys.toList()
            launchedBlocks.single().invoke()

            // ----- Assert -----
            launchedBlocks.size shouldBe 1
            reselectedBeforeRun shouldBe emptyList()
            reselectedKeys shouldBe listOf(ReadingTab)
        }
    }

    @Nested
    inner class NavPulseKeyFor {
        @Test
        fun `library maps to the shared library pulse key`() {
            // ----- Arrange -----
            val tab: Tab = LibraryTab

            // ----- Act -----
            val result = navPulseKeyFor(tab)

            // ----- Assert -----
            result shouldBe LibraryNavPulseKey
        }

        @Test
        fun `any other tab maps to itself`() {
            // ----- Arrange -----
            val tab: Tab = ReadingTab

            // ----- Act -----
            val result = navPulseKeyFor(tab)

            // ----- Assert -----
            result shouldBe ReadingTab
        }
    }
}
