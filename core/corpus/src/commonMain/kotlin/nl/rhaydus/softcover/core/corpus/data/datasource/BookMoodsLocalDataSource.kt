package nl.rhaydus.softcover.core.corpus.data.datasource

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.database.dao.BookMoodsDao
import nl.rhaydus.softcover.core.database.model.BookMoodEntity

internal interface BookMoodsLocalDataSource {
    fun observeAll(): Flow<List<BookMoodEntity>>

    fun observe(bookId: Int): Flow<List<BookMoodEntity>>

    suspend fun replaceForBook(
        bookId: Int,
        entities: List<BookMoodEntity>,
    )
}

internal class BookMoodsLocalDataSourceImpl(
    private val dao: BookMoodsDao,
) : BookMoodsLocalDataSource {
    override fun observeAll(): Flow<List<BookMoodEntity>> = dao.observeAll()

    override fun observe(bookId: Int): Flow<List<BookMoodEntity>> = dao.observe(bookId = bookId)

    override suspend fun replaceForBook(
        bookId: Int,
        entities: List<BookMoodEntity>,
    ) = dao.replaceForBook(
        bookId = bookId,
        entities = entities,
    )
}
