package nl.rhaydus.softcover.core.component.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.callout.Banner
import nl.rhaydus.softcover.core.component.callout.BannerUiModel
import nl.rhaydus.softcover.core.component.callout.Callout
import nl.rhaydus.softcover.core.component.callout.CalloutUiModel
import nl.rhaydus.softcover.core.component.celebration.MarkAsReadBurst
import nl.rhaydus.softcover.core.component.celebration.MarkAsReadBurstScale
import nl.rhaydus.softcover.core.component.celebration.MarkAsReadBurstUiModel
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.divider.DividerCenter
import nl.rhaydus.softcover.core.component.divider.DividerInk
import nl.rhaydus.softcover.core.component.divider.VerticalDivider
import nl.rhaydus.softcover.core.component.state.EmptyState
import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.component.topbar.EditorialTopBar
import nl.rhaydus.softcover.core.component.topbar.SearchTopBar
import nl.rhaydus.softcover.core.component.topbar.SearchTopBarUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel

internal val chromeEntries: ImmutableList<GalleryEntry> = persistentListOf(
    galleryEntry(
        name = "TopBar",
        family = GalleryFamily.TOPBAR,
        blurb = "The page bar: an autosizing centred title, an always-drawn back button, and a " +
            "slot for the screen's own actions.",
        previews = TopBarUiModel,
        content = { model, modifier ->
            TopBar(
                model = model,
                onEvent = {},
                modifier = modifier,
            )
        },
    ),
    galleryEntry(
        name = "EditorialTopBar",
        family = GalleryFamily.TOPBAR,
        blurb = "The desktop page bar: a back button, a start-aligned editorial headline and a " +
            "slot for the screen's own actions.",
        previews = TopBarUiModel,
        content = { model, modifier ->
            EditorialTopBar(
                model = model,
                onEvent = {},
                modifier = modifier,
            )
        },
    ),
    galleryEntry(
        name = "SearchTopBar",
        family = GalleryFamily.TOPBAR,
        blurb = "The search chrome: a rounded pill holding the query beside a barcode-scan " +
            "button. Kept separate from TopBar (§ 7.6) — its focus contract has no counterpart there.",
        previews = SearchTopBarUiModel,
        content = { model, modifier ->
            SearchTopBar(
                model = model,
                onEvent = {},
                modifier = modifier,
            )
        },
    ),
    galleryEntry(
        name = "Banner",
        family = GalleryFamily.CALLOUT,
        blurb = "A full-width notice that slides in above a screen's content — the offline " +
            "report at the app root, and a dismissible pace nudge on a book card.",
        previews = BannerUiModel,
        content = { model, modifier ->
            Banner(
                model = model,
                modifier = modifier,
            )
        },
    ),
    galleryEntry(
        name = "Callout",
        family = GalleryFamily.CALLOUT,
        blurb = "A rounded card that calls out one thing worth acting on or knowing — a status, " +
            "a scanned-edition prompt, an app update — with an optional action and dismiss.",
        previews = CalloutUiModel,
        content = { model, modifier ->
            Callout(
                model = model,
                modifier = modifier,
            )
        },
    ),
    GalleryEntry(
        name = "Divider",
        family = GalleryFamily.DIVIDER,
        blurb = "A hairline rule; anatomy and ink resolve per call site, from a plain row " +
            "break to a labelled or glyph-broken one, or a vertical pane separator.",
        fixtures = persistentListOf(
            GalleryFixture(
                label = "Plain rule",
                render = { modifier -> Divider(modifier = modifier) },
            ),
            GalleryFixture(
                label = "Content-tint rule",
                render = { modifier ->
                    Divider(
                        modifier = modifier,
                        ink = DividerInk.ContentTint,
                    )
                },
            ),
            GalleryFixture(
                label = "On-image rule",
                render = { modifier ->
                    Box(modifier = Modifier.background(Color.Black)) {
                        Divider(
                            modifier = modifier,
                            ink = DividerInk.OnImage,
                        )
                    }
                },
            ),
            GalleryFixture(
                label = "Quote mark centre",
                render = { modifier ->
                    Divider(
                        modifier = modifier,
                        center = DividerCenter.QuoteMark,
                    )
                },
            ),
            GalleryFixture(
                label = "Labelled centre",
                render = { modifier ->
                    Divider(
                        modifier = modifier,
                        center = DividerCenter.Label(text = "Or type it"),
                    )
                },
            ),
            GalleryFixture(
                label = "Vertical rule",
                render = { modifier -> VerticalDivider(modifier = modifier.height(48.dp)) },
            ),
        ),
    ),
    galleryEntry(
        name = "EmptyState",
        family = GalleryFamily.STATE,
        blurb = "The message a screen shows in place of content it has nothing to draw: a " +
            "centred flourish with an optional body and button, or an inline title and body.",
        previews = EmptyStateUiModel,
        content = { model, modifier ->
            EmptyState(
                model = model,
                modifier = modifier,
            )
        },
    ),
    GalleryEntry(
        name = "SoftcoverText",
        family = GalleryFamily.RICHTEXT,
        blurb = "The app's text leaf: one style channel, and it masks a stand-in in the real " +
            "style while loading or while the text is null, so the slot never jumps.",
        fixtures = persistentListOf(
            GalleryFixture(
                label = "Plain",
                render = { modifier ->
                    SoftcoverText(
                        text = "The Left Hand of Darkness",
                        standIn = "A book title stand-in",
                        modifier = modifier,
                    )
                },
            ),
            GalleryFixture(
                label = "Loading",
                render = { modifier ->
                    SoftcoverText(
                        text = "The Left Hand of Darkness",
                        isLoading = true,
                        standIn = "A book title stand-in",
                        modifier = modifier,
                    )
                },
            ),
            GalleryFixture(
                label = "Text null, not loading",
                render = { modifier ->
                    SoftcoverText(
                        text = null as String?,
                        standIn = "A book title stand-in",
                        modifier = modifier,
                    )
                },
            ),
            GalleryFixture(
                label = "Two lines, loading",
                render = { modifier ->
                    SoftcoverText(
                        text = null as String?,
                        isLoading = true,
                        standIn = "A two line stand-in\nfor a longer description",
                        maxLines = 2,
                        modifier = modifier.width(140.dp),
                    )
                },
            ),
            GalleryFixture(
                label = "Mixed spans",
                render = { modifier ->
                    SoftcoverText(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Bold ") }
                            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append("italic ") }
                            withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                                append("underline")
                            }
                        },
                        modifier = modifier,
                    )
                },
            ),
        ),
    ),
    GalleryEntry(
        name = "MarkAsReadBurst",
        family = GalleryFamily.CELEBRATION,
        blurb = "The radial particle burst played on a mark-as-read commit. A still fixture " +
            "cannot show it — the motion debug screen is where you watch one.",
        fixtures = persistentListOf(
            GalleryFixture(
                label = "Card",
                render = { modifier ->
                    MarkAsReadBurst(
                        model = MarkAsReadBurstUiModel(
                            triggerKey = 1,
                            scale = MarkAsReadBurstScale.Card,
                        ),
                        modifier = modifier.size(160.dp),
                    )
                },
            ),
            GalleryFixture(
                label = "Screen",
                render = { modifier ->
                    MarkAsReadBurst(
                        model = MarkAsReadBurstUiModel(
                            triggerKey = 1,
                            scale = MarkAsReadBurstScale.Screen,
                        ),
                        modifier = modifier.size(160.dp),
                    )
                },
            ),
        ),
    ),
)
