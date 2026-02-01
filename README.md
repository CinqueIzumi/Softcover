# Softcover
Softcover is a native app for Android based on [Hardcover.app](https://hardcover.app/)

# Tech Stack
- Minimum SDK level is 26 (Android 8.0)
- Written in [Kotlin](https://kotlinlang.org/).
- [Jetpack Compose](https://developer.android.com/jetpack/compose): Android's modern toolkit for building Native UI.
- [Apollo](https://www.apollographql.com/docs/kotlin): Used to communicate with the API (GraphQL)
- [DataStore](https://developer.android.com/jetpack/androidx/releases/datastore): Used for caching simple data on the user's device.
- [Voyager](https://voyager.adriel.cafe/): Used for navigation and their custom state holder model implementation.
- [Coil](https://github.com/coil-kt/coil): Used to load images within the UI.
- [Timber](https://github.com/JakeWharton/timber): Used for logging.
- [Koin](https://insert-koin.io/docs/setup/koin/): Used for dependency injection.
- [Room](https://developer.android.com/jetpack/androidx/releases/room): Used for locally caching the user's books.

# Features
- Search for books within the data supplied by Hardcover.
- Add books to the user's library (Want to Read, Reading, Read, Did not Finish).
- Remove books from the user's library.
- View the user's saved books.
- Update user's book progress.
- Switch editions of a user's book.

## Disclaimer
This project is an independent, open-source application and is **not affiliated with, endorsed by, or sponsored by** [Hardcover.app](https://hardcover.app/).