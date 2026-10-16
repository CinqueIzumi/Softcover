package nl.rhaydus.softcover.feature.widget.progress

import io.kotest.matchers.nulls.shouldBeNull
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

class OnUpdatePercentageProgressClickActionTest {
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
        fun `converts the percentage to a page for a paged book`() = runTest {
            // ----- Arrange -----
            val book = stubPagedBook(pages = 300)
            stateFlow.value = UpdateProgressUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            every {
                dependencies.recordBookProgress
            } returns recordBookProgress

            // ----- Act -----
            OnUpdatePercentageProgressClickAction(newPercentage = "50").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                recordBookProgress(
                    book = book,
                    newPage = 150,
                    newSeconds = null,
                    actionAt = null,
                )
            }
        }

        @Test
        fun `converts the percentage to seconds for an audiobook`() = runTest {
            // ----- Arrange -----
            val book = stubAudiobook(audioSeconds = 7200)
            stateFlow.value = UpdateProgressUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            every {
                dependencies.recordBookProgress
            } returns recordBookProgress

            // ----- Act -----
            OnUpdatePercentageProgressClickAction(newPercentage = "25").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                recordBookProgress(
                    book = book,
                    newPage = null,
                    newSeconds = 1800,
                    actionAt = null,
                )
            }
        }

        @Test
        fun `clamps a percentage above 100 to the last page`() = runTest {
            // ----- Arrange -----
            val book = stubPagedBook(pages = 200)
            stateFlow.value = UpdateProgressUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            every {
                dependencies.recordBookProgress
            } returns recordBookProgress

            // ----- Act -----
            OnUpdatePercentageProgressClickAction(newPercentage = "150").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                recordBookProgress(
                    book = book,
                    newPage = 200,
                    newSeconds = null,
                    actionAt = null,
                )
            }
        }

        @Test
        fun `treats a non-numeric percentage as zero`() = runTest {
            // ----- Arrange -----
            val book = stubAudiobook(audioSeconds = 3600)
            stateFlow.value = UpdateProgressUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            every {
                dependencies.recordBookProgress
            } returns recordBookProgress

            // ----- Act -----
            OnUpdatePercentageProgressClickAction(newPercentage = "abc").execute(
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
        fun `records nothing when there is no book to update`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            every {
                dependencies.recordBookProgress
            } returns recordBookProgress

            // ----- Act -----
            OnUpdatePercentageProgressClickAction(newPercentage = "50").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                recordBookProgress(
                    book = any(),
                    newPage = any(),
                    newSeconds = any(),
                    actionAt = any(),
                )
            }
            stateFlow.value.bookToUpdate.shouldBeNull()
        }
    }
}
