package nl.rhaydus.softcover.feature.profile.presentation.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.component.sheet.SheetModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.profile.domain.model.ReadingDay
import nl.rhaydus.softcover.core.profile.domain.model.ReadingDayBook
import nl.rhaydus.softcover.feature.profile.presentation.mapper.dayDetailUiModel

/**
 * One reading day's sheet: the finished [scaffold] and a row per book in [books].
 */
@Immutable
internal data class DayDetailUiModel(
    val scaffold: SheetScaffoldUiModel,
    val books: BookCardSet<DayDetailBook>,
) : SheetModel<DayDetailUiModel> {
    override val closeRequested: Boolean get() = scaffold.closeRequested

    override fun requestingClose(): DayDetailUiModel = copy(scaffold = scaffold.requestingClose())

    companion object : UiModelPreviews<DayDetailUiModel> {
        private val previewDate = LocalDate(
            year = 2026,
            month = 4,
            day = 14,
        )

        override val previews: ImmutableMap<String, DayDetailUiModel> = persistentMapOf(
            "Two books, with update times" to dayDetailUiModel(
                day = ReadingDay(
                    date = previewDate,
                    books = listOf(
                        ReadingDayBook(
                            bookId = 1,
                            fromPage = 112,
                            toPage = 158,
                            updateCount = 2,
                            pages = 46,
                            finished = false,
                            firstUpdateAt = LocalTime(
                                hour = 9,
                                minute = 10,
                            ),
                            lastUpdateAt = LocalTime(
                                hour = 22,
                                minute = 40,
                            ),
                        ),
                        ReadingDayBook(
                            bookId = 2,
                            fromPage = 0,
                            toPage = 16,
                            updateCount = 1,
                            pages = 16,
                            finished = false,
                            firstUpdateAt = LocalTime(
                                hour = 8,
                                minute = 5,
                            ),
                            lastUpdateAt = LocalTime(
                                hour = 8,
                                minute = 5,
                            ),
                        ),
                    ),
                ),
                books = listOf(
                    previewBook(
                        id = 1,
                        title = "Piranesi",
                    ),
                    previewBook(
                        id = 2,
                        title = "The Name of the Wind",
                    ),
                ),
                currentYear = previewDate.year,
                streaks = AtlasStreaks(),
            ),
            "Finished remainder, missing book" to dayDetailUiModel(
                day = ReadingDay(
                    date = previewDate,
                    books = listOf(
                        ReadingDayBook(
                            bookId = 1,
                            fromPage = 380,
                            toPage = 412,
                            updateCount = 0,
                            pages = 32,
                            finished = true,
                            firstUpdateAt = null,
                            lastUpdateAt = null,
                        ),
                        ReadingDayBook(
                            bookId = 3,
                            fromPage = 10,
                            toPage = 30,
                            updateCount = 1,
                            pages = 20,
                            finished = false,
                            firstUpdateAt = LocalTime(
                                hour = 14,
                                minute = 41,
                            ),
                            lastUpdateAt = LocalTime(
                                hour = 14,
                                minute = 41,
                            ),
                        ),
                    ),
                ),
                books = listOf(
                    previewBook(
                        id = 1,
                        title = "Piranesi",
                    ),
                ),
                currentYear = previewDate.year + 1,
                streaks = AtlasStreaks(),
            ),
        )

        private fun previewBook(
            id: Int,
            title: String,
        ): Book = Book(
            id = id,
            title = title,
            editions = emptyList(),
            defaultEdition = null,
            rating = 4.2,
            description = "",
            releaseYear = 2020,
            coverUrl = "",
            authors = emptyList(),
            usersCount = 0,
            ratingsCount = 0,
            bookSeries = null,
            positionsInSeries = emptyList(),
            isCompilation = false,
            userBook = null,
        )
    }
}
