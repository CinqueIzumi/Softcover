package nl.rhaydus.softcover.core.presentation.navigation

/**
 * Marks a Voyager `Screen` constructor parameter that must be **excluded** from the Android
 * back-stack's Java serialization.
 *
 * On Android, Voyager's `Screen` extends `java.io.Serializable` and the default `Navigator`
 * persists the screen stack across configuration change / process death via `rememberSaveable`. A
 * non-serializable nav argument (e.g. a `BookInitialCover` morph hint that holds domain models)
 * would then throw `NotSerializableException`. This annotation actuals to `kotlin.jvm.Transient` on
 * Android — the field is dropped from serialization (it restores as `null`, and the screen re-fetches
 * by id) — and is erased on iOS, where `Screen` is not serializable, so there is nothing to exclude.
 */
@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
@Target(AnnotationTarget.FIELD)
expect annotation class TransientNavArg()
