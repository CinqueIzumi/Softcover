package nl.rhaydus.softcover.feature.widget.presentation.util

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.os.Bundle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReportedWidgetSizesTest {
    private val minSize = {
        DpSize(
            width = 40.dp,
            height = 40.dp,
        )
    }

    private fun options(
        minWidth: Int = 0,
        maxWidth: Int = 0,
        minHeight: Int = 0,
        maxHeight: Int = 0,
    ): Bundle = mockk<Bundle> {
        every {
            getInt(
                AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,
                0,
            )
        } returns minWidth
        every {
            getInt(
                AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,
                0,
            )
        } returns maxWidth
        every {
            getInt(
                AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,
                0,
            )
        } returns minHeight
        every {
            getInt(
                AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,
                0,
            )
        } returns maxHeight
    }

    @Nested
    inner class ReportedWidgetSizes {
        @Test
        fun `the min and max dimensions give the portrait and landscape sizes`() {
            // ----- Arrange -----
            val bundle = options(
                minWidth = 110,
                maxWidth = 250,
                minHeight = 120,
                maxHeight = 300,
            )

            // ----- Act -----
            val sizes = bundle.reportedWidgetSizes(minSize = minSize)

            // ----- Assert -----
            sizes shouldContainExactly listOf(
                DpSize(
                    width = 110.dp,
                    height = 300.dp,
                ),
                DpSize(
                    width = 250.dp,
                    height = 120.dp,
                ),
            )
        }

        @Test
        fun `only a usable portrait pair gives just the portrait size`() {
            // ----- Arrange -----
            val bundle = options(
                minWidth = 110,
                maxHeight = 300,
            )

            // ----- Act -----
            val sizes = bundle.reportedWidgetSizes(minSize = minSize)

            // ----- Assert -----
            sizes shouldContainExactly listOf(
                DpSize(
                    width = 110.dp,
                    height = 300.dp,
                ),
            )
        }

        @Test
        fun `only a usable landscape pair gives just the landscape size`() {
            // ----- Arrange -----
            val bundle = options(
                maxWidth = 250,
                minHeight = 120,
            )

            // ----- Act -----
            val sizes = bundle.reportedWidgetSizes(minSize = minSize)

            // ----- Assert -----
            sizes shouldContainExactly listOf(
                DpSize(
                    width = 250.dp,
                    height = 120.dp,
                ),
            )
        }

        @Test
        fun `nothing usable gives the min size`() {
            // ----- Arrange -----
            val bundle = options()

            // ----- Act -----
            val sizes = bundle.reportedWidgetSizes(minSize = minSize)

            // ----- Assert -----
            sizes shouldContainExactly listOf(minSize())
        }
    }

    @Nested
    inner class ProviderMinSize {
        @BeforeEach
        fun setUp() {
            mockkStatic(AppWidgetManager::class)
        }

        @AfterEach
        fun tearDown() {
            unmockkStatic(AppWidgetManager::class)
        }

        private fun contextWith(info: AppWidgetProviderInfo): Context {
            val manager = mockk<AppWidgetManager> {
                every {
                    getAppWidgetInfo(7)
                } returns info
            }
            val metrics = DisplayMetrics().apply { density = 2f }
            val resources = mockk<Resources> {
                every {
                    displayMetrics
                } returns metrics
            }
            val context = mockk<Context> {
                every {
                    this@mockk.resources
                } returns resources
            }
            every {
                AppWidgetManager.getInstance(context)
            } returns manager
            return context
        }

        private fun providerInfo(
            resizeMode: Int,
        ): AppWidgetProviderInfo = mockk<AppWidgetProviderInfo>().apply {
            this.resizeMode = resizeMode
            minWidth = 200
            minHeight = 300
            minResizeWidth = 100
            minResizeHeight = 150
        }

        @Test
        fun `the min resize size lowers only the axes the widget can resize along`() {
            // ----- Arrange -----
            val info = providerInfo(resizeMode = AppWidgetProviderInfo.RESIZE_HORIZONTAL)
            val context = contextWith(info)

            // ----- Act -----
            val result = context.providerMinSize(7)

            // ----- Assert -----
            result shouldBe DpSize(
                width = 50.dp,
                height = 150.dp,
            )
        }

        @Test
        fun `a widget that cannot be resized keeps its declared minimum`() {
            // ----- Arrange -----
            val info = providerInfo(resizeMode = AppWidgetProviderInfo.RESIZE_NONE)
            val context = contextWith(info)

            // ----- Act -----
            val result = context.providerMinSize(7)

            // ----- Assert -----
            result shouldBe DpSize(
                width = 100.dp,
                height = 150.dp,
            )
        }
    }
}
