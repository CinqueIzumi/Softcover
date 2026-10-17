package nl.rhaydus.softcover.orchestration.session

import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ReadingSession
import nl.rhaydus.softcover.core.presentation.session.ActiveSessionUiModel
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel

private const val SESSION_TRANSITION_SURFACE = "session"

internal fun toActiveSessionUiModel(
    session: ReadingSession,
    book: Book,
): ActiveSessionUiModel =
    ActiveSessionUiModel(
        title = book.title,
        author = book.authors.firstOrNull()?.name,
        isPaused = session.isPaused,
        startedAtEpochMs = session.startedAt.toEpochMilliseconds(),
        pausedSeconds = session.pausedSeconds,
        pausedAtEpochMs = session.lastPausedAt?.toEpochMilliseconds(),
        peekBarCover = book.toCoverUiModel(
            variant = CoverVariant.Flat,
            shareTransition = true,
            transitionSurface = SESSION_TRANSITION_SURFACE,
        ),
        focusCover = book.toCoverUiModel(
            variant = CoverVariant.Spotlight,
            shareTransition = true,
            transitionSurface = SESSION_TRANSITION_SURFACE,
        ),
    )
