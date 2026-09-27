package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.component.chip.ChipLeading
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab

class RearrangeChipSnapshotTest {
    private val readingTab = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING)
    private val dnfTab = LibraryTab.Status.of(UserBookStatus.DID_NOT_FINISH)
    private val customListTab = LibraryTab.CustomList(
        listId = 10,
        listName = "Winter reading",
    )

    @Nested
    inner class Compute {
        @Test
        fun `Status tab on MANUAL, not DNF, with 2+ books gets a rearrange chip`() {
            // ----- Arrange -----
            val snapshot = RearrangeChipSnapshot(
                visibleTabs = listOf(readingTab),
                sortModeByTab = mapOf(readingTab.id to LibrarySortMode.MANUAL),
                displayBookCountByTab = mapOf(readingTab.id to 2),
                displayEditionCountByTab = emptyMap(),
                rankedCustomListIds = emptySet(),
                isRearranging = false,
            )

            // ----- Act -----
            val chipByTab = snapshot.compute()

            // ----- Assert -----
            chipByTab.keys shouldBe setOf(readingTab.id)
        }

        @Test
        fun `Status tab that is Did Not Finish never gets a chip even on MANUAL with 2+ books`() {
            // ----- Arrange -----
            val snapshot = RearrangeChipSnapshot(
                visibleTabs = listOf(dnfTab),
                sortModeByTab = mapOf(dnfTab.id to LibrarySortMode.MANUAL),
                displayBookCountByTab = mapOf(dnfTab.id to 5),
                displayEditionCountByTab = emptyMap(),
                rankedCustomListIds = emptySet(),
                isRearranging = false,
            )

            // ----- Act -----
            val chipByTab = snapshot.compute()

            // ----- Assert -----
            chipByTab shouldBe emptyMap()
        }

        @Test
        fun `Status tab with fewer than 2 books gets no chip`() {
            // ----- Arrange -----
            val snapshot = RearrangeChipSnapshot(
                visibleTabs = listOf(readingTab),
                sortModeByTab = mapOf(readingTab.id to LibrarySortMode.MANUAL),
                displayBookCountByTab = mapOf(readingTab.id to 1),
                displayEditionCountByTab = emptyMap(),
                rankedCustomListIds = emptySet(),
                isRearranging = false,
            )

            // ----- Act -----
            val chipByTab = snapshot.compute()

            // ----- Assert -----
            chipByTab shouldBe emptyMap()
        }

        @Test
        fun `Status tab on a non-MANUAL sort mode gets no chip`() {
            // ----- Arrange -----
            val snapshot = RearrangeChipSnapshot(
                visibleTabs = listOf(readingTab),
                sortModeByTab = mapOf(readingTab.id to LibrarySortMode.TITLE),
                displayBookCountByTab = mapOf(readingTab.id to 5),
                displayEditionCountByTab = emptyMap(),
                rankedCustomListIds = emptySet(),
                isRearranging = false,
            )

            // ----- Act -----
            val chipByTab = snapshot.compute()

            // ----- Assert -----
            chipByTab shouldBe emptyMap()
        }

        @Test
        fun `CustomList tab on ORDER, ranked, with 2+ editions gets a rearrange chip`() {
            // ----- Arrange -----
            val snapshot = RearrangeChipSnapshot(
                visibleTabs = listOf(customListTab),
                sortModeByTab = mapOf(customListTab.id to LibrarySortMode.ORDER),
                displayBookCountByTab = emptyMap(),
                displayEditionCountByTab = mapOf(customListTab.id to 2),
                rankedCustomListIds = setOf(customListTab.listId),
                isRearranging = false,
            )

            // ----- Act -----
            val chipByTab = snapshot.compute()

            // ----- Assert -----
            chipByTab.keys shouldBe setOf(customListTab.id)
        }

        @Test
        fun `CustomList tab that is not ranked gets no chip even on ORDER with 2+ editions`() {
            // ----- Arrange -----
            val snapshot = RearrangeChipSnapshot(
                visibleTabs = listOf(customListTab),
                sortModeByTab = mapOf(customListTab.id to LibrarySortMode.ORDER),
                displayBookCountByTab = emptyMap(),
                displayEditionCountByTab = mapOf(customListTab.id to 5),
                rankedCustomListIds = emptySet(),
                isRearranging = false,
            )

            // ----- Act -----
            val chipByTab = snapshot.compute()

            // ----- Assert -----
            chipByTab shouldBe emptyMap()
        }

        @Test
        fun `the All tab never gets a chip regardless of sort mode or counts`() {
            // ----- Arrange -----
            val snapshot = RearrangeChipSnapshot(
                visibleTabs = listOf(LibraryTab.All),
                sortModeByTab = mapOf(LibraryTab.All.id to LibrarySortMode.MANUAL),
                displayBookCountByTab = mapOf(LibraryTab.All.id to 10),
                displayEditionCountByTab = emptyMap(),
                rankedCustomListIds = emptySet(),
                isRearranging = false,
            )

            // ----- Act -----
            val chipByTab = snapshot.compute()

            // ----- Assert -----
            chipByTab shouldBe emptyMap()
        }

        @Test
        fun `isRearranging shows a chip on every visible tab regardless of its own gating`() {
            // ----- Arrange -----
            val snapshot = RearrangeChipSnapshot(
                visibleTabs = listOf(LibraryTab.All, dnfTab, customListTab),
                sortModeByTab = emptyMap(),
                displayBookCountByTab = emptyMap(),
                displayEditionCountByTab = emptyMap(),
                rankedCustomListIds = emptySet(),
                isRearranging = true,
            )

            // ----- Act -----
            val chipByTab = snapshot.compute()

            // ----- Assert -----
            chipByTab.keys shouldBe setOf(LibraryTab.All.id, dnfTab.id, customListTab.id)
        }

        @Test
        fun `isRearranging chip is labelled Done and selected`() {
            // ----- Arrange -----
            val snapshot = RearrangeChipSnapshot(
                visibleTabs = listOf(readingTab),
                sortModeByTab = mapOf(readingTab.id to LibrarySortMode.MANUAL),
                displayBookCountByTab = mapOf(readingTab.id to 2),
                displayEditionCountByTab = emptyMap(),
                rankedCustomListIds = emptySet(),
                isRearranging = true,
            )

            // ----- Act -----
            val chip = snapshot.compute().getValue(readingTab.id)

            // ----- Assert -----
            chip.label shouldBe "Done"
            chip.tone shouldBe ChipTone.Tonal
            chip.selected shouldBe true
            chip.leading shouldBe ChipLeading.Icon(icon = SoftcoverIcon.DragHandle)
        }

        @Test
        fun `idle rearrange chip is labelled Reorder and not selected`() {
            // ----- Arrange -----
            val snapshot = RearrangeChipSnapshot(
                visibleTabs = listOf(readingTab),
                sortModeByTab = mapOf(readingTab.id to LibrarySortMode.MANUAL),
                displayBookCountByTab = mapOf(readingTab.id to 2),
                displayEditionCountByTab = emptyMap(),
                rankedCustomListIds = emptySet(),
                isRearranging = false,
            )

            // ----- Act -----
            val chip = snapshot.compute().getValue(readingTab.id)

            // ----- Assert -----
            chip.label shouldBe "Reorder"
            chip.tone shouldBe ChipTone.Tonal
            chip.selected shouldBe false
        }

        @Test
        fun `a tab missing from sortModeByTab falls back to LibraryTab's default sort mode`() {
            // ----- Arrange -----
            // readingTab's default sort mode is DATE_ADDED (LibrarySortMode.Default), not MANUAL,
            // so with no entry in sortModeByTab it must not qualify for a rearrange chip.
            val snapshot = RearrangeChipSnapshot(
                visibleTabs = listOf(readingTab),
                sortModeByTab = emptyMap(),
                displayBookCountByTab = mapOf(readingTab.id to 5),
                displayEditionCountByTab = emptyMap(),
                rankedCustomListIds = emptySet(),
                isRearranging = false,
            )

            // ----- Act -----
            val chipByTab = snapshot.compute()

            // ----- Assert -----
            chipByTab shouldBe emptyMap()
        }
    }
}
