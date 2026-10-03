package nl.rhaydus.softcover.feature.profile.presentation.state

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.share.ReadingLifeShareCardUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.component.topbar.EditorialTopBarUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.profile.domain.model.AuthorDemographics
import nl.rhaydus.softcover.core.profile.domain.model.RatingBand
import nl.rhaydus.softcover.core.profile.domain.model.ReadingLife
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.presentation.mapper.toRatingsHistogramCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.toRatingsHistogramHeader
import nl.rhaydus.toad.UiState

internal data class ProfileUiState(
    val userProfileData: UserProfileData? = null,
    val readingLife: ReadingLife? = null,
    val readingLifeShareCard: ReadingLifeShareCardUiModel? = null,
    val recentlyLovedCards: ImmutableList<BookCardUiModel> = persistentListOf(),
    val hideUntaggedAuthors: Boolean = false,
    val isLoading: Boolean = true,

    val readingAtlasHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(eyebrow = "Reading atlas"),
    val theReaderHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(eyebrow = "The reader"),
    val authorRepresentationHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Who you read",
        headline = "The authors behind your shelf",
    ),
    val genreRankingHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "What you reach for",
        headline = "The genres you read most",
    ),
    val recentlyLovedHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Recently loved",
        headline = "The last few that landed",
    ),
    val yearColumnHistoryHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "The shape of the years",
        headline = "Reading, year by year",
    ),
    val ratingsHistogramHeader: SectionHeaderUiModel = RatingBand.NEW_READER.toRatingsHistogramHeader(),
    val ratingsHistogramCaption: String = RatingBand.NEW_READER.toRatingsHistogramCaption(),
    val shareSheetScaffold: SheetScaffoldUiModel = profileShareSheetScaffold(),
    val logOutSheetScaffold: SheetScaffoldUiModel = logOutSheetScaffold(),

    /** Both bars carry no title — the page's own masthead names it. */
    val topBar: TopBarUiModel = TopBarUiModel(),
    val editorialTopBar: EditorialTopBarUiModel = EditorialTopBarUiModel(),
) : UiState {
    private val allAuthorDemographics: AuthorDemographics
        get() = readingLife?.authorDemographics ?: AuthorDemographics()

    /**
     * Whether the reader has any loved reads to show. Read from [readingLife] rather than the mapped
     * [recentlyLovedCards], which land a frame later and would flash the empty copy.
     */
    val hasRecentlyLoved: Boolean
        get() = readingLife?.recentlyLoved.orEmpty().isNotEmpty()

    /**
     * The author breakdown as the section should render it: every author by default, or - once
     * [hideUntaggedAuthors] is on - only the ones tagged for each attribute, with the percentages
     * renormalised per bar. Derived here rather than in the render so the two layouts (mobile and
     * desktop) can't disagree about which reading they show.
     */
    val authorDemographics: AuthorDemographics
        get() = if (hideUntaggedAuthors) {
            allAuthorDemographics.excludingUntaggedAuthors()
        } else {
            allAuthorDemographics
        }

    /**
     * Whether any of the three breakdowns actually has an untagged bucket to drop. Reads the
     * *unfiltered* data on purpose: [authorDemographics] has already zeroed those buckets while the
     * switch is on, so deriving it from that would make the switch disappear the moment it was used.
     */
    val hasUntaggedAuthors: Boolean
        get() = with(allAuthorDemographics) {
            unknownGenderCount > 0 ||
                bipocBreakdown.unknownCount > 0 ||
                lgbtqBreakdown.unknownCount > 0
        }
}
