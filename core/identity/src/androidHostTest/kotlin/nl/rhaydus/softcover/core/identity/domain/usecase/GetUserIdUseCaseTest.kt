package nl.rhaydus.softcover.core.identity.domain.usecase

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.exception.NoUserIdFoundException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GetUserIdUseCaseTest {
    private lateinit var getUserIdAsFlowUseCase: GetUserIdAsFlowUseCase
    private lateinit var useCase: GetUserIdUseCase

    @BeforeEach
    fun setUp() {
        getUserIdAsFlowUseCase = mockk()
        useCase = GetUserIdUseCase(getUserIdAsFlowUseCase = getUserIdAsFlowUseCase)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns success with the userId when the flow emits a valid positive id`() = runTest {
            // ----- Arrange -----
            every {
                getUserIdAsFlowUseCase()
            } returns flowOf(42)

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe 42
        }

        @Test
        fun `returns failure with NoUserIdFoundException when the flow emits -1`() = runTest {
            // ----- Arrange -----
            every {
                getUserIdAsFlowUseCase()
            } returns flowOf(-1)

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<NoUserIdFoundException>()
        }

        @Test
        fun `returns success with userId 1 as a boundary value`() = runTest {
            // ----- Arrange -----
            every {
                getUserIdAsFlowUseCase()
            } returns flowOf(1)

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe 1
        }

        @Test
        fun `returns success with userId 0 as a boundary value`() = runTest {
            // ----- Arrange -----
            every {
                getUserIdAsFlowUseCase()
            } returns flowOf(0)

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe 0
        }

        @Test
        fun `returns failure when the flow is empty`() = runTest {
            // ----- Arrange -----
            every {
                getUserIdAsFlowUseCase()
            } returns emptyFlow()

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
        }
    }
}
