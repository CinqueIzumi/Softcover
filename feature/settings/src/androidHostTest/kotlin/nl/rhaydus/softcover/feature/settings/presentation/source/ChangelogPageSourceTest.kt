package nl.rhaydus.softcover.feature.settings.presentation.source

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.app.AppVersionInfo
import nl.rhaydus.softcover.core.domain.app.AppVersionProvider
import nl.rhaydus.softcover.core.uibinding.date.formatLongDate
import nl.rhaydus.softcover.feature.settings.domain.model.Changelog
import nl.rhaydus.softcover.feature.settings.domain.model.ChangelogEntry
import nl.rhaydus.softcover.feature.settings.domain.model.MarkdownBlock
import nl.rhaydus.softcover.feature.settings.domain.model.MarkdownSpan
import nl.rhaydus.softcover.feature.settings.domain.model.PublishedDocumentSource
import nl.rhaydus.softcover.feature.settings.domain.usecase.ObserveChangelogUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.RefreshChangelogUseCase
import nl.rhaydus.softcover.feature.settings.presentation.mapper.toMarkdownDocumentUiModel
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private const val INSTALLED_VERSION = "3.2.0"

private val TEST_ENTRY = ChangelogEntry(
    version = "3.1.0",
    headline = "Headline",
    releaseDate = LocalDate(
        year = 2026,
        monthNumber = 3,
        dayOfMonth = 5,
    ),
    blocks = persistentListOf(MarkdownBlock.Paragraph(spans = persistentListOf(MarkdownSpan(text = "Body")))),
)

private val TEST_CHANGELOG = Changelog(
    entries = persistentListOf(TEST_ENTRY),
    fetchedAtEpochMillis = 1_000L,
    source = PublishedDocumentSource.CACHE,
)

class ChangelogPageSourceTest {
    private lateinit var observeChangelogUseCase: ObserveChangelogUseCase
    private lateinit var refreshChangelogUseCase: RefreshChangelogUseCase
    private lateinit var appVersionProvider: AppVersionProvider
    private lateinit var source: ChangelogPageSource

    @BeforeEach
    fun setUp() {
        observeChangelogUseCase = mockk()
        refreshChangelogUseCase = mockk()
        appVersionProvider = mockk()

        every {
            appVersionProvider.versionInfo
        } returns AppVersionInfo(
            name = INSTALLED_VERSION,
            code = 1,
        )

        source = ChangelogPageSource(
            observeChangelogUseCase = observeChangelogUseCase,
            refreshChangelogUseCase = refreshChangelogUseCase,
            appVersionProvider = appVersionProvider,
        )
    }

    private suspend fun observeSections(vararg entries: ChangelogEntry) = run {
        every {
            observeChangelogUseCase()
        } returns flowOf(TEST_CHANGELOG.copy(entries = persistentListOf(*entries)))

        source.observe().first().sections
    }

    @Nested
    inner class Observe {
        @Test
        fun `maps each entry to its own section in order`() = runTest {
            // ----- Arrange -----
            val older = TEST_ENTRY.copy(version = "3.0.0")

            // ----- Act -----
            val sections = observeSections(
                TEST_ENTRY,
                older,
            )

            // ----- Assert -----
            sections.map { it.header.shouldNotBeNull().eyebrow } shouldBe listOf("3.1.0", "3.0.0")
        }

        @Test
        fun `an empty changelog yields no sections`() = runTest {
            // ----- Arrange -----
            val entries = emptyArray<ChangelogEntry>()

            // ----- Act -----
            val sections = observeSections(*entries)

            // ----- Assert -----
            sections shouldBe emptyList()
        }

        @Test
        fun `eyebrow is the bare version when it is not the installed release`() = runTest {
            // ----- Arrange -----
            val entry = TEST_ENTRY.copy(version = "3.1.0")

            // ----- Act -----
            val section = observeSections(entry).single()

            // ----- Assert -----
            section.header.shouldNotBeNull().eyebrow shouldBe "3.1.0"
        }

        @Test
        fun `eyebrow marks the entry as the installed version when the release matches`() = runTest {
            // ----- Arrange -----
            val entry = TEST_ENTRY.copy(version = "3.2.0")

            // ----- Act -----
            val section = observeSections(entry).single()

            // ----- Assert -----
            section.header.shouldNotBeNull().eyebrow shouldBe "3.2.0 · Your version"
        }

        @Test
        fun `eyebrow still marks the installed version when it carries a prefix or pre-release suffix`() = runTest {
            // ----- Arrange -----
            every {
                appVersionProvider.versionInfo
            } returns AppVersionInfo(
                name = "v3.2.0-rc1",
                code = 1,
            )
            val entry = TEST_ENTRY.copy(version = "3.2")

            // ----- Act -----
            val section = observeSections(entry).single()

            // ----- Assert -----
            section.header.shouldNotBeNull().eyebrow shouldBe "3.2 · Your version"
        }

        @Test
        fun `headline is carried through to the header`() = runTest {
            // ----- Arrange -----
            val entry = TEST_ENTRY.copy(headline = "A new shelf")

            // ----- Act -----
            val section = observeSections(entry).single()

            // ----- Assert -----
            section.header.shouldNotBeNull().headlineText shouldBe "A new shelf"
        }

        @Test
        fun `dateText is the formatted long date when a release date is present`() = runTest {
            // ----- Arrange -----
            val entry = TEST_ENTRY

            // ----- Act -----
            val section = observeSections(entry).single()

            // ----- Assert -----
            section.dateText shouldBe entry.releaseDate?.formatLongDate()
        }

        @Test
        fun `dateText is null when the release has no date`() = runTest {
            // ----- Arrange -----
            val entry = TEST_ENTRY.copy(releaseDate = null)

            // ----- Act -----
            val section = observeSections(entry).single()

            // ----- Assert -----
            section.dateText shouldBe null
        }

        @Test
        fun `body is the blocks mapped to a markdown document`() = runTest {
            // ----- Arrange -----
            val entry = TEST_ENTRY

            // ----- Act -----
            val section = observeSections(entry).single()

            // ----- Assert -----
            section.body shouldBe entry.blocks.toMarkdownDocumentUiModel()
        }

        @Test
        fun `carries the bundled flag and fetch time through`() = runTest {
            // ----- Arrange -----
            every {
                observeChangelogUseCase()
            } returns flowOf(
                TEST_CHANGELOG.copy(
                    source = PublishedDocumentSource.BUNDLED,
                    fetchedAtEpochMillis = null,
                ),
                TEST_CHANGELOG,
            )

            // ----- Act -----
            val snapshots = mutableListOf<Pair<Boolean, Long?>>()
            source.observe().collect { snapshots.add(it.isBundled to it.fetchedAtEpochMillis) }

            // ----- Assert -----
            snapshots shouldBe listOf(true to null, false to 1_000L)
        }
    }
}
