package nl.rhaydus.softcover.feature.settings.presentation.screen.section

/**
 * Where [SettingsMenuRow] leads: [SubPage] pushes a screen (chevron), [ExternalLink] hands off to
 * `openUrl` (an "opens in your browser" glyph), and [Copyable] has no interaction — its title sits in
 * a `SelectionContainer` instead (the Discord-username row).
 */
internal sealed interface SettingsMenuRowTarget {
    data class SubPage(val onClick: () -> Unit) : SettingsMenuRowTarget

    data class ExternalLink(val onClick: () -> Unit) : SettingsMenuRowTarget

    data object Copyable : SettingsMenuRowTarget
}
