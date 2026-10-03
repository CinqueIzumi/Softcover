package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import org.junit.jupiter.api.Test

class SortModeResolutionTest {
    @Test
    fun `an explicit mode wins over the tab default`() {
        // ----- Arrange -----
        val tabId = LibraryTab.Status.of(UserBookStatus.READ).id

        // ----- Act -----
        val result = resolveSortMode(
            sortModeByTab = mapOf(tabId to LibrarySortMode.TITLE),
            tabId = tabId,
        )

        // ----- Assert -----
        result shouldBe LibrarySortMode.TITLE
    }

    @Test
    fun `falls back to the tab default when no mode is chosen`() {
        // ----- Arrange -----
        val tabId = LibraryTab.Status.of(UserBookStatus.READ).id

        // ----- Act -----
        val result = resolveSortMode(
            sortModeByTab = emptyMap(),
            tabId = tabId,
        )

        // ----- Assert -----
        result shouldBe LibraryTab.defaultSortMode(tabId = tabId)
    }

    @Test
    fun `a choice for another tab does not affect this tab`() {
        // ----- Arrange -----
        val tabId = LibraryTab.Status.of(UserBookStatus.READ).id

        // ----- Act -----
        val result = resolveSortMode(
            sortModeByTab = mapOf("other" to LibrarySortMode.TITLE),
            tabId = tabId,
        )

        // ----- Assert -----
        result shouldBe LibraryTab.defaultSortMode(tabId = tabId)
    }
}
