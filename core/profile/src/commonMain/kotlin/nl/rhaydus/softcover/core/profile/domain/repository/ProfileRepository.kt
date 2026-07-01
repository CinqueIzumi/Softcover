package nl.rhaydus.softcover.core.profile.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileSnapshot

interface ProfileRepository {
    fun observeUserProfileData(): Flow<UserProfileData?>

    suspend fun fetchUserProfileSnapshot(): UserProfileSnapshot

    fun streamReadingDaysDescending(userId: Int): Flow<LocalDate>

    suspend fun getActiveReadingDaysSince(
        userId: Int,
        since: LocalDate,
    ): Set<LocalDate>

    suspend fun cacheUserProfileData(data: UserProfileData)

    suspend fun markActiveReadingDate(date: LocalDate)

    suspend fun clearProfileCache()
}
