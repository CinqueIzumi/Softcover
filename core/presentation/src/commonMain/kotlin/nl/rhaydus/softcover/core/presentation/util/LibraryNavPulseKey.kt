package nl.rhaydus.softcover.core.presentation.util

/**
 * The `NavPulse` key for the Library tab icon. A stable, shared identity so the cross-tab trigger
 * (marking a book as read on the Reading tab, in `feature:reading`) and the observing nav chrome (the
 * bottom bar, in `:orchestration`) name the same signal without either reaching for the other's
 * `LibraryTab` type. Fed to `navPulse.pulse(...)` / `rememberPulseScale(navPulse, ...)`.
 */
data object LibraryNavPulseKey
