package nl.rhaydus.softcover.feature.widget.presentation.screen

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import nl.rhaydus.designsystem.util.ObserveAsEvents
import nl.rhaydus.softcover.core.component.progress.UpdateProgressBottomSheet
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.uibinding.theme.toSchemeBrightness
import nl.rhaydus.softcover.core.uibinding.theme.toSpinePalette
import nl.rhaydus.softcover.feature.widget.presentation.action.LoadUpdateProgressAction
import nl.rhaydus.softcover.feature.widget.presentation.action.OnProgressSheetEventAction
import nl.rhaydus.softcover.feature.widget.presentation.event.UpdateProgressEvent
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.UpdateProgressScreenModel

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
            val sheet = state.progressSheet.session.modelOrNull

            if (sheet != null) {
                UpdateProgressBottomSheet(
                    model = sheet,
                    onEvent = { screenModel.runAction(OnProgressSheetEventAction(event = it)) },
                )
            }
        }
    }
}
