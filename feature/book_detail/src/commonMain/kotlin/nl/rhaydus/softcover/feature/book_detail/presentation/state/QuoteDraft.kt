package nl.rhaydus.softcover.feature.book_detail.presentation.state

import kotlinx.datetime.LocalDateTime
import nl.rhaydus.softcover.core.component.quote.QuotePositionTab
import nl.rhaydus.softcover.core.domain.model.PrivacySetting

/**
 * The quote the reader is composing. Its presence on the state is what keeps the quote sheet open.
 *
 * [positionText] backs the Page and Percentage tabs; the Time tab reads [hours], [minutes] and [seconds].
 * [readAt] is the backdate for this quote alone, `null` being "just now".
 */
internal data class QuoteDraft(
    val tab: QuotePositionTab,
    val privacy: PrivacySetting,
    val text: String = "",
    val positionText: String = "",
    val hours: String = "",
    val minutes: String = "",
    val seconds: String = "",
    val readAt: LocalDateTime? = null,
    val progressAdvanceEnabled: Boolean = true,
)
