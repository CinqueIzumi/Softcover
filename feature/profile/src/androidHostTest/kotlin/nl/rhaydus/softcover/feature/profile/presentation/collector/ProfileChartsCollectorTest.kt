package nl.rhaydus.softcover.feature.profile.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Gender
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetHideUntaggedAuthorsAsFlowUseCase
import nl.rhaydus.softcover.core.profile.domain.model.AuthorDemographics
import nl.rhaydus.softcover.core.profile.domain.model.DemographicBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenderSlice
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenreSlice
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import nl.rhaydus.softcover.core.profile.domain.model.ReadingLife
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingLifeUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveUserProfileDataUseCase
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.demographicChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genderChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genreRankingUiModel
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProfileChartsCollectorTest {
    private lateinit var observeReadingLifeUseCase: ObserveReadingLifeUseCase
    private lateinit var observeUserProfileDataUseCase: ObserveUserProfileDataUseCase
    private lateinit var getHideUntaggedAuthorsAsFlowUseCase: GetHideUntaggedAuthorsAsFlowUseCase
    private lateinit var stateFlow: MutableStateFlow<ProfileUiState>
    private lateinit var scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>
    private lateinit var readingLifeFlow: MutableSharedFlow<ReadingLife?>
    private lateinit var profileDataFlow: MutableSharedFlow<UserProfileData?>
    private lateinit var hideFlow: MutableSharedFlow<Boolean>

    @BeforeEach
    fun setUp() {
        readingLifeFlow = MutableSharedFlow()
        profileDataFlow = MutableSharedFlow()
        hideFlow = MutableSharedFlow()
        observeReadingLifeUseCase = mockk()
        observeUserProfileDataUseCase = mockk()
        getHideUntaggedAuthorsAsFlowUseCase = mockk()
        stateFlow = MutableStateFlow(ProfileUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalProfileVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            observeReadingLifeUseCase()
        } returns readingLifeFlow

        every {
            observeUserProfileDataUseCase()
        } returns profileDataFlow

        every {
            getHideUntaggedAuthorsAsFlowUseCase()
        } returns hideFlow
    }

    private fun stubDependencies(testScope: TestScope): ProfileDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<ProfileDependencies>(relaxed = true).also { mock ->
            every {
                mock.observeReadingLifeUseCase
            } returns observeReadingLifeUseCase

            every {
                mock.observeUserProfileDataUseCase
            } returns observeUserProfileDataUseCase

            every {
                mock.getHideUntaggedAuthorsAsFlowUseCase
            } returns getHideUntaggedAuthorsAsFlowUseCase

            every {
                mock.coroutineScope
            } returns testScope

            every {
                mock.mainDispatcher
            } returns dispatcher
        }
    }

    private fun buildDemographics(): AuthorDemographics = AuthorDemographics(
        genderSlices = listOf(
            GenderSlice(
                gender = Gender.Female,
                count = 6,
                fraction = 0.6,
            ),
            GenderSlice(
                gender = Gender.Unknown,
                count = 4,
                fraction = 0.4,
            ),
        ),
        knownGenderCount = 6,
        unknownGenderCount = 4,
        bipocBreakdown = DemographicBreakdown(
            yesCount = 2,
            noCount = 2,
            unknownCount = 6,
        ),
        lgbtqBreakdown = DemographicBreakdown(
            yesCount = 1,
            noCount = 3,
            unknownCount = 6,
        ),
    )

    private fun buildGenres(): GenreBreakdown = GenreBreakdown(
        slices = listOf(
            GenreSlice(
                name = "Fantasy",
                count = 3,
                fraction = 0.75,
            ),
            GenreSlice(
                name = "Poetry",
                count = 1,
                fraction = 0.25,
            ),
        ),
        taggedBookCount = 4,
    )

    private fun buildReadingLife(
        demographics: AuthorDemographics = buildDemographics(),
        genres: GenreBreakdown = buildGenres(),
    ): ReadingLife = ReadingLife(
        booksByYear = emptyList(),
        pagesByYear = emptyList(),
        pagesByMonth = emptyList(),
        genres = genres,
        ratings = RatingsDistribution(),
        recentlyLoved = emptyList(),
        authorDemographics = demographics,
    )

    private fun buildProfileData(): UserProfileData = UserProfileData(
        profileImageUrl = "https://example.com/avatar.png",
        name = "Jane Doe",
        username = "cinque",
        bio = "Avid reader",
        booksRead = 42,
        totalPagesRead = 12000,
        averageRating = 4.2,
        readingStreak = 7,
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `one write sets the genre ranking, the charts and the demographics together`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val demographics = buildDemographics()
                val genres = buildGenres()
                val dependencies = stubDependencies(this)
                val job = launch {
                    ProfileChartsCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                hideFlow.emit(false)
                profileDataFlow.emit(buildProfileData())
                readingLifeFlow.emit(
                    buildReadingLife(
                        demographics = demographics,
                        genres = genres,
                    ),
                )

                // ----- Assert -----
                stateFlow.value.genreRanking shouldBe genreRankingUiModel(genres)
                stateFlow.value.genderChart shouldBe genderChartUiModel(demographics.genderSlices)
                stateFlow.value.bipocChart shouldBe demographicChartUiModel(demographics.bipocBreakdown)
                stateFlow.value.lgbtqChart shouldBe demographicChartUiModel(demographics.lgbtqBreakdown)
                stateFlow.value.authorDemographics shouldBe demographics
                job.cancel()
            }

        @Test
        fun `stays loading when only the reading life has arrived`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val initial = stateFlow.value
                val dependencies = stubDependencies(this)
                val job = launch {
                    ProfileChartsCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                hideFlow.emit(false)
                readingLifeFlow.emit(buildReadingLife())

                // ----- Assert -----
                stateFlow.value.genreRanking shouldBe initial.genreRanking
                stateFlow.value.genderChart shouldBe initial.genderChart
                stateFlow.value.bipocChart shouldBe initial.bipocChart
                stateFlow.value.lgbtqChart shouldBe initial.lgbtqChart
                stateFlow.value.authorDemographics shouldBe AuthorDemographics()
                job.cancel()
            }

        @Test
        fun `stays loading when only the profile data has arrived`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val initial = stateFlow.value
                val dependencies = stubDependencies(this)
                val job = launch {
                    ProfileChartsCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                hideFlow.emit(false)
                profileDataFlow.emit(buildProfileData())

                // ----- Assert -----
                stateFlow.value.genreRanking shouldBe initial.genreRanking
                stateFlow.value.genderChart shouldBe initial.genderChart
                stateFlow.value.bipocChart shouldBe initial.bipocChart
                stateFlow.value.lgbtqChart shouldBe initial.lgbtqChart
                stateFlow.value.authorDemographics shouldBe AuthorDemographics()
                job.cancel()
            }

        @Test
        fun `stays loading when the reading life is null`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val initial = stateFlow.value
                val dependencies = stubDependencies(this)
                val job = launch {
                    ProfileChartsCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                hideFlow.emit(false)
                profileDataFlow.emit(buildProfileData())
                readingLifeFlow.emit(null)

                // ----- Assert -----
                stateFlow.value.genreRanking shouldBe initial.genreRanking
                stateFlow.value.genderChart shouldBe initial.genderChart
                job.cancel()
            }

        @Test
        fun `writes the tagged-only charts and demographics when untagged authors are hidden`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val demographics = buildDemographics()
                val tagged = demographics.excludingUntaggedAuthors()
                val dependencies = stubDependencies(this)
                val job = launch {
                    ProfileChartsCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                hideFlow.emit(true)
                profileDataFlow.emit(buildProfileData())
                readingLifeFlow.emit(buildReadingLife(demographics = demographics))

                // ----- Assert -----
                stateFlow.value.genderChart shouldBe genderChartUiModel(tagged.genderSlices)
                stateFlow.value.bipocChart shouldBe demographicChartUiModel(tagged.bipocBreakdown)
                stateFlow.value.lgbtqChart shouldBe demographicChartUiModel(tagged.lgbtqBreakdown)
                stateFlow.value.authorDemographics shouldBe tagged
                job.cancel()
            }

        @Test
        fun `does not exclude untaggeds from the genre ranking when untagged authors are hidden`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val genres = buildGenres()
                val dependencies = stubDependencies(this)
                val job = launch {
                    ProfileChartsCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                hideFlow.emit(true)
                profileDataFlow.emit(buildProfileData())
                readingLifeFlow.emit(buildReadingLife(genres = genres))

                // ----- Assert -----
                stateFlow.value.genreRanking shouldBe genreRankingUiModel(genres)
                job.cancel()
            }

        @Test
        fun `rebuilds the charts and demographics when the hide flag flips on`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val demographics = buildDemographics()
                val tagged = demographics.excludingUntaggedAuthors()
                val dependencies = stubDependencies(this)
                val job = launch {
                    ProfileChartsCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }
                hideFlow.emit(false)
                profileDataFlow.emit(buildProfileData())
                readingLifeFlow.emit(buildReadingLife(demographics = demographics))

                // ----- Act -----
                hideFlow.emit(true)

                // ----- Assert -----
                stateFlow.value.genderChart shouldBe genderChartUiModel(tagged.genderSlices)
                stateFlow.value.bipocChart shouldBe demographicChartUiModel(tagged.bipocBreakdown)
                stateFlow.value.authorDemographics shouldBe tagged
                job.cancel()
            }

        @Test
        fun `restores the full charts and demographics when the hide flag flips back off`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val demographics = buildDemographics()
                val dependencies = stubDependencies(this)
                val job = launch {
                    ProfileChartsCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }
                hideFlow.emit(true)
                profileDataFlow.emit(buildProfileData())
                readingLifeFlow.emit(buildReadingLife(demographics = demographics))

                // ----- Act -----
                hideFlow.emit(false)

                // ----- Assert -----
                stateFlow.value.genderChart shouldBe genderChartUiModel(demographics.genderSlices)
                stateFlow.value.bipocChart shouldBe demographicChartUiModel(demographics.bipocBreakdown)
                stateFlow.value.authorDemographics shouldBe demographics
                job.cancel()
            }

        @Test
        fun `rebuilds everything when a new reading life arrives`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val secondDemographics = AuthorDemographics(
                    bipocBreakdown = DemographicBreakdown(
                        yesCount = 5,
                        noCount = 0,
                    ),
                )
                val secondGenres = GenreBreakdown(
                    slices = listOf(
                        GenreSlice(
                            name = "Horror",
                            count = 1,
                            fraction = 1.0,
                        ),
                    ),
                )
                val dependencies = stubDependencies(this)
                val job = launch {
                    ProfileChartsCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }
                hideFlow.emit(false)
                profileDataFlow.emit(buildProfileData())
                readingLifeFlow.emit(buildReadingLife())

                // ----- Act -----
                readingLifeFlow.emit(
                    buildReadingLife(
                        demographics = secondDemographics,
                        genres = secondGenres,
                    ),
                )

                // ----- Assert -----
                stateFlow.value.genreRanking shouldBe genreRankingUiModel(secondGenres)
                stateFlow.value.bipocChart shouldBe demographicChartUiModel(secondDemographics.bipocBreakdown)
                stateFlow.value.authorDemographics shouldBe secondDemographics
                job.cancel()
            }
    }
}
