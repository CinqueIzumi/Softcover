package nl.rhaydus.softcover.core.profile.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileSnapshot

interface ProfileRepository {
    fun observeUserProfileData(): Flow<UserProfileData?>

    suspend fun fetchUserProfileSnapshot(): UserProfileSnapshot

    fun streamReadingDaysDescending(userId: Int): Flow<LocalDate>

    // Merges into whatever is cached without touching the stats half - see
    // ProfileLocalDataSource for how the two halves are kept from clobbering each other.
    suspend fun cacheUserProfileActivity(
        readingStreak: Int,
        recentReadingDays: Set<LocalDate>,
    )

    // Merges into whatever is cached without touching the activity half.
    suspend fun cacheUserProfileStats(snapshot: UserProfileSnapshot)

    suspend fun markActiveReadingDate(date: LocalDate)

    suspend fun clearProfileCache()
}
