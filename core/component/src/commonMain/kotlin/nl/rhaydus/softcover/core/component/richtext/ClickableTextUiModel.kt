package nl.rhaydus.softcover.core.component.richtext

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.LinkAnnotation
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Prose with tappable runs in it: an [AnnotatedString] whose `url` string annotations [ClickableText]
 * resolves to taps.
 *
 * The caller builds the annotated string — deciding which words are links is the feature's reading of
 * its own copy — and the component only renders and reports.
 */
@Immutable
data class ClickableTextUiModel(val text: AnnotatedString) {
    companion object : UiModelPreviews<ClickableTextUiModel> {
        override val previews: ImmutableList<ClickableTextUiModel> = persistentListOf(
            ClickableTextUiModel(
                text = buildAnnotatedString {
                    append("Find your key ")
                    pushStringAnnotation(
                        tag = "url",
                        annotation = "https://hardcover.app/account/api",
                    )
                    append("here")
                    pop()
                    append(".")
                },
            ),
        )
    }
}
