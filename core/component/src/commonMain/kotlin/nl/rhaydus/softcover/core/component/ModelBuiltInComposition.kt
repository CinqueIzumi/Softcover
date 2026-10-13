package nl.rhaydus.softcover.core.component

/**
 * Exempts a function or top-level property from the `UiModelBuiltInRender` detekt rule. Every use is
 * listed in `component-contract.md` § 7.4a.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
annotation class ModelBuiltInComposition(val reason: String)
