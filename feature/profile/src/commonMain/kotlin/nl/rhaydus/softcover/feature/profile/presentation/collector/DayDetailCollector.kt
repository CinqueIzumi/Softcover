package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.component.sheet.isClosing
import nl.rhaydus.softcover.core.component.sheet.openOrUpdate
import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.dayDetailUiModel
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.DayDetailUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope

/**
 * The only writer that opens or updates the day sheet. The sheet follows `selectedReadingDay`: it opens or updates while a day is
 * selected and hides when the selection clears. A day whose pages fall to zero, because a sync replaced its
 * rows, clears the selection itself. A sheet already hiding is left alone, so a late emission cannot reopen it.
 */
internal class DayDetailCollector : StatsAtlasCollector {
    override suspend fun onLaunch(
        scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>,
        dependencies: StatsAtlasDependencies,
    ) {
        scope.state
            .map { it.selectedReadingDay }
            .distinctUntilChanged()
            .flatMapLatest { day ->
                if (day == null) {
                    flowOf(null)
                } else {
                    dayDetail(
                        day = day,
                        scope = scope,
                        dependencies = dependencies,
                    )
                }
            }
            .collect { model ->
                scope.setState {
                    val sheet = it.dayDetailSheet

                    when {
                        model == null -> it.copy(dayDetailSheet = sheet.requestClose())
                        sheet.isClosing -> it
                        else -> it.copy(dayDetailSheet = sheet.openOrUpdate(model = model))
                    }
                }
            }
    }

    private fun dayDetail(
        day: LocalDate,
        scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>,
        dependencies: StatsAtlasDependencies,
    ): Flow<DayDetailUiModel> = combine(
        dependencies.observeReadingDayUseCase(day),
        dependencies.observeCurrentDateUseCase(),
    ) { readingDay, today -> readingDay to today.year }
        .transformLatest { (readingDay, currentYear) ->
            if (readingDay.books.sumOf { it.pages } == 0) {
                scope.setState { it.copy(selectedReadingDay = null) }

                return@transformLatest
            }

            emitAll(
                dependencies.observeBooksByIdsUseCase(readingDay.books.map { it.bookId }).map { books ->
                    dayDetailUiModel(
                        day = readingDay,
                        books = books,
                        currentYear = currentYear,
                    )
                },
            )
        }
}
