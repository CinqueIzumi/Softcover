package nl.rhaydus.softcover.feature.widget.progress

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.RecordBookProgressUseCase
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnUpdateTimeProgressClickActionTest {
    private lateinit var recordBookProgress: RecordBookProgressUseCase
    private lateinit var stateFlow: MutableStateFlow<UpdateProgressUiState>
    private lateinit var scope: ActionScope<UpdateProgressUiState, UpdateProgressEvent, UpdateProgressLocalVariables>

    @BeforeEach
    fun setUp() {
        recordBookProgress = mockk(relaxed = true)
        coEvery {
            recordBookProgress(
                book = any(),
                newPage = any(),
                newSeconds = any(),
                actionAt = any(),
            )
        } returns Result.success(null)
        stateFlow = MutableStateFlow(UpdateProgressUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(UpdateProgressLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `records the entered hours minutes and seconds as total seconds`() = runTest {
            // ----- Arrange -----
            val book = stubAudiobook(audioSeconds = 7200)
            stateFlow.value = UpdateProgressUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            every {
                dependencies.recordBookProgress
            } returns recordBookProgress

            // ----- Act -----
            OnUpdateTimeProgressClickAction(
                hours = "1",
                minutes = "2",
                seconds = "5",
            ).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                recordBookProgress(
                    book = book,
                    newPage = null,
                    newSeconds = 3725,
                    actionAt = null,
                )
            }
        }

        @Test
        fun `clamps the entered time to the known duration`() = runTest {
            // ----- Arrange -----
            val book = stubAudiobook(audioSeconds = 3600)
            stateFlow.value = UpdateProgressUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            every {
                dependencies.recordBookProgress
            } returns recordBookProgress

            // ----- Act -----
            OnUpdateTimeProgressClickAction(
                hours = "2",
                minutes = "0",
                seconds = "0",
            ).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                recordBookProgress(
                    book = book,
                    newPage = null,
                    newSeconds = 3600,
                    actionAt = null,
                )
            }
        }

        @Test
        fun `treats blank fields as zero`() = runTest {
            // ----- Arrange -----
            val book = stubAudiobook(audioSeconds = 3600)
            stateFlow.value = UpdateProgressUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            every {
                dependencies.recordBookProgress
            } returns recordBookProgress

            // ----- Act -----
            OnUpdateTimeProgressClickAction(
                hours = "",
                minutes = "",
                seconds = "",
            ).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                recordBookProgress(
                    book = book,
                    newPage = null,
                    newSeconds = 0,
                    actionAt = null,
                )
            }
        }

        @Test
        fun `accepts any time when the duration is unknown`() = runTest {
            // ----- Arrange -----
            val book = stubAudiobook(audioSeconds = null)
            stateFlow.value = UpdateProgressUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            every {
                dependencies.recordBookProgress
            } returns recordBookProgress

            // ----- Act -----
            OnUpdateTimeProgressClickAction(
                hours = "5",
                minutes = "0",
                seconds = "0",
            ).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                recordBookProgress(
                    book = book,
                    newPage = null,
                    newSeconds = 18000,
                    actionAt = null,
                )
            }
        }
    }
}
