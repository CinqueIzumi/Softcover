package nl.rhaydus.softcover.feature.widget.presentation.worker

import android.content.Context
import android.content.Intent
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

class WidgetTimeChangedReceiverTest {
    private val scheduler = mockk<WidgetMaintenanceScheduler>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        startKoin { modules(module { single { scheduler } }) }
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }

    private fun intentWith(action: String?): Intent {
        val intent = mockk<Intent>()
        every {
            intent.action
        } returns action
        return intent
    }

    @Nested
    inner class OnReceive {
        @ParameterizedTest
        @ValueSource(strings = [Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED])
        fun `a clock or time zone change requests a repaint`(action: String) {
            // ----- Arrange -----
            val receiver = WidgetTimeChangedReceiver()

            // ----- Act -----
            receiver.onReceive(
                context,
                intentWith(action),
            )

            // ----- Assert -----
            verify(exactly = 1) { scheduler.requestRepaint() }
        }

        @ParameterizedTest
        @ValueSource(strings = [Intent.ACTION_BOOT_COMPLETED, "com.example.SPOOFED", ""])
        fun `any other action does nothing`(action: String) {
            // ----- Arrange -----
            val receiver = WidgetTimeChangedReceiver()

            // ----- Act -----
            receiver.onReceive(
                context,
                intentWith(action),
            )

            // ----- Assert -----
            verify(exactly = 0) { scheduler.requestRepaint() }
        }

        @Test
        fun `an intent without an action does nothing`() {
            // ----- Arrange -----
            val receiver = WidgetTimeChangedReceiver()

            // ----- Act -----
            receiver.onReceive(
                context,
                intentWith(null),
            )

            // ----- Assert -----
            verify(exactly = 0) { scheduler.requestRepaint() }
        }
    }
}
