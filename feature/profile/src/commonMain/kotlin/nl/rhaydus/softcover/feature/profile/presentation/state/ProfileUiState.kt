package nl.rhaydus.softcover.feature.profile.presentation.state

import nl.rhaydus.softcover.core.profile.domain.model.AuthorDemographics
import nl.rhaydus.softcover.core.profile.domain.model.ReadingLife
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.toad.UiState

internal data class ProfileUiState(
    val userProfileData: UserProfileData? = null,
    val readingLife: ReadingLife? = null,
    val hideUntaggedAuthors: Boolean = false,
    val isLoading: Boolean = true,
) : UiState {
    private val allAuthorDemographics: AuthorDemographics
        get() = readingLife?.authorDemographics ?: AuthorDemographics()

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
