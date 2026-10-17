package nl.rhaydus.softcover.core.component.gallery

import kotlinx.collections.immutable.ImmutableMap

/**
 * The R5 fixture contract from `component-contract.md` § 7.2 — a UI model's `companion object`
 * implements this wherever the model is rendered in the Component Gallery or a `@Preview`, or its
 * fixtures are read by a test.
 *
 * [previews] maps each fixture's gallery label to the fixture, so the gallery's data, the
 * `@Preview`s and the mappers' expected outputs are one set. Per § 7.2 R5, cover the variants, not
 * the permutations: one fixture per variant branch, plus one for each decoration that changes the
 * anatomy. A fixture that only differs by string content is noise in the gallery.
 */
interface UiModelPreviews<M> {
    val previews: ImmutableMap<String, M>
}
