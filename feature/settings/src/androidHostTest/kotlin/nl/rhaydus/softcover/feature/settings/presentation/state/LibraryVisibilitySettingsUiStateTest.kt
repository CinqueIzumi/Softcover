package nl.rhaydus.softcover.feature.settings.presentation.state

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryVisibilitySettingsUiStateTest {
    @Nested
    inner class IsDirty {
        @Test
        fun `is false when not initialized`() {
            // ----- Arrange -----
            val state = LibraryVisibilitySettingsUiState(
                initialized = false,
                persistedEnabledStatusCodes = setOf(1),
                draftEnabledStatusCodes = setOf(99),
            )

            // ----- Act -----
            val isDirty = state.isDirty

            // ----- Assert -----
            isDirty shouldBe false
        }

        @Test
        fun `is false when initialized and draft matches persisted across all three fields`() {
            // ----- Arrange -----
            val state = LibraryVisibilitySettingsUiState(
                initialized = true,
                persistedEnabledStatusCodes = setOf(1, 2),
                draftEnabledStatusCodes = setOf(1, 2),
                persistedEnabledListIds = setOf(10),
                draftEnabledListIds = setOf(10),
                persistedTabOrder = listOf("list-1", "status-2"),
                draftTabOrder = listOf("list-1", "status-2"),
            )

            // ----- Act -----
            val isDirty = state.isDirty

            // ----- Assert -----
            isDirty shouldBe false
        }

        @Test
        fun `is true when only draftTabOrder differs from persistedTabOrder`() {
            // ----- Arrange -----
            val state = LibraryVisibilitySettingsUiState(
                initialized = true,
                persistedEnabledStatusCodes = setOf(1),
                draftEnabledStatusCodes = setOf(1),
                persistedEnabledListIds = setOf(10),
                draftEnabledListIds = setOf(10),
                persistedTabOrder = listOf("status-1", "list-10"),
                draftTabOrder = listOf("list-10", "status-1"),
            )

            // ----- Act -----
            val isDirty = state.isDirty

            // ----- Assert -----
            isDirty shouldBe true
        }

        @Test
        fun `is true when draftEnabledStatusCodes differs from persisted`() {
            // ----- Arrange -----
            val state = LibraryVisibilitySettingsUiState(
                initialized = true,
                persistedEnabledStatusCodes = setOf(1),
                draftEnabledStatusCodes = setOf(1, 3),
                persistedEnabledListIds = emptySet(),
                draftEnabledListIds = emptySet(),
                persistedTabOrder = emptyList(),
                draftTabOrder = emptyList(),
            )

            // ----- Act -----
            val isDirty = state.isDirty

            // ----- Assert -----
            isDirty shouldBe true
        }

        @Test
        fun `is true when draftEnabledListIds differs from persisted`() {
            // ----- Arrange -----
            val state = LibraryVisibilitySettingsUiState(
                initialized = true,
                persistedEnabledStatusCodes = emptySet(),
                draftEnabledStatusCodes = emptySet(),
                persistedEnabledListIds = setOf(5),
                draftEnabledListIds = setOf(5, 7),
                persistedTabOrder = emptyList(),
                draftTabOrder = emptyList(),
            )

            // ----- Act -----
            val isDirty = state.isDirty

            // ----- Assert -----
            isDirty shouldBe true
        }
    }
}
