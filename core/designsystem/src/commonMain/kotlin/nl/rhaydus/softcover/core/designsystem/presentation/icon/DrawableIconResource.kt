package nl.rhaydus.softcover.core.designsystem.presentation.icon

import nl.rhaydus.designsystem.icon.RhaydusIconResource

/**
 * Builds a [RhaydusIconResource.Drawable] from a [SoftcoverIcon] catalog entry. Resolving the catalog's
 * `internal` Compose `DrawableResource` here keeps that resource encapsulated in this module while call
 * sites still pass the typed catalog icon (`SoftcoverIcon.Search`) rather than a raw resource — the
 * bridge between the app's icon catalog and the foundation's brand-agnostic icon token.
 */
fun drawableIconResource(
    contentDescription: String,
    icon: SoftcoverIcon,
): RhaydusIconResource = RhaydusIconResource.Drawable(
    contentDescription = contentDescription,
    resource = icon.resource,
)
