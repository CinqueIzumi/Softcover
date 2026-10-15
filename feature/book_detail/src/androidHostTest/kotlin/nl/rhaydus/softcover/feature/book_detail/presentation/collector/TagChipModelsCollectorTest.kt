package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.chip.ChipInteraction
import nl.rhaydus.softcover.core.component.chip.ChipLeading
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipTrailing
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TagChipModelsCollectorTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        dependencies = mockk<BookDetailDependencies>(relaxed = true)
    }

    private fun stubBook(tags: List<Tag>): Book = mockk {
        every {
            this@mockk.tags
        } returns tags
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `maps userTags to read-only chips keyed by category and name`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val userTags = persistentListOf(
                UserTag(
                    name = "Fantasy",
                    category = TagCategory.GENRE,
                ),
                UserTag(
                    name = "Cozy",
                    category = TagCategory.MOOD,
                ),
            )
            val collector = TagChipModelsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(userTags = userTags.toImmutableList())

            // ----- Assert -----
            stateFlow.value.userTagChips.map { it.key } shouldBe listOf("GENRE:Fantasy", "MOOD:Cozy")
            stateFlow.value.userTagChips.map { it.label } shouldBe listOf("Fantasy", "Cozy")
            stateFlow.value.userTagChips.all { it.interaction == ChipInteraction.Inert } shouldBe true
            job.cancel()
        }

        @Test
        fun `groups community tags into Genre, Mood, Other tags, Content warning in that order`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val tags = listOf(
                    Tag(
                        id = 1,
                        name = "Mystery",
                        category = TagCategory.CONTENT_WARNING,
                    ),
                    Tag(
                        id = 2,
                        name = "Sci-fi",
                        category = TagCategory.GENRE,
                    ),
                    Tag(
                        id = 3,
                        name = "Dark",
                        category = TagCategory.MOOD,
                    ),
                    Tag(
                        id = 4,
                        name = "Misc",
                        category = TagCategory.TAG,
                    ),
                    Tag(
                        id = 5,
                        name = "Other",
                        category = TagCategory.OTHER,
                    ),
                )
                val book = stubBook(tags)
                val collector = TagChipModelsCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(book = book)

                // ----- Assert -----
                stateFlow.value.communityTagGroups.map { it.label } shouldBe listOf(
                    "Genre",
                    "Mood",
                    "Other tags",
                    "Content warning",
                )
                job.cancel()
            }

        @Test
        fun `TAG tags form an Other tags group of plain chips capped at 5 by count descending`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val tags = (1..6).map { i ->
                    Tag(
                        id = i,
                        name = "tag-$i",
                        category = TagCategory.TAG,
                        count = i * 10,
                    )
                }
                val book = stubBook(tags)
                val collector = TagChipModelsCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(book = book)

                // ----- Assert -----
                val group = stateFlow.value.communityTagGroups.single { it.label == "Other tags" }
                group.chips.map { it.label } shouldBe listOf("tag-6", "tag-5", "tag-4", "tag-3", "tag-2")
                group.chips.all { it.tone == ChipTone.Tonal } shouldBe true
                group.chips.all { it.interaction == ChipInteraction.Inert } shouldBe true
                job.cancel()
            }

        @Test
        fun `caps each category at the top 5 tags by popularity`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val tags = (1..6).map { i ->
                Tag(
                    id = i,
                    name = "genre-$i",
                    category = TagCategory.GENRE,
                    count = i * 10,
                )
            }
            val book = stubBook(tags)
            val collector = TagChipModelsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = book)

            // ----- Assert -----
            val genreGroup = stateFlow.value.communityTagGroups.single { it.label == "Genre" }
            genreGroup.chips.size shouldBe 5
            genreGroup.chips.map { it.label } shouldBe listOf(
                "genre-6",
                "genre-5",
                "genre-4",
                "genre-3",
                "genre-2",
            )
            job.cancel()
        }

        @Test
        fun `content warning chips use the Spoiler variant while other categories use Tonal`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val tags = listOf(
                    Tag(
                        id = 1,
                        name = "Death",
                        category = TagCategory.CONTENT_WARNING,
                    ),
                    Tag(
                        id = 2,
                        name = "Adventure",
                        category = TagCategory.GENRE,
                    ),
                    Tag(
                        id = 3,
                        name = "Tense",
                        category = TagCategory.MOOD,
                    ),
                )
                val book = stubBook(tags)
                val collector = TagChipModelsCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(book = book)

                // ----- Assert -----
                val groups = stateFlow.value.communityTagGroups
                groups.single { it.label == "Content warning" }
                    .chips.all { it.tone == ChipTone.Spoiler } shouldBe true
                groups.single { it.label == "Genre" }
                    .chips.none { it.tone == ChipTone.Spoiler } shouldBe true
                groups.single { it.label == "Mood" }
                    .chips.none { it.tone == ChipTone.Spoiler } shouldBe true
                job.cancel()
            }

        @Test
        fun `community chips are inert except a concealed content-warning chip`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val tags = listOf(
                Tag(
                    id = 1,
                    name = "Adventure",
                    category = TagCategory.GENRE,
                ),
                Tag(
                    id = 2,
                    name = "Death",
                    category = TagCategory.CONTENT_WARNING,
                ),
            )
            val book = stubBook(tags)
            val collector = TagChipModelsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = book)

            // ----- Assert -----
            val groups = stateFlow.value.communityTagGroups
            groups.single { it.label == "Genre" }
                .chips.all { it.interaction == ChipInteraction.Inert } shouldBe true
            groups.single { it.label == "Content warning" }
                .chips.all { it.interaction == ChipInteraction.Clickable } shouldBe true
            job.cancel()
        }

        @Test
        fun `a revealed content-warning chip is Tonal and Inert`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val tags = listOf(
                Tag(
                    id = 1,
                    name = "Death",
                    category = TagCategory.CONTENT_WARNING,
                ),
            )
            val book = stubBook(tags)
            val collector = TagChipModelsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                book = book,
                revealedTagKeys = persistentSetOf("1"),
            )

            // ----- Assert -----
            val chip = stateFlow.value.communityTagGroups.single().chips.single()
            chip.tone shouldBe ChipTone.Tonal
            chip.interaction shouldBe ChipInteraction.Inert
            job.cancel()
        }

        @Test
        fun `revealing one content-warning chip does not reveal another`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val tags = listOf(
                Tag(
                    id = 1,
                    name = "Death",
                    category = TagCategory.CONTENT_WARNING,
                ),
                Tag(
                    id = 2,
                    name = "Violence",
                    category = TagCategory.CONTENT_WARNING,
                ),
            )
            val book = stubBook(tags)
            val collector = TagChipModelsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                book = book,
                revealedTagKeys = persistentSetOf("1"),
            )

            // ----- Assert -----
            val chips = stateFlow.value.communityTagGroups.single().chips.associateBy { it.key }
            chips.getValue("1").tone shouldBe ChipTone.Tonal
            chips.getValue("2").tone shouldBe ChipTone.Spoiler
            job.cancel()
        }

        @Test
        fun `tagEditorOpenerChip reads Add tags and Dashed when the user has no tags`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val collector = TagChipModelsCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(userTags = persistentListOf())

                // ----- Assert -----
                stateFlow.value.tagEditorOpenerChip?.label shouldBe "Add tags"
                job.cancel()
            }

        @Test
        fun `tagEditorOpenerChip reads Edit tags once the user has at least one tag`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val userTags = persistentListOf(
                    UserTag(
                        name = "Fantasy",
                        category = TagCategory.GENRE,
                    ),
                )
                val collector = TagChipModelsCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(userTags = userTags.toImmutableList())

                // ----- Assert -----
                stateFlow.value.tagEditorOpenerChip?.label shouldBe "Edit tags"
                job.cancel()
            }

        @Test
        fun `userTagEditorGroups follow EDITABLE_CATEGORIES order with a trailing Other group`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val userTags = persistentListOf(
                    UserTag(
                        name = "Zesty",
                        category = TagCategory.OTHER,
                    ),
                    UserTag(
                        name = "Grim",
                        category = TagCategory.CONTENT_WARNING,
                    ),
                    UserTag(
                        name = "Cozy",
                        category = TagCategory.MOOD,
                    ),
                    UserTag(
                        name = "Fantasy",
                        category = TagCategory.GENRE,
                    ),
                )
                val collector = TagChipModelsCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(userTags = userTags.toImmutableList())

                // ----- Assert -----
                stateFlow.value.userTagEditorGroups.map { it.label } shouldBe listOf(
                    TagCategory.GENRE.label,
                    TagCategory.MOOD.label,
                    TagCategory.CONTENT_WARNING.label,
                    TagCategory.OTHER.label,
                )
                job.cancel()
            }

        @Test
        fun `userTagEditorGroups omits editable categories with no tags and carries no Other group when empty`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val userTags = persistentListOf(
                    UserTag(
                        name = "Fantasy",
                        category = TagCategory.GENRE,
                    ),
                )
                val collector = TagChipModelsCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(userTags = userTags.toImmutableList())

                // ----- Assert -----
                stateFlow.value.userTagEditorGroups.map { it.label } shouldBe listOf(TagCategory.GENRE.label)
                job.cancel()
            }

        @Test
        fun `userTagEditorGroups chips carry a SpoilerToggle leading and a Remove Dismiss trailing`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val userTags = persistentListOf(
                    UserTag(
                        name = "Fantasy",
                        category = TagCategory.GENRE,
                        spoiler = false,
                    ),
                    UserTag(
                        name = "Death",
                        category = TagCategory.CONTENT_WARNING,
                        spoiler = true,
                    ),
                )
                val collector = TagChipModelsCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(userTags = userTags.toImmutableList())

                // ----- Assert -----
                val chips = stateFlow.value.userTagEditorGroups.flatMap { it.chips }.associateBy { it.label }
                chips.getValue("Fantasy").leading.shouldBeInstanceOf<ChipLeading.SpoilerToggle>().marked shouldBe false
                chips.getValue("Fantasy").trailing shouldBe ChipTrailing.Dismiss(label = "Remove Fantasy")
                chips.getValue("Death").leading.shouldBeInstanceOf<ChipLeading.SpoilerToggle>().marked shouldBe true
                chips.getValue("Death").trailing shouldBe ChipTrailing.Dismiss(label = "Remove Death")
                job.cancel()
            }

        @Test
        fun `userTagByEditorChipKey resolves every editor chip key back to its UserTag`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val fantasy = UserTag(
                    name = "Fantasy",
                    category = TagCategory.GENRE,
                )
                val cozy = UserTag(
                    name = "Cozy",
                    category = TagCategory.MOOD,
                )
                val userTags = persistentListOf(fantasy, cozy)
                val collector = TagChipModelsCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(userTags = userTags.toImmutableList())

                // ----- Assert -----
                stateFlow.value.userTagByEditorChipKey shouldBe mapOf(
                    "GENRE:Fantasy" to fantasy,
                    "MOOD:Cozy" to cozy,
                )
                job.cancel()
            }
    }
}
