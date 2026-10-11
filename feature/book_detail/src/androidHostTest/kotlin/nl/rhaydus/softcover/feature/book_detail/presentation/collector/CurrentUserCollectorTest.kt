package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveUserProfileDataUseCase
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CurrentUserCollectorTest {
    private lateinit var profileFlow: MutableStateFlow<UserProfileData?>
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        profileFlow = MutableStateFlow(null)
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        val observeUserProfileDataUseCase = mockk<ObserveUserProfileDataUseCase> {
            every {
                this@mockk.invoke()
            } returns profileFlow
        }
        dependencies = mockk<BookDetailDependencies>(relaxed = true).also {
            every {
                it.observeUserProfileDataUseCase
            } returns observeUserProfileDataUseCase
        }
    }

    private fun TestScope.launchCollector() {
        backgroundScope.launch {
            CurrentUserCollector().onLaunch(
                scope = scope,
                dependencies = dependencies,
            )
        }
    }

    private fun profile(
        username: String,
        profileImageUrl: String,
    ): UserProfileData = mockk {
        every {
            this@mockk.username
        } returns username

        every {
            this@mockk.profileImageUrl
        } returns profileImageUrl
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `publishes the username and avatar of the profile`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            launchCollector()

            // ----- Act -----
            profileFlow.value = profile(
                username = "frodo",
                profileImageUrl = "https://example.com/frodo.png",
            )

            // ----- Assert -----
            stateFlow.value.currentUsername shouldBe "frodo"
            stateFlow.value.currentUserAvatarUrl shouldBe "https://example.com/frodo.png"
        }

        @Test
        fun `treats a blank username and avatar as missing`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            launchCollector()

            // ----- Act -----
            profileFlow.value = profile(
                username = "  ",
                profileImageUrl = "",
            )

            // ----- Assert -----
            stateFlow.value.currentUsername shouldBe null
            stateFlow.value.currentUserAvatarUrl shouldBe null
        }

        @Test
        fun `clears the identity when the profile goes away`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            profileFlow.value = profile(
                username = "frodo",
                profileImageUrl = "https://example.com/frodo.png",
            )
            launchCollector()

            // ----- Act -----
            profileFlow.value = null

            // ----- Assert -----
            stateFlow.value.currentUsername shouldBe null
            stateFlow.value.currentUserAvatarUrl shouldBe null
        }
    }
}
