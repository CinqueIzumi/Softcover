package nl.rhaydus.softcover.feature.settings.presentation.state

import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
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
                persistedEnabledStatusCodes = persistentSetOf(1),
                draftEnabledStatusCodes = persistentSetOf(99),
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
                persistedEnabledStatusCodes = persistentSetOf(1, 2),
                draftEnabledStatusCodes = persistentSetOf(1, 2),
                persistedEnabledListIds = persistentSetOf(10),
                draftEnabledListIds = persistentSetOf(10),
                persistedTabOrder = persistentListOf("list-1", "status-2"),
                draftTabOrder = persistentListOf("list-1", "status-2"),
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
                persistedEnabledStatusCodes = persistentSetOf(1),
                draftEnabledStatusCodes = persistentSetOf(1),
                persistedEnabledListIds = persistentSetOf(10),
                draftEnabledListIds = persistentSetOf(10),
                persistedTabOrder = persistentListOf("status-1", "list-10"),
                draftTabOrder = persistentListOf("list-10", "status-1"),
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
                persistedEnabledStatusCodes = persistentSetOf(1),
                draftEnabledStatusCodes = persistentSetOf(1, 3),
                persistedEnabledListIds = persistentSetOf(),
                draftEnabledListIds = persistentSetOf(),
                persistedTabOrder = persistentListOf(),
                draftTabOrder = persistentListOf(),
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
                persistedEnabledStatusCodes = persistentSetOf(),
                draftEnabledStatusCodes = persistentSetOf(),
                persistedEnabledListIds = persistentSetOf(5),
                draftEnabledListIds = persistentSetOf(5, 7),
                persistedTabOrder = persistentListOf(),
                draftTabOrder = persistentListOf(),
            )

            // ----- Act -----
            val isDirty = state.isDirty

            // ----- Assert -----
            isDirty shouldBe true
        }
    }
}
