package nl.rhaydus.softcover.feature.widget.presentation.mapper

import android.net.Uri
import app.cash.turbine.test
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.core.domain.model.UpcomingWindow
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeed
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeedFetchStatus
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingPosition
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingSettings
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingSettingsChange
import nl.rhaydus.softcover.feature.widget.presentation.model.TrendingWidgetUiModel
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class TrendingWidgetModelsTest {
    private val requestedBookIds = mutableListOf<Int>()
    private val coverUris = mutableMapOf<Int, Uri?>()
    private val coverDelaysMs = mutableMapOf<Int, Long>()

    private fun book(id: Int): Book = mockk<Book> {
        every {
            this@mockk.id
        } returns id
        every {
            displayTitle
        } returns "Title $id"
        every {
            authorString
        } returns "Author $id"
    }

    private fun models(
        isSignedIn: Boolean = true,
        position: Flow<TrendingPosition> = flowOf(positionAt(0)),
        settingsChanges: Flow<TrendingSettingsChange> = emptyFlow(),
        fetchStatus: (TrendingFeed) -> Flow<TrendingFeedFetchStatus> = { flowOf(TrendingFeedFetchStatus.Settled) },
        observeTrending: (TrendingFeed) -> Flow<List<Book>>,
    ): Flow<TrendingWidgetUiModel> = trendingWidgetModels(
        isSignedIn = flowOf(isSignedIn),
        themeConfiguration = flowOf(ThemeConfiguration()),
        position = position,
        settingsChanges = settingsChanges,
        observeTrending = observeTrending,
        fetchStatus = fetchStatus,
        eyebrowFor = { feed -> "Eyebrow ${feed.eyebrowName()}" },
        emptyHeadlineFor = { feed -> "Headline ${feed.eyebrowName()}" },
        coverUri = { book ->
            requestedBookIds += book.id
            delay(coverDelaysMs[book.id] ?: 0L)
            coverUris[book.id]
        },
    )

    private fun TrendingFeed.eyebrowName(): String = when (this) {
        is TrendingFeed.Trending -> period.name
        is TrendingFeed.Upcoming -> "Upcoming${window.name}"
    }

    private fun positionAt(
        page: Int,
        period: TrendingPeriod = TrendingPeriod.Week,
    ): TrendingPosition = TrendingPosition(
        feed = TrendingFeed.Trending(period = period),
        page = page,
    )

    private fun fixedBooks(books: List<Book>): (TrendingFeed) -> Flow<List<Book>> = { flowOf(books) }

    @Nested
    inner class TrendingWidgetModels {
        @Test
        fun `signed out shows the signed out content`() = runTest {
            // ----- Arrange -----
            val flow = models(
                isSignedIn = false,
                observeTrending = fixedBooks(listOf(book(1))),
            )

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                awaitItem().content shouldBe TrendingWidgetUiModel.Content.SignedOut
                awaitComplete()
            }
        }

        @Test
        fun `an empty list after a completed fetch shows the empty content with the feed's eyebrow and headline`() =
            runTest {
                // ----- Arrange -----
                val flow = models(observeTrending = fixedBooks(emptyList()))

                // ----- Act -----
                flow.test {
                    // ----- Assert -----
                    awaitItem().content shouldBe TrendingWidgetUiModel.Content.Empty(
                        eyebrow = "Eyebrow Week",
                        headline = "Headline Week",
                    )
                    awaitComplete()
                }
            }

        @Test
        fun `an empty list shows the loader while the feed is still loading`() = runTest {
            // ----- Arrange -----
            val flow = models(
                fetchStatus = { flowOf(TrendingFeedFetchStatus.Loading) },
                observeTrending = fixedBooks(emptyList()),
            )

            // ----- Act -----
            val content = flow.first().content

            // ----- Assert -----
            content shouldBe TrendingWidgetUiModel.Content.Switching(eyebrow = "Eyebrow Week")
        }

        @Test
        fun `an empty list after a failed fetch shows the fetch failed content with the feed's eyebrow`() = runTest {
            // ----- Arrange -----
            val flow = models(
                fetchStatus = { flowOf(TrendingFeedFetchStatus.Failed) },
                observeTrending = fixedBooks(emptyList()),
            )

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                awaitItem().content shouldBe TrendingWidgetUiModel.Content.FetchFailed(eyebrow = "Eyebrow Week")
                awaitComplete()
            }
        }

        @Test
        fun `a ranking is shown even when the last fetch failed`() = runTest {
            // ----- Arrange -----
            val flow = models(
                fetchStatus = { flowOf(TrendingFeedFetchStatus.Failed) },
                observeTrending = fixedBooks(listOf(book(1))),
            )

            // ----- Act & Assert -----
            flow.test {
                awaitItem().content.shouldBeInstanceOf<TrendingWidgetUiModel.Content.Ranked>()
                awaitComplete()
            }
        }

        @Test
        fun `the loader gives way to the empty content when the fetch settles with nothing`() = runTest {
            // ----- Arrange -----
            val status = MutableStateFlow(TrendingFeedFetchStatus.Loading)
            val flow = models(
                fetchStatus = { status },
                observeTrending = fixedBooks(emptyList()),
            )

            // ----- Act & Assert -----
            flow.test {
                awaitItem().content shouldBe TrendingWidgetUiModel.Content.Switching(eyebrow = "Eyebrow Week")
                status.value = TrendingFeedFetchStatus.Settled
                awaitItem().content shouldBe TrendingWidgetUiModel.Content.Empty(
                    eyebrow = "Eyebrow Week",
                    headline = "Headline Week",
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `the loader of an upcoming feed carries that feed's eyebrow`() = runTest {
            // ----- Arrange -----
            val flow = models(
                position = flowOf(
                    TrendingPosition(
                        feed = TrendingFeed.Upcoming(window = UpcomingWindow.SixMonths),
                        page = 0,
                    ),
                ),
                fetchStatus = { flowOf(TrendingFeedFetchStatus.Loading) },
                observeTrending = fixedBooks(emptyList()),
            )

            // ----- Act -----
            val content = flow.first().content

            // ----- Assert -----
            content shouldBe TrendingWidgetUiModel.Content.Switching(eyebrow = "Eyebrow UpcomingSixMonths")
        }

        @Test
        fun `a ranking never shows the loader even while a fetch runs`() = runTest {
            // ----- Arrange -----
            val flow = models(
                fetchStatus = { flowOf(TrendingFeedFetchStatus.Loading) },
                observeTrending = fixedBooks(listOf(book(1))),
            )

            // ----- Act & Assert -----
            flow.test {
                awaitItem().content.shouldBeInstanceOf<TrendingWidgetUiModel.Content.Ranked>()
                awaitComplete()
            }
        }

        @Test
        fun `an upcoming feed's empty content takes its headline from that feed`() = runTest {
            // ----- Arrange -----
            val flow = models(
                position = flowOf(
                    TrendingPosition(
                        feed = TrendingFeed.Upcoming(window = UpcomingWindow.Year),
                        page = 0,
                    ),
                ),
                observeTrending = fixedBooks(emptyList()),
            )

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                awaitItem().content shouldBe TrendingWidgetUiModel.Content.Empty(
                    eyebrow = "Eyebrow UpcomingYear",
                    headline = "Headline UpcomingYear",
                )
                awaitComplete()
            }
        }

        @Test
        fun `the first page carries rank, book details and the page label`() = runTest {
            // ----- Arrange -----
            val flow = models(observeTrending = fixedBooks(listOf(book(30), book(10), book(20), book(40), book(50))))

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                val content = awaitItem().content.shouldBeInstanceOf<TrendingWidgetUiModel.Content.Ranked>()
                content.eyebrow shouldBe "Eyebrow Week"
                content.items.map { it.rank } shouldContainExactly listOf("1", "2")
                content.items.map { it.bookId } shouldContainExactly listOf(30, 10)
                content.items.first().title shouldBe "Title 30"
                content.items.first().author shouldBe "Author 30"
                content.pageLabel shouldBe "1/3"
                content.showsArrows shouldBe true
                awaitComplete()
            }
        }

        @Test
        fun `a later page continues the ranking and labels its page`() = runTest {
            // ----- Arrange -----
            val flow = models(
                position = flowOf(positionAt(1)),
                observeTrending = fixedBooks((1..10).map { book(it) }),
            )

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                val content = awaitItem().content.shouldBeInstanceOf<TrendingWidgetUiModel.Content.Ranked>()
                content.items.map { it.rank } shouldContainExactly listOf("3", "4")
                content.items.map { it.bookId } shouldContainExactly listOf(3, 4)
                content.pageLabel shouldBe "2/5"
                awaitComplete()
            }
        }

        @Test
        fun `a single page has no label and no arrows`() = runTest {
            // ----- Arrange -----
            val flow = models(observeTrending = fixedBooks(listOf(book(1), book(2))))

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                val content = awaitItem().content.shouldBeInstanceOf<TrendingWidgetUiModel.Content.Ranked>()
                content.items.map { it.rank } shouldContainExactly listOf("1", "2")
                content.pageLabel.shouldBeNull()
                content.showsArrows shouldBe false
                awaitComplete()
            }
        }

        @Test
        fun `the list is capped at ten so the last page is the fifth`() = runTest {
            // ----- Arrange -----
            val flow = models(
                position = flowOf(positionAt(4)),
                observeTrending = fixedBooks((1..15).map { book(it) }),
            )

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                val content = awaitItem().content.shouldBeInstanceOf<TrendingWidgetUiModel.Content.Ranked>()
                content.items.map { it.bookId } shouldContainExactly listOf(9, 10)
                content.pageLabel shouldBe "5/5"
                awaitComplete()
            }
        }

        @Test
        fun `a cover that resolves within the hit window is shown in a single emission`() = runTest {
            // ----- Arrange -----
            val uri = mockk<Uri>()
            coverUris[1] = uri
            coverDelaysMs[1] = 50L
            coverDelaysMs[2] = 50L
            val flow = models(observeTrending = fixedBooks(listOf(book(1), book(2))))

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                val content = awaitItem().content.shouldBeInstanceOf<TrendingWidgetUiModel.Content.Ranked>()
                content.items.map { it.cover } shouldContainExactly listOf(
                    TrendingWidgetUiModel.Cover.Loaded(uri = uri),
                    TrendingWidgetUiModel.Cover.Missing,
                )
                awaitComplete()
            }
        }

        @Test
        fun `a slow cover shows the page with pending covers first, then the resolved covers`() = runTest {
            // ----- Arrange -----
            val uri = mockk<Uri>()
            coverUris[1] = uri
            coverDelaysMs[1] = 200L
            coverDelaysMs[2] = 200L
            val flow = models(observeTrending = fixedBooks(listOf(book(1), book(2))))

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                val pending = awaitItem().content.shouldBeInstanceOf<TrendingWidgetUiModel.Content.Ranked>()
                pending.items.map { it.title } shouldContainExactly listOf("Title 1", "Title 2")
                pending.items.map { it.cover } shouldContainExactly List(2) { TrendingWidgetUiModel.Cover.Pending }
                val resolved = awaitItem().content.shouldBeInstanceOf<TrendingWidgetUiModel.Content.Ranked>()
                resolved.items.map { it.cover } shouldContainExactly listOf(
                    TrendingWidgetUiModel.Cover.Loaded(uri = uri),
                    TrendingWidgetUiModel.Cover.Missing,
                )
                awaitComplete()
            }
        }

        @Test
        fun `a middle page prefetches the covers of the previous and next page`() = runTest {
            // ----- Arrange -----
            val flow = models(
                position = flowOf(positionAt(1)),
                observeTrending = fixedBooks((1..10).map { book(it) }),
            )

            // ----- Act -----
            flow.test {
                awaitItem()
                awaitComplete()
            }

            // ----- Assert -----
            requestedBookIds.sorted() shouldContainExactly listOf(1, 2, 3, 4, 5, 6)
        }

        @Test
        fun `the first page prefetches the last page as its previous page`() = runTest {
            // ----- Arrange -----
            val flow = models(
                position = flowOf(positionAt(0)),
                observeTrending = fixedBooks((1..10).map { book(it) }),
            )

            // ----- Act -----
            flow.test {
                awaitItem()
                awaitComplete()
            }

            // ----- Assert -----
            requestedBookIds.sorted() shouldContainExactly listOf(1, 2, 3, 4, 9, 10)
        }

        @Test
        fun `the last page prefetches the first page as its next page`() = runTest {
            // ----- Arrange -----
            val flow = models(
                position = flowOf(positionAt(4)),
                observeTrending = fixedBooks((1..10).map { book(it) }),
            )

            // ----- Act -----
            flow.test {
                awaitItem()
                awaitComplete()
            }

            // ----- Assert -----
            requestedBookIds.sorted() shouldContainExactly listOf(1, 2, 7, 8, 9, 10)
        }

        @Test
        fun `two pages prefetch the other page once`() = runTest {
            // ----- Arrange -----
            val flow = models(observeTrending = fixedBooks((1..4).map { book(it) }))

            // ----- Act -----
            flow.test {
                awaitItem()
                awaitComplete()
            }

            // ----- Assert -----
            requestedBookIds.sorted() shouldContainExactly listOf(1, 2, 3, 4)
        }

        @Test
        fun `a single page prefetches nothing beyond its own covers`() = runTest {
            // ----- Arrange -----
            val flow = models(observeTrending = fixedBooks(listOf(book(1), book(2))))

            // ----- Act -----
            flow.test {
                awaitItem()
                awaitComplete()
            }

            // ----- Assert -----
            requestedBookIds.sorted() shouldContainExactly listOf(1, 2)
        }

        @Test
        fun `a stale stored page past the end is clamped to the last page`() = runTest {
            // ----- Arrange -----
            val flow = models(
                position = flowOf(positionAt(9)),
                observeTrending = fixedBooks((1..5).map { book(it) }),
            )

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                val content = awaitItem().content.shouldBeInstanceOf<TrendingWidgetUiModel.Content.Ranked>()
                content.items.map { it.bookId } shouldContainExactly listOf(5)
                content.pageLabel shouldBe "3/3"
                awaitComplete()
            }
        }

        @Test
        fun `switching the period resubscribes to that period's list`() = runTest {
            // ----- Arrange -----
            val position = MutableStateFlow(positionAt(0))
            val booksByFeed = mapOf<TrendingFeed, List<Book>>(
                TrendingFeed.Trending(period = TrendingPeriod.Week) to listOf(book(1), book(2)),
                TrendingFeed.Trending(period = TrendingPeriod.Year) to listOf(book(100), book(200)),
            )
            val flow = models(
                position = position,
                observeTrending = { feed -> flowOf(booksByFeed.getValue(feed)) },
            )

            // ----- Act -----
            lateinit var week: TrendingWidgetUiModel.Content
            lateinit var year: TrendingWidgetUiModel.Content
            flow.test {
                week = awaitItem().content
                position.value = positionAt(
                    page = 0,
                    period = TrendingPeriod.Year,
                )
                year = awaitItem().content
                cancelAndIgnoreRemainingEvents()
            }

            // ----- Assert -----
            val weekContent = week.shouldBeInstanceOf<TrendingWidgetUiModel.Content.Ranked>()
            weekContent.eyebrow shouldBe "Eyebrow Week"
            weekContent.items.map { it.bookId } shouldContainExactly listOf(1, 2)
            val yearContent = year.shouldBeInstanceOf<TrendingWidgetUiModel.Content.Ranked>()
            yearContent.eyebrow shouldBe "Eyebrow Year"
            yearContent.items.map { it.bookId } shouldContainExactly listOf(100, 200)
        }

        @Test
        fun `an upcoming feed lists its own books under its own eyebrow`() = runTest {
            // ----- Arrange -----
            val upcoming = TrendingFeed.Upcoming(window = UpcomingWindow.SixMonths)
            val flow = models(
                position = flowOf(
                    TrendingPosition(
                        feed = upcoming,
                        page = 0,
                    ),
                ),
                observeTrending = { feed ->
                    flowOf(if (feed == upcoming) listOf(book(7), book(8)) else listOf(book(1)))
                },
            )

            // ----- Act & Assert -----
            flow.test {
                val ranked = awaitItem().content.shouldBeInstanceOf<TrendingWidgetUiModel.Content.Ranked>()
                ranked.eyebrow shouldBe "Eyebrow UpcomingSixMonths"
                ranked.items.map { it.bookId } shouldContainExactly listOf(7, 8)
                awaitComplete()
            }
        }
    }

    @Nested
    inner class SettingsChanges {
        private fun changeTo(period: TrendingPeriod) = TrendingSettingsChange(
            appWidgetId = 1,
            settings = TrendingSettings(feed = TrendingFeed.Trending(period = period)),
        )

        private fun replayed(change: TrendingSettingsChange): MutableSharedFlow<TrendingSettingsChange> =
            MutableSharedFlow<TrendingSettingsChange>(replay = 1).also { it.tryEmit(change) }

        @Test
        fun `a settings change shows the new period as switching before the ranking`() = runTest {
            // ----- Arrange -----
            val flow = models(
                position = flowOf(
                    positionAt(
                        page = 0,
                        period = TrendingPeriod.Year,
                    ),
                ),
                settingsChanges = replayed(changeTo(TrendingPeriod.Year)),
                observeTrending = fixedBooks(listOf(book(1), book(2))),
            )

            // ----- Act & Assert -----
            flow.test {
                awaitItem().content shouldBe TrendingWidgetUiModel.Content.Switching(eyebrow = "Eyebrow Year")

                val ranked = awaitItem().content.shouldBeInstanceOf<TrendingWidgetUiModel.Content.Ranked>()
                ranked.eyebrow shouldBe "Eyebrow Year"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `switching stays until the position reaches the new period`() = runTest {
            // ----- Arrange -----
            val position = MutableStateFlow(
                positionAt(
                    page = 0,
                    period = TrendingPeriod.Week,
                ),
            )

            val flow = models(
                position = position,
                settingsChanges = replayed(changeTo(TrendingPeriod.Year)),
                observeTrending = fixedBooks(listOf(book(1), book(2))),
            )

            // ----- Act -----
            val contents = mutableListOf<TrendingWidgetUiModel.Content>()
            flow.test {
                contents += awaitItem().content
                expectNoEvents()
                position.value = positionAt(
                    page = 0,
                    period = TrendingPeriod.Year,
                )
                contents += awaitItem().content
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }

            // ----- Assert -----
            contents[0] shouldBe TrendingWidgetUiModel.Content.Switching(eyebrow = "Eyebrow Year")
            contents[1].shouldBeInstanceOf<TrendingWidgetUiModel.Content.Ranked>().eyebrow shouldBe "Eyebrow Year"
        }

        @Test
        fun `a page turn after a settings change does not show switching again`() = runTest {
            // ----- Arrange -----
            val position = MutableStateFlow(
                positionAt(
                    page = 0,
                    period = TrendingPeriod.Year,
                ),
            )

            val flow = models(
                position = position,
                settingsChanges = replayed(changeTo(TrendingPeriod.Year)),
                observeTrending = fixedBooks((1..6).map { book(it) }),
            )

            // ----- Act -----
            val contents = mutableListOf<TrendingWidgetUiModel.Content>()
            flow.test {
                contents += awaitItem().content
                contents += awaitItem().content
                position.value = positionAt(
                    page = 1,
                    period = TrendingPeriod.Year,
                )
                contents += awaitItem().content
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }

            // ----- Assert -----
            contents.map { it::class } shouldContainExactly listOf(
                TrendingWidgetUiModel.Content.Switching::class,
                TrendingWidgetUiModel.Content.Ranked::class,
                TrendingWidgetUiModel.Content.Ranked::class,
            )
        }

        @Test
        fun `a data refresh after a settings change does not show switching again`() = runTest {
            // ----- Arrange -----
            val books = MutableStateFlow(listOf(book(1), book(2)))

            val flow = models(
                position = flowOf(
                    positionAt(
                        page = 0,
                        period = TrendingPeriod.Year,
                    ),
                ),
                settingsChanges = replayed(changeTo(TrendingPeriod.Year)),
                observeTrending = { books },
            )

            // ----- Act -----
            val contents = mutableListOf<TrendingWidgetUiModel.Content>()
            flow.test {
                contents += awaitItem().content
                contents += awaitItem().content
                books.value = listOf(book(3), book(4))
                contents += awaitItem().content
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }

            // ----- Assert -----
            contents.map { it::class } shouldContainExactly listOf(
                TrendingWidgetUiModel.Content.Switching::class,
                TrendingWidgetUiModel.Content.Ranked::class,
                TrendingWidgetUiModel.Content.Ranked::class,
            )
        }

        @Test
        fun `a settings change while signed out shows no switching`() = runTest {
            // ----- Arrange -----
            val flow = models(
                isSignedIn = false,
                settingsChanges = replayed(changeTo(TrendingPeriod.Year)),
                observeTrending = fixedBooks(listOf(book(1))),
            )

            // ----- Act & Assert -----
            flow.test {
                awaitItem().content shouldBe TrendingWidgetUiModel.Content.SignedOut
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `switching gives way to the ranking for the current period when the position never catches up`() =
            runTest {
                // ----- Arrange -----
                val flow = models(
                    position = flowOf(positionAt(page = 0)),
                    settingsChanges = replayed(changeTo(TrendingPeriod.Year)),
                    observeTrending = fixedBooks(listOf(book(1), book(2))),
                )

                // ----- Act -----
                val contents = mutableListOf<TrendingWidgetUiModel.Content>()
                var quietBeforeTimeout = false
                flow.test {
                    contents += awaitItem().content
                    testScheduler.advanceTimeBy(4.seconds)
                    testScheduler.runCurrent()
                    expectNoEvents()
                    quietBeforeTimeout = true
                    testScheduler.advanceTimeBy(2.seconds)
                    testScheduler.runCurrent()
                    contents += awaitItem().content
                    cancelAndIgnoreRemainingEvents()
                }

                // ----- Assert -----
                quietBeforeTimeout shouldBe true
                contents[0] shouldBe TrendingWidgetUiModel.Content.Switching(eyebrow = "Eyebrow Year")
                contents[1].shouldBeInstanceOf<TrendingWidgetUiModel.Content.Ranked>().eyebrow shouldBe "Eyebrow Week"
            }
    }
}
