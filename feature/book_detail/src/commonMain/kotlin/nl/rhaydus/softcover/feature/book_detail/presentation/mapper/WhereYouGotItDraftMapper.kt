package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import nl.rhaydus.softcover.core.domain.model.AcquisitionSource
import nl.rhaydus.softcover.core.domain.model.BookCopy
import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItDraft

internal fun BookCopy.toWhereYouGotItDraft(): WhereYouGotItDraft = when (val stored = source) {
    is AcquisitionSource.Preset -> WhereYouGotItDraft.Preset(key = stored.kind.toKey())

    is AcquisitionSource.Other -> WhereYouGotItDraft.Custom(text = stored.text)

    null -> WhereYouGotItDraft.None
}

internal fun WhereYouGotItDraft.withPresetClicked(key: String): WhereYouGotItDraft =
    if (this == WhereYouGotItDraft.Preset(key = key)) WhereYouGotItDraft.None else WhereYouGotItDraft.Preset(key = key)

internal fun WhereYouGotItDraft.withCustomText(text: String): WhereYouGotItDraft =
    if (text.isEmpty()) WhereYouGotItDraft.None else WhereYouGotItDraft.Custom(text = text)

internal fun WhereYouGotItDraft.toSource(): AcquisitionSource? = when (this) {
    WhereYouGotItDraft.None -> null

    is WhereYouGotItDraft.Preset -> key.toAcquisitionKind()?.let { AcquisitionSource.Preset(kind = it) }

    is WhereYouGotItDraft.Custom -> text.trim().takeIf { it.isNotEmpty() }?.let { AcquisitionSource.Other(text = it) }
}
