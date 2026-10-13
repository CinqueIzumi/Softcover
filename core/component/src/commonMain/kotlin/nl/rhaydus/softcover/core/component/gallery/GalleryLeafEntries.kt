package nl.rhaydus.softcover.core.component.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.chip.ChipFlow
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.chrome.ChromeIconButton
import nl.rhaydus.softcover.core.component.chrome.overMediaIconButtonColors
import nl.rhaydus.softcover.core.component.snackbar.SnackbarSurface
import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel
import nl.rhaydus.softcover.core.component.state.OfflineEmptyStatePlaceholder
import nl.rhaydus.softcover.core.component.text.FadeThroughText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

private class StillSnackbarData(
    message: String,
    actionLabel: String?,
) : SnackbarData {
    override val visuals: SnackbarVisuals = object : SnackbarVisuals {
        override val message: String = message
        override val actionLabel: String? = actionLabel
        override val withDismissAction: Boolean = false
        override val duration: SnackbarDuration = SnackbarDuration.Indefinite
    }

    override fun performAction() = Unit

    override fun dismiss() = Unit
}

private val plainSnackbar = StillSnackbarData(
    message = "Added Piranesi to Want to read",
    actionLabel = null,
)

private val actionSnackbar = StillSnackbarData(
    message = "Removed Piranesi from Winter reading",
    actionLabel = "Undo",
)

internal val leafEntries: ImmutableList<GalleryEntry> = persistentListOf(
    galleryEntry(
        name = "ChipFlow",
        family = GalleryFamily.CHIP,
        blurb = "Every chip of a set wrapped in one flow row; an event whose chip has left the set " +
            "is dropped before it reaches the host.",
        previews = ChipSet,
        content = { model, modifier ->
            ChipFlow(
                set = model,
                onEvent = { _, _ -> },
                modifier = modifier,
            )
        },
    ),
    GalleryEntry(
        name = "ChromeIconButton",
        family = GalleryFamily.CHROME,
        blurb = "The icon-only button of a bar or strip, with a hover tooltip on desktop; a scrim " +
            "variant keeps it legible over cover art.",
        fixtures = persistentListOf(
            GalleryFixture(
                label = "Default",
                render = { modifier ->
                    ChromeIconButton(
                        icon = SoftcoverIcon.Edit,
                        contentDescription = "Edit",
                        tooltip = "Edit",
                        onClick = {},
                        modifier = modifier,
                    )
                },
            ),
            GalleryFixture(
                label = "Over media",
                render = { modifier ->
                    Box(modifier = modifier.background(MaterialTheme.colorScheme.scrim)) {
                        ChromeIconButton(
                            icon = SoftcoverIcon.ArrowBack,
                            contentDescription = "Back",
                            tooltip = "Back",
                            onClick = {},
                            colors = overMediaIconButtonColors(),
                        )
                    }
                },
            ),
            GalleryFixture(
                label = "Smaller glyph",
                render = { modifier ->
                    ChromeIconButton(
                        icon = SoftcoverIcon.MoreVert,
                        contentDescription = "More",
                        tooltip = "More",
                        onClick = {},
                        iconSize = 18.dp,
                        modifier = modifier,
                    )
                },
            ),
        ),
    ),
    GalleryEntry(
        name = "FadeThroughText",
        family = GalleryFamily.TEXT,
        blurb = "SoftcoverText that fades out and back in when its text changes between two loaded " +
            "values, and masks while loading. A still fixture cannot show the fade.",
        fixtures = persistentListOf(
            GalleryFixture(
                label = "Loaded",
                render = { modifier ->
                    FadeThroughText(
                        text = "Chapter 14: The Salt Marsh",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = modifier,
                    )
                },
            ),
            GalleryFixture(
                label = "Loading",
                render = { modifier ->
                    FadeThroughText(
                        text = "Chapter 14: The Salt Marsh",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        isLoading = true,
                        standIn = "Chapter 14: The Salt Marsh",
                        modifier = modifier,
                    )
                },
            ),
        ),
    ),
    GalleryEntry(
        name = "OfflineEmptyStatePlaceholder",
        family = GalleryFamily.STATE,
        blurb = "The full-pane empty state for a screen whose content cannot load offline: it fills " +
            "its parent and owns its own padding.",
        fixtures = persistentListOf(
            GalleryFixture(
                label = "Offline",
                render = { modifier ->
                    OfflineEmptyStatePlaceholder(
                        model = EmptyStateUiModel.previews.getValue("Nothing on the shelf"),
                        modifier = modifier.height(280.dp),
                    )
                },
            ),
        ),
    ),
    GalleryEntry(
        name = "SoftcoverSnackbarHost",
        family = GalleryFamily.SNACKBAR,
        blurb = "The host bound to the app's snackbar manager, which draws the topmost snackbar with " +
            "a fade and scale entrance. The fixtures hold one still, since the live host reads " +
            "global state and times out.",
        fixtures = persistentListOf(
            GalleryFixture(
                label = "Message",
                render = { modifier ->
                    SnackbarSurface(
                        data = plainSnackbar,
                        modifier = modifier,
                    )
                },
            ),
            GalleryFixture(
                label = "With action",
                render = { modifier ->
                    SnackbarSurface(
                        data = actionSnackbar,
                        modifier = modifier,
                    )
                },
            ),
        ),
    ),
)
