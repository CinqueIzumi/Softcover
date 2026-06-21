package nl.rhaydus.softcover.orchestration.usecase

import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.account.ResetUserDataUseCase
import nl.rhaydus.softcover.core.domain.cache.NetworkCacheCleaner
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
import nl.rhaydus.softcover.core.profile.domain.repository.ProfileRepository

internal class ResetUserDataUseCaseImpl(
    private val settingsRepository: SettingsRepository,
    private val booksRepository: BooksRepository,
    private val profileRepository: ProfileRepository,
    private val networkCacheCleaner: NetworkCacheCleaner,
) : ResetUserDataUseCase {
    override suspend operator fun invoke(): Result<Unit> {
        return runCatching {
            // Full factory reset: secure-storage token, the entire local database + cover files, the
            // cached profile, every app setting (resetAllSettings restores userId to its -1
            // unauthenticated default), and the network response cache.
            settingsRepository.updateApiKey(key = "")

            booksRepository.removeAllBooks()

            profileRepository.clearProfileCache()

            settingsRepository.resetAllSettings()

            networkCacheCleaner.clearAll()
        }
    }
}
