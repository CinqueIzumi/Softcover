package nl.rhaydus.softcover.core.book.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HasSyncedStatusesThisSessionUseCaseTest {
    private lateinit var booksRepository: BooksRepository
    private lateinit var useCase: HasSyncedStatusesThisSessionUseCase

    @BeforeEach
    fun setUp() {
        booksRepository = mockk()
        useCase = HasSyncedStatusesThisSessionUseCase(booksRepository = booksRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `is true only when every asked status has been synced`() {
            // ----- Arrange -----
            every {
                booksRepository.syncedStatusesThisSession
            } returns setOf(UserBookStatus.READ, UserBookStatus.CURRENTLY_READING)

            // ----- Act -----
            val covered = useCase(statuses = setOf(UserBookStatus.READ))
            val partlyCovered = useCase(statuses = setOf(UserBookStatus.READ, UserBookStatus.DID_NOT_FINISH))
            val uncovered = useCase(statuses = setOf(UserBookStatus.DID_NOT_FINISH))

            // ----- Assert -----
            covered shouldBe true
            partlyCovered shouldBe false
            uncovered shouldBe false
        }
    }
}
