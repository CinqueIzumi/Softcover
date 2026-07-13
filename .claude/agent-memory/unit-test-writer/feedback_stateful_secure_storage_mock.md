---
name: Stateful mock for keyed SecureStorage read/write/delete
description: Pattern for testing migration logic that reads a value back after writing it, against a mockk SecureStorage
type: feedback
---

`nl.rhaydus.platform.SecureStorage` is `read(key)`/`write(key, value)`/`delete(key)`. When a class under test writes a
value and later reads it back in the same call path (e.g. `ApiKeyLocalDataSourceImpl.migrateLegacyKeyIfNeeded()` writes
via `secureStorage.write` then re-reads via `readSecureKey()`), a plain `coEvery { read(key) } returns X` stub can't
reflect the write. Back it with a class-level `var storedKey: String? = null` and three `coEvery { } answers { }` stubs:
`read(key)` returns `storedKey`, `write(key, any())` sets `storedKey = secondArg()`, `delete(key)` sets
`storedKey = null`. Set `storedKey` directly in a test's Arrange section to seed "already stored" scenarios instead of
re-stubbing `read`.

**Why:** [[project_test_conventions]] — mockk stubs are stateless by default, but multi-step migration/read-after-write
flows need to model real backing-store state to assert correctly (e.g. flow emits the newly-migrated key after
construction).

**How to apply:** Any test of a class that both writes to and later reads from the same mocked keyed store within one
code path (`SecureStorage`, similar keyed stores). Reset the backing var in `@BeforeEach`.
