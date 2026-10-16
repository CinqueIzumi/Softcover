package nl.rhaydus.softcover.feature.widget.progress

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import nl.rhaydus.designsystem.util.ObserveAsEvents
import nl.rhaydus.softcover.core.component.progress.ProgressSheetEvent
import nl.rhaydus.softcover.core.component.progress.UpdateProgressBottomSheet
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.uibinding.theme.toSchemeBrightness
import nl.rhaydus.softcover.core.uibinding.theme.toSpinePalette

/** Holds only [bookId], so the screen survives process death; it finishes through [LocalActivity]. */
internal class UpdateProgressScreen(private val bookId: Int) : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<UpdateProgressScreenModel>()
        val state by screenModel.state.collectAsStateWithLifecycle()
        val activity = LocalActivity.current

        LaunchedEffect(bookId) {
            screenModel.runAction(LoadUpdateProgressAction(bookId = bookId))
        }

        ObserveAsEvents(flow = screenModel.events) {
            when (it) {
                UpdateProgressEvent.Finished -> activity?.finish()
            }
        }

        val themeConfiguration = state.themeConfiguration

        SoftcoverTheme(
            palette = themeConfiguration.colorPalette.toSpinePalette(),
            brightness = themeConfiguration.themeMode.toSchemeBrightness(systemInDark = isSystemInDarkTheme()),
            dynamicColor = themeConfiguration.useDynamicColor,
        ) {
            val sheet = state.progressSheet.modelOrNull

            if (sheet != null) {
                UpdateProgressBottomSheet(
                    model = sheet,
                    onEvent = { event ->
                        when (event) {
                            is ProgressSheetEvent.TabSelected -> screenModel.runAction(OnProgressTabClickAction(event.tab))

                            is ProgressSheetEvent.PagesSubmitted -> screenModel.runAction(
                                OnUpdatePageProgressClickAction(newPage = event.page),
                            )

                            is ProgressSheetEvent.PercentageSubmitted -> screenModel.runAction(
                                OnUpdatePercentageProgressClickAction(newPercentage = event.percentage),
                            )

                            is ProgressSheetEvent.TimeChanged -> screenModel.runAction(
                                OnProgressTimeChangedAction(
                                    unit = event.unit,
                                    text = event.text,
                                ),
                            )

                            is ProgressSheetEvent.MinutesStepped -> screenModel.runAction(
                                OnProgressMinutesSteppedAction(delta = event.delta),
                            )

                            is ProgressSheetEvent.TimeSubmitted -> screenModel.runAction(
                                OnUpdateTimeProgressClickAction(
                                    hours = event.hours,
                                    minutes = event.minutes,
                                    seconds = event.seconds,
                                ),
                            )

                            ProgressSheetEvent.MarkAsReadRequested -> screenModel.runAction(OnMarkAsReadAction)

                            is ProgressSheetEvent.WhenReadShifted -> screenModel.runAction(
                                OnWhenReadShiftedAction(minutes = event.minutes),
                            )

                            ProgressSheetEvent.WhenReadReset -> screenModel.runAction(OnWhenReadResetAction())

                            ProgressSheetEvent.Dismissed -> screenModel.runAction(DismissProgressSheetAction)
                        }
                    },
                )
            }
        }
    }
}
