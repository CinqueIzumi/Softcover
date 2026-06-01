package nl.rhaydus.softcover.core.book.data.datasource

class BookNotFoundException(val bookId: Int) : Exception("Book $bookId not found on remote")
