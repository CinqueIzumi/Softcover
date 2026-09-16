package nl.rhaydus.softcover.core.uibinding.release

/**
 * A mapper input, not a component parameter (R11 — a component takes only its model). It picks the
 * copy and placement an unreleased badge should carry; `UnreleasedMapper` resolves that choice into
 * a `BadgeUiModel` label and `BadgeVariant`, and this enum deliberately does not cross into
 * `:core:component` itself.
 */
enum class UnreleasedBadgeStyle {
    Compact,
    Prominent,
    Featured,
}
