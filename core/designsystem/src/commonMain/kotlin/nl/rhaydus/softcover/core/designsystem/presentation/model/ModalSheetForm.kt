package nl.rhaydus.softcover.core.designsystem.presentation.model

/**
 * The physical form an adaptive modal resolved to for the current window width. A body hosted in an
 * adaptive sheet reads this (via `LocalModalSheetForm`) to adjust density — e.g. stepping a primary
 * action down from a phone-sized thumb target to a desktop pointer target — without re-deriving the
 * window width class itself.
 *
 * - [SHEET] — a bottom sheet (compact and medium widths).
 * - [PANEL] — a centered desktop panel (expanded widths).
 */
enum class ModalSheetForm {
    SHEET,
    PANEL,
}
