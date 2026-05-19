package nl.rhaydus.softcover.feature.books.data.datasource

class BookNotFoundException(val bookId: Int) : Exception("Book $bookId not found on remote")
