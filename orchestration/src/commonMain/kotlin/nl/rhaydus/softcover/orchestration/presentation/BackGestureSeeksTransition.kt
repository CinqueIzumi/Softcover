package nl.rhaydus.softcover.orchestration.presentation

/**
 * Whether the system back gesture drives the root push/pop transition by its progress (`true`) or
 * commits on release and plays the plain pop transition (`false`). Only Android reports gesture
 * progress to the app; iOS's edge swipe and desktop have nothing to seek with.
 */
internal expect val backGestureSeeksTransition: Boolean
