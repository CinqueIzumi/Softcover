---
name: feedback_apollo_compiled_field_mocking
description: How to mock CompiledField for Apollo normalized cache resolver tests — which methods need stubs for the delegate path
metadata:
  type: feedback
---

When testing a `CacheResolver` that delegates to `FieldPolicyCacheResolver`, you must stub three methods beyond the obvious ones:

1. `field.argumentValue(any(), any())` — called by `SoftcoverCacheResolver` itself (single argument lookup)
2. `field.argumentValues(any(), any())` — called by `FieldPolicyCacheResolver.resolveField` with a lambda filter; stub to return `emptyMap()` so no key-arg rewrite happens
3. `field.nameWithArguments(any())` — called by `DefaultCacheResolver.resolveField` to build the parent-map lookup key; stub to return `field.responseName`

Without (2) and (3), MockK throws `no answer found for CompiledField.argumentValues(...)`.

**Why:** `FieldPolicyCacheResolver` calls the `argumentValues(vars, filter)` overload (not `argumentValue`) to extract `@fieldPolicy` key args, then falls through to `DefaultCacheResolver` which uses `nameWithArguments` as the map key.

**How to apply:** Add a `stubForDelegation(responseName: String)` helper in any `CacheResolver` test that exercises the fall-through path:

```kotlin
private fun stubForDelegation(responseName: String) {
    every { field.responseName } returns responseName
    every { field.argumentValues(any(), any()) } returns emptyMap()
    every { field.nameWithArguments(any()) } returns responseName
}
```

`CompiledField.argumentValues` is `@ApolloExperimental` — the compiler emits a warning but it compiles fine.
