package nl.rhaydus.softcover.feature.explore.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.feature.explore.domain.usecase.DismissContinueSeriesBookUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.DismissContinueSeriesUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.EnrichDismissedContinueSeriesMetadataUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetDismissedContinueSeriesBooksUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetDismissedContinueSeriesUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.UndoContinueSeriesBookDismissalUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.UndoContinueSeriesDismissalUseCase
import nl.rhaydus.toad.ActionDependencies

internal class HiddenSuggestionsDependencies(
    val getDismissedContinueSeriesBooksUseCase: GetDismissedContinueSeriesBooksUseCase,
    val getDismissedContinueSeriesUseCase: GetDismissedContinueSeriesUseCase,
    val undoContinueSeriesBookDismissalUseCase: UndoContinueSeriesBookDismissalUseCase,
    val undoContinueSeriesDismissalUseCase: UndoContinueSeriesDismissalUseCase,
    val dismissContinueSeriesBookUseCase: DismissContinueSeriesBookUseCase,
    val dismissContinueSeriesUseCase: DismissContinueSeriesUseCase,
    val enrichDismissedContinueSeriesMetadataUseCase: EnrichDismissedContinueSeriesMetadataUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
