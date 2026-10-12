package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import nl.rhaydus.softcover.core.component.callout.CalloutUiModel
import nl.rhaydus.softcover.core.component.callout.CalloutVariant
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.DateStyle

private const val FINISHED_EYEBROW = "Finished"
private const val DID_NOT_FINISH_EYEBROW = "Did not finish"
private const val WANT_TO_READ_EYEBROW = "Up next"

internal fun Book.toStatusCallout(dateStyle: DateStyle): CalloutUiModel? {
    val userBook = userBook ?: return null

    return when (status) {
        BookStatus.Read -> statusCallout(
            eyebrow = FINISHED_EYEBROW,
            icon = SoftcoverIcon.BookmarkCheck,
            body = when (
                val readDate = userBook.getReadDateString(
                    style = dateStyle,
                    finishedAt = userBookRead?.finishedAt,
                )
            ) {
                null -> "This book has been in your library since ${userBook.getFallbackDateString(style = dateStyle)}."
                else -> "You finished this on $readDate."
            },
        )

        BookStatus.DidNotFinish -> statusCallout(
            eyebrow = DID_NOT_FINISH_EYEBROW,
            icon = SoftcoverIcon.Bookmark,
            body = when (val dnfDate = userBook.getDnfDateString(style = dateStyle)) {
                null -> "This book has been in your library since ${userBook.getFallbackDateString(style = dateStyle)}."
                else -> "You put this down on $dnfDate."
            },
        )

        BookStatus.WantToRead -> statusCallout(
            eyebrow = WANT_TO_READ_EYEBROW,
            icon = SoftcoverIcon.BookmarkAdd,
            body = "On your Want to read shelf since ${userBook.getFallbackDateString(style = dateStyle)}.",
        )

        BookStatus.Reading,
        BookStatus.None,
        -> null
    }
}

private fun statusCallout(
    eyebrow: String,
    icon: SoftcoverIcon,
    body: String,
): CalloutUiModel = CalloutUiModel(
    variant = CalloutVariant.Neutral(icon = icon),
    eyebrow = eyebrow,
    body = body,
)
