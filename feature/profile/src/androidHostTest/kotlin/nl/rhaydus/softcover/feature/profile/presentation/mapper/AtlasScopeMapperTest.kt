package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.domain.model.StatsWindow
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AtlasScopeMapperTest {
    private val today = LocalDate(
        year = 2026,
        month = 10,
        day = 23,
    )

    @Nested
    inner class AtlasScopeChips {
        @Test
        fun `orders the rolling windows, years as given, then all time`() {
            // ----- Arrange -----
            val yearsRead = listOf(2026, 2024, 2021)

            // ----- Act -----
            val result = atlasScopeChips(
                yearsRead = yearsRead,
                selectedKey = ALL_TIME_SCOPE_KEY,
            )

            // ----- Assert -----
            result.chips.map { it.label }.shouldContainExactly(
                "Last 30 days",
                "Last 6 months",
                "Last 12 months",
                "2026",
                "2024",
                "2021",
                "All time",
            )
        }

        @Test
        fun `selects exactly the chip with the selected key`() {
            // ----- Arrange -----
            val yearsRead = listOf(2026, 2025)
            val selectedKey = statsScopeKey(StatsScope.CalendarYear(2025))

            // ----- Act -----
            val result = atlasScopeChips(
                yearsRead = yearsRead,
                selectedKey = selectedKey,
            )

            // ----- Assert -----
            result.chips.filter { it.selected }.map { it.label }.shouldContainExactly("2025")
        }

        @Test
        fun `a reader with no finished years gets the rolling windows and all time`() {
            // ----- Arrange -----
            val yearsRead = emptyList<Int>()

            // ----- Act -----
            val result = atlasScopeChips(
                yearsRead = yearsRead,
                selectedKey = ALL_TIME_SCOPE_KEY,
            )

            // ----- Assert -----
            result.chips.map { it.label }.shouldContainExactly(
                "Last 30 days",
                "Last 6 months",
                "Last 12 months",
                "All time",
            )
        }

        @Test
        fun `resolves each chip key back to its scope`() {
            // ----- Arrange -----
            val chips = atlasScopeChips(
                yearsRead = listOf(2025),
                selectedKey = ALL_TIME_SCOPE_KEY,
            )

            // ----- Act -----
            val scopes = chips.chips.map { chips[it.key] }

            // ----- Assert -----
            scopes.shouldContainExactly(
                StatsScope.Rolling(StatsWindow.LAST_30_DAYS),
                StatsScope.Rolling(StatsWindow.LAST_6_MONTHS),
                StatsScope.Rolling(StatsWindow.LAST_12_MONTHS),
                StatsScope.CalendarYear(2025),
                StatsScope.AllTime,
            )
        }
    }

    @Nested
    inner class AtlasScopeLine {
        @Test
        fun `a calendar year names the year`() {
            // ----- Arrange -----
            val scope = StatsScope.CalendarYear(2026)

            // ----- Act -----
            val result = atlasScopeLine(
                scope = scope,
                today = today,
            )

            // ----- Assert -----
            result shouldBe "Showing 2026"
        }

        @Test
        fun `all time shows every year`() {
            // ----- Arrange -----
            val scope = StatsScope.AllTime

            // ----- Act -----
            val result = atlasScopeLine(
                scope = scope,
                today = today,
            )

            // ----- Assert -----
            result shouldBe "Showing every year"
        }

        @Test
        fun `a rolling window inside the current year omits the year`() {
            // ----- Arrange -----
            val scope = StatsScope.Rolling(StatsWindow.LAST_30_DAYS)

            // ----- Act -----
            val result = atlasScopeLine(
                scope = scope,
                today = today,
            )

            // ----- Assert -----
            result shouldBe "Showing 24 September to today"
        }

        @Test
        fun `a rolling window that starts in an earlier year names that year`() {
            // ----- Arrange -----
            val scope = StatsScope.Rolling(StatsWindow.LAST_12_MONTHS)

            // ----- Act -----
            val result = atlasScopeLine(
                scope = scope,
                today = today,
            )

            // ----- Assert -----
            result shouldBe "Showing 24 October 2025 to today"
        }
    }

    @Nested
    inner class NoFinishedBooksSentence {
        @Test
        fun `names a rolling window when nothing was finished in it`() {
            // ----- Arrange -----
            val scope = StatsScope.Rolling(StatsWindow.LAST_6_MONTHS)

            // ----- Act -----
            val result = scope.noFinishedBooksSentence(booksRead = 0)

            // ----- Assert -----
            result shouldBe "No finished books in the last 6 months."
        }

        @Test
        fun `names a calendar year when nothing was finished in it`() {
            // ----- Arrange -----
            val scope = StatsScope.CalendarYear(2024)

            // ----- Act -----
            val result = scope.noFinishedBooksSentence(booksRead = 0)

            // ----- Assert -----
            result shouldBe "No finished books in 2024."
        }

        @Test
        fun `is null once the scope has finished books`() {
            // ----- Arrange -----
            val scope = StatsScope.CalendarYear(2024)

            // ----- Act -----
            val result = scope.noFinishedBooksSentence(booksRead = 3)

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `is null for all time even when nothing was finished`() {
            // ----- Arrange -----
            val scope = StatsScope.AllTime

            // ----- Act -----
            val result = scope.noFinishedBooksSentence(booksRead = 0)

            // ----- Assert -----
            result shouldBe null
        }
    }
}
