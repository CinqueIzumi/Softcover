package nl.rhaydus.softcover.feature.settings.presentation.screen.section

/**
 * Where [SettingsMenuRow] leads: [SubPage] pushes a screen (chevron), [ExternalLink] hands off to
 * `openUrl` (an "opens in your browser" glyph), and [Copyable] has no interaction — its title sits in
 * a `SelectionContainer` instead (the Discord-username row). [Edit] and [Add] act in place on the row's
 * subject (a pencil, a plus), and [Static] is a row with nothing to do.
 */
internal sealed interface SettingsMenuRowTarget {
    data class SubPage(val onClick: () -> Unit) : SettingsMenuRowTarget

    data class ExternalLink(val onClick: () -> Unit) : SettingsMenuRowTarget

    data class Edit(val onClick: () -> Unit) : SettingsMenuRowTarget

    data class Add(val onClick: () -> Unit) : SettingsMenuRowTarget

    data object Copyable : SettingsMenuRowTarget

    data object Static : SettingsMenuRowTarget
}
