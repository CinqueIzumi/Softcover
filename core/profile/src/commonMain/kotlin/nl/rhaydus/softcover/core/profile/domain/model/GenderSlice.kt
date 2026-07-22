package nl.rhaydus.softcover.core.profile.domain.model

import nl.rhaydus.softcover.core.domain.model.Gender

/**
 * One gender bucket among the user's distinct authors, including the [Gender.Unknown] bucket as its
 * own slice pinned last. [fraction] is the share of all distinct authors, so the slices sum to 1;
 * [AuthorDemographics.knownGenderCount] / [AuthorDemographics.unknownGenderCount] give the split.
 */
data class GenderSlice(
    val gender: Gender,
    val count: Int,
    val fraction: Double,
)
