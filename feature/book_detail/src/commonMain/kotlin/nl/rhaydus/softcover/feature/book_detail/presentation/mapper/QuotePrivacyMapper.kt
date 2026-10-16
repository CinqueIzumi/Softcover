package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import nl.rhaydus.softcover.core.component.quote.QuotePrivacyOption
import nl.rhaydus.softcover.core.domain.model.PrivacySetting

internal fun PrivacySetting.toQuotePrivacyOption(): QuotePrivacyOption = when (this) {
    PrivacySetting.PUBLIC -> QuotePrivacyOption.PUBLIC
    PrivacySetting.FOLLOWERS -> QuotePrivacyOption.FOLLOWERS
    PrivacySetting.PRIVATE -> QuotePrivacyOption.PRIVATE
}

internal fun QuotePrivacyOption.toPrivacySetting(): PrivacySetting = when (this) {
    QuotePrivacyOption.PUBLIC -> PrivacySetting.PUBLIC
    QuotePrivacyOption.FOLLOWERS -> PrivacySetting.FOLLOWERS
    QuotePrivacyOption.PRIVATE -> PrivacySetting.PRIVATE
}

/** The reader's default for a new quote: the book's own privacy, or public when the book has none. */
internal fun privacySettingFor(code: Int?): PrivacySetting =
    PrivacySetting.entries.firstOrNull { it.code == code } ?: PrivacySetting.PUBLIC
