package nl.rhaydus.softcover.feature.settings.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.app.AppVersionInfo
import nl.rhaydus.softcover.core.domain.app.AppVersionProvider
import nl.rhaydus.softcover.feature.settings.domain.model.Changelog
import nl.rhaydus.softcover.feature.settings.domain.model.ChangelogEntry
import nl.rhaydus.softcover.feature.settings.domain.model.PublishedDocumentSource
import nl.rhaydus.softcover.feature.settings.domain.repository.ChangelogRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ObserveChangelogUseCaseTest {
    private lateinit var changelogRepository: ChangelogRepository
    private lateinit var appVersionProvider: AppVersionProvider
    private lateinit var useCase: ObserveChangelogUseCase

    @BeforeEach
    fun setUp() {
        changelogRepository = mockk()
        appVersionProvider = mockk()
        useCase = ObserveChangelogUseCase(
            changelogRepository = changelogRepository,
            appVersionProvider = appVersionProvider,
        )
    }

    private fun entry(version: String) = ChangelogEntry(
        version = version,
        headline = "Headline $version",
        releaseDate = null,
        blocks = persistentListOf(),
    )

    private fun changelog(
        vararg versions: String,
        fetchedAtEpochMillis: Long? = 1_000L,
        source: PublishedDocumentSource = PublishedDocumentSource.CACHE,
    ) = Changelog(
        entries = versions.map(::entry).toImmutableList(),
        fetchedAtEpochMillis = fetchedAtEpochMillis,
        source = source,
    )

    private fun installed(name: String) {
        every {
            appVersionProvider.versionInfo
        } returns AppVersionInfo(
            name = name,
            code = 1,
        )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `hides entries newer than the installed version`() = runTest {
            // ----- Arrange -----
            installed("3.2.0")
            every {
                changelogRepository.observeChangelog()
            } returns flowOf(
                changelog(
                    "3.3.0",
                    "3.2.1",
                    "3.2.0",
                ),
            )

            // ----- Act & Assert -----
            useCase().test {
                awaitItem().entries.map { it.version } shouldBe listOf("3.2.0")
                awaitComplete()
            }
        }

        @Test
        fun `keeps entries equal to and older than the installed version in order`() = runTest {
            // ----- Arrange -----
            installed("3.2.0")
            every {
                changelogRepository.observeChangelog()
            } returns flowOf(
                changelog(
                    "3.2.0",
                    "3.1.0",
                    "2.9.5",
                ),
            )

            // ----- Act & Assert -----
            useCase().test {
                awaitItem().entries.map { it.version } shouldBe listOf("3.2.0", "3.1.0", "2.9.5")
                awaitComplete()
            }
        }

        @Test
        fun `treats an rc installed version as the release it precedes`() = runTest {
            // ----- Arrange -----
            installed("3.2.0-rc1")
            every {
                changelogRepository.observeChangelog()
            } returns flowOf(
                changelog(
                    "3.3.0",
                    "3.2.0",
                    "3.1.0",
                ),
            )

            // ----- Act & Assert -----
            useCase().test {
                awaitItem().entries.map { it.version } shouldBe listOf("3.2.0", "3.1.0")
                awaitComplete()
            }
        }

        @Test
        fun `ignores a build suffix on the installed version`() = runTest {
            // ----- Arrange -----
            installed("3.2.0+45")
            every {
                changelogRepository.observeChangelog()
            } returns flowOf(
                changelog(
                    "3.2.1",
                    "3.2.0",
                ),
            )

            // ----- Act & Assert -----
            useCase().test {
                awaitItem().entries.map { it.version } shouldBe listOf("3.2.0")
                awaitComplete()
            }
        }

        @Test
        fun `yields no entries when every release is newer than the installed version`() = runTest {
            // ----- Arrange -----
            installed("1.0.0")
            every {
                changelogRepository.observeChangelog()
            } returns flowOf(
                changelog(
                    "1.1.0",
                    "2.0.0",
                ),
            )

            // ----- Act & Assert -----
            useCase().test {
                awaitItem().entries.shouldBeEmpty()
                awaitComplete()
            }
        }

        @Test
        fun `keeps the fetch time and source`() = runTest {
            // ----- Arrange -----
            installed("3.2.0")
            every {
                changelogRepository.observeChangelog()
            } returns flowOf(
                changelog(
                    "3.3.0",
                    fetchedAtEpochMillis = 7_000L,
                    source = PublishedDocumentSource.BUNDLED,
                ),
            )

            // ----- Act & Assert -----
            useCase().test {
                val result = awaitItem()

                result.fetchedAtEpochMillis shouldBe 7_000L
                result.source shouldBe PublishedDocumentSource.BUNDLED
                awaitComplete()
            }
        }
    }
}
