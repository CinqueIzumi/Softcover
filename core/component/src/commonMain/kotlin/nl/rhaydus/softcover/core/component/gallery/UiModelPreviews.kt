package nl.rhaydus.softcover.core.component.gallery

import kotlinx.collections.immutable.ImmutableList

/**
 * The R5 fixture contract from `component-contract.md` § 7.2 — a UI model's `companion object`
 * implements this, so "every UI model ships preview fixtures" is a compile error to forget rather
 * than a review note.
 *
 * [previews] is both the Component Gallery's data and the mappers' expected outputs, so a
 * component's preview set and its test set cannot diverge. Per § 7.2 R5, cover the variants, not
 * the permutations: one fixture per variant branch, plus one for each decoration that changes the
 * anatomy. A fixture that only differs by string content is noise in the gallery.
 */
interface UiModelPreviews<M> {
    val previews: ImmutableList<M>
}
