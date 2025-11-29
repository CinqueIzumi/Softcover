package nl.rhaydus.softcover.core.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.UserBooksQuery
import nl.rhaydus.softcover.core.domain.Book
import javax.inject.Inject

// TODO: client should not directly be exposed here
// TODO: Functionality here has been implemented to test, should not be done within main view model
@HiltViewModel
class MainViewModel @Inject constructor(
    private val apolloClient: ApolloClient,
) : ViewModel() {

    fun fetchUserBooks() {
        viewModelScope.launch {
            val result = apolloClient
                // TODO: UserID should be fetched from some sort of local data source instead
                .query(query = UserBooksQuery(userId = 0))
                .execute()

            val books = result.data?.user_books ?: emptyList()

            val mappedBooks = books.map {
                Book(
                    id = it.book.id,
                    title = it.book.title ?: ""
                )
            }

            Log.d("", "Result: $mappedBooks")
        }
    }
}