package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import nl.rhaydus.softcover.feature.settings.presentation.model.DocumentPage
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.DocumentPageScreenModel
import org.koin.compose.currentKoinScope
import org.koin.core.parameter.parametersOf

/**
 * The model for [page], tagged by it so Settings can hold the Roadmap and What's new models at once.
 * `koinScreenModel` derives its tag from a Koin qualifier and cannot also pass the page as a parameter.
 */
@Composable
internal fun Screen.koinDocumentPageScreenModel(page: DocumentPage): DocumentPageScreenModel {
    val koinScope = currentKoinScope()

    return rememberScreenModel(tag = page.name) {
        koinScope.get<DocumentPageScreenModel> { parametersOf(page) }
    }
}
