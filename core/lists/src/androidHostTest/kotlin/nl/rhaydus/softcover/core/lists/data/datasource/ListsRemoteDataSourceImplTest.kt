package nl.rhaydus.softcover.core.lists.data.datasource

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.CreateListBookMutation
import nl.rhaydus.softcover.CreateListBookMutation.Data.Insert_list_book.List_book.Companion.listBookFragment as createListBookListBookFragment
import nl.rhaydus.softcover.CreateListMutation
import nl.rhaydus.softcover.CreateListMutation.Data.Insert_list.List.Companion.listFragment as createListListFragment
import nl.rhaydus.softcover.GetUserBookListsQuery
import nl.rhaydus.softcover.GetUserBookListsQuery.Data.Me.List.Companion.listFragment as userListsListFragment
import nl.rhaydus.softcover.GetUserListSignaturesQuery
import nl.rhaydus.softcover.MarkEditionAsOwnedMutation
import nl.rhaydus.softcover.RemoveListBookMutation
import nl.rhaydus.softcover.RemoveListBookMutation.Data.Delete_list_book.List.Companion.listFragment as removeListBookListFragment
import nl.rhaydus.softcover.UpdateListBookPositionsMutation
import nl.rhaydus.softcover.UpdateListMutation
import nl.rhaydus.softcover.UpdateListMutation.Data.ListResponse.List.Companion.listFragment as updateListListFragment
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.core.lists.data.mapper.buildListSignature
import nl.rhaydus.softcover.core.lists.data.mapper.toBookList
import nl.rhaydus.softcover.core.lists.data.mapper.toListBook
import nl.rhaydus.softcover.core.lists.domain.exception.ListNameTakenException
import nl.rhaydus.softcover.core.network.helper.safeMutation
import nl.rhaydus.softcover.core.network.helper.safeQuery
import nl.rhaydus.softcover.fragment.ListFragment
import nl.rhaydus.softcover.type.ListInput

class ListsRemoteDataSourceImplTest {
    private lateinit var apolloClient: ApolloClient
    private lateinit var appDispatchers: AppDispatchers
    private lateinit var dataSource: ListsRemoteDataSourceImpl

    @BeforeEach
    fun setUp() {
        apolloClient = mockk()

        val dispatcher = UnconfinedTestDispatcher()
        appDispatchers = AppDispatchers(
            main = dispatcher,
            io = dispatcher,
            default = dispatcher,
        )

        dataSource = ListsRemoteDataSourceImpl(
            apolloClient = apolloClient,
            appDispatchers = appDispatchers,
        )

        mockkStatic("nl.rhaydus.softcover.core.network.helper.ApolloExtensionsKt")
        mockkStatic("nl.rhaydus.softcover.core.lists.data.mapper.ListMapperKt")
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    private fun stubBookEdition(id: Int = 10): BookEdition = mockk {
        every {
            this@mockk.id
        } returns id
    }

    private fun stubBookList(): BookList = mockk()

    private fun stubListBook(listBookId: Int = 1): ListBook = mockk {
        every {
            this@mockk.listBookId
        } returns listBookId
    }

    // ----- FetchListSignatures -----

    @Nested
    inner class FetchListSignatures {
        @Test
        fun `maps each list to a ListSignature with correct signature string`() = runTest {
            // ----- Arrange -----
            val userId = 1
            val updatedAt = "2024-01-10"
            val maxBookUpdatedAt = "2024-01-09"
            val count = 3
            val queryData = mockk<GetUserListSignaturesQuery.Data>()
            val meEntry = mockk<GetUserListSignaturesQuery.Data.Me>()
            val listEntry = mockk<GetUserListSignaturesQuery.Data.Me.List>()
            val aggregate = mockk<GetUserListSignaturesQuery.Data.Me.List.List_books_aggregate>()
            val aggregateInner = mockk<GetUserListSignaturesQuery.Data.Me.List.List_books_aggregate.Aggregate>()
            val max = mockk<GetUserListSignaturesQuery.Data.Me.List.List_books_aggregate.Aggregate.Max>()

            coEvery {
                apolloClient.safeQuery(query = any<GetUserListSignaturesQuery>())
            } returns queryData

            every {
                queryData.me
            } returns listOf(meEntry)
            every {
                meEntry.lists
            } returns listOf(listEntry)
            every {
                listEntry.id
            } returns 42
            every {
                listEntry.updated_at
            } returns updatedAt
            every {
                listEntry.list_books_aggregate
            } returns aggregate
            every {
                aggregate.aggregate
            } returns aggregateInner
            every {
                aggregateInner.count
            } returns count
            every {
                aggregateInner.max
            } returns max
            every {
                max.updated_at
            } returns maxBookUpdatedAt

            // ----- Act -----
            val result = dataSource.fetchListSignatures()

            // ----- Assert -----
            result.size shouldBe 1
            result[0].listId shouldBe 42
            result[0].signature shouldBe buildListSignature(
                updatedAt = updatedAt,
                count = count,
                maxListBookUpdatedAt = maxBookUpdatedAt,
            )
        }

        @Test
        fun `signature uses empty strings for null updatedAt and null maxListBookUpdatedAt`() = runTest {
            // ----- Arrange -----
            val userId = 2
            val queryData = mockk<GetUserListSignaturesQuery.Data>()
            val meEntry = mockk<GetUserListSignaturesQuery.Data.Me>()
            val listEntry = mockk<GetUserListSignaturesQuery.Data.Me.List>()
            val aggregate = mockk<GetUserListSignaturesQuery.Data.Me.List.List_books_aggregate>()
            val aggregateInner = mockk<GetUserListSignaturesQuery.Data.Me.List.List_books_aggregate.Aggregate>()

            coEvery {
                apolloClient.safeQuery(query = any<GetUserListSignaturesQuery>())
            } returns queryData

            every {
                queryData.me
            } returns listOf(meEntry)
            every {
                meEntry.lists
            } returns listOf(listEntry)
            every {
                listEntry.id
            } returns 7
            every {
                listEntry.updated_at
            } returns null
            every {
                listEntry.list_books_aggregate
            } returns aggregate
            every {
                aggregate.aggregate
            } returns aggregateInner
            every {
                aggregateInner.count
            } returns 5
            every {
                aggregateInner.max
            } returns null

            // ----- Act -----
            val result = dataSource.fetchListSignatures()

            // ----- Assert -----
            result[0].signature shouldBe "|5|"
        }

        @Test
        fun `uses count 0 when aggregate is null`() = runTest {
            // ----- Arrange -----
            val userId = 3
            val queryData = mockk<GetUserListSignaturesQuery.Data>()
            val meEntry = mockk<GetUserListSignaturesQuery.Data.Me>()
            val listEntry = mockk<GetUserListSignaturesQuery.Data.Me.List>()
            val aggregate = mockk<GetUserListSignaturesQuery.Data.Me.List.List_books_aggregate>()

            coEvery {
                apolloClient.safeQuery(query = any<GetUserListSignaturesQuery>())
            } returns queryData

            every {
                queryData.me
            } returns listOf(meEntry)
            every {
                meEntry.lists
            } returns listOf(listEntry)
            every {
                listEntry.id
            } returns 9
            every {
                listEntry.updated_at
            } returns "2024-02-01"
            every {
                listEntry.list_books_aggregate
            } returns aggregate
            every {
                aggregate.aggregate
            } returns null

            // ----- Act -----
            val result = dataSource.fetchListSignatures()

            // ----- Assert -----
            result[0].signature shouldBe "2024-02-01|0|"
        }

        @Test
        fun `throws when me list is empty`() = runTest {
            // ----- Arrange -----
            val queryData = mockk<GetUserListSignaturesQuery.Data>()

            coEvery {
                apolloClient.safeQuery(query = any<GetUserListSignaturesQuery>())
            } returns queryData

            every {
                queryData.me
            } returns emptyList()

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.fetchListSignatures()
            }
        }
    }

    // ----- FetchUserLists -----

    @Nested
    inner class FetchUserLists {
        @Test
        fun `returns mapped book lists for each list entry returned by the query`() = runTest {
            // ----- Arrange -----
            val userId = 1
            val expectedBookList = stubBookList()
            val queryData = mockk<GetUserBookListsQuery.Data>()
            val meEntry = mockk<GetUserBookListsQuery.Data.Me>()
            val listEntry = mockk<GetUserBookListsQuery.Data.Me.List>()
            val listFragment = mockk<ListFragment>()

            mockkObject(GetUserBookListsQuery.Data.Me.List.Companion)

            coEvery {
                apolloClient.safeQuery(query = any<GetUserBookListsQuery>())
            } returns queryData

            every {
                queryData.me
            } returns listOf(meEntry)

            every {
                meEntry.lists
            } returns listOf(listEntry)

            with(GetUserBookListsQuery.Data.Me.List.Companion) {
                every {
                    listEntry.listFragment()
                } returns listFragment
            }

            every {
                listFragment.toBookList()
            } returns expectedBookList

            // ----- Act -----
            val result = dataSource.fetchUserLists(userId = userId)

            // ----- Assert -----
            result shouldBe listOf(expectedBookList)
        }

        @Test
        fun `throws when me list is empty`() = runTest {
            // ----- Arrange -----
            val queryData = mockk<GetUserBookListsQuery.Data>()

            coEvery {
                apolloClient.safeQuery(query = any<GetUserBookListsQuery>())
            } returns queryData

            every {
                queryData.me
            } returns emptyList()

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.fetchUserLists(userId = 1)
            }
        }

        @Test
        fun `returns empty list when me has no lists`() = runTest {
            // ----- Arrange -----
            val userId = 1
            val queryData = mockk<GetUserBookListsQuery.Data>()
            val meEntry = mockk<GetUserBookListsQuery.Data.Me>()

            coEvery {
                apolloClient.safeQuery(query = any<GetUserBookListsQuery>())
            } returns queryData

            every {
                queryData.me
            } returns listOf(meEntry)

            every {
                meEntry.lists
            } returns emptyList()

            // ----- Act -----
            val result = dataSource.fetchUserLists(userId = userId)

            // ----- Assert -----
            result shouldBe emptyList()
        }
    }

    // ----- RemoveListBook -----

    @Nested
    inner class RemoveListBook {
        @Test
        fun `throws when mutation returns null delete_list_book wrapper`() = runTest {
            // ----- Arrange -----
            val listBook = stubListBook(listBookId = 22)
            val mutationData = mockk<RemoveListBookMutation.Data>()

            coEvery {
                apolloClient.safeMutation(mutation = any<RemoveListBookMutation>())
            } returns mutationData

            every {
                mutationData.delete_list_book
            } returns null

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.removeListBook(book = listBook)
            }
        }

        @Test
        fun `returns mapped book list when mutation succeeds`() = runTest {
            // ----- Arrange -----
            val listBook = stubListBook(listBookId = 22)
            val expectedBookList = stubBookList()
            val mutationData = mockk<RemoveListBookMutation.Data>()
            val deleteListBook = mockk<RemoveListBookMutation.Data.Delete_list_book>()
            val listEntry = mockk<RemoveListBookMutation.Data.Delete_list_book.List>()
            val listFragment = mockk<ListFragment>()

            mockkObject(RemoveListBookMutation.Data.Delete_list_book.List.Companion)

            coEvery {
                apolloClient.safeMutation(mutation = any<RemoveListBookMutation>())
            } returns mutationData

            every {
                mutationData.delete_list_book
            } returns deleteListBook

            every {
                deleteListBook.list
            } returns listEntry

            with(RemoveListBookMutation.Data.Delete_list_book.List.Companion) {
                every {
                    listEntry.listFragment()
                } returns listFragment
            }

            every {
                listFragment.toBookList()
            } returns expectedBookList

            // ----- Act -----
            val result = dataSource.removeListBook(book = listBook)

            // ----- Assert -----
            result shouldBe expectedBookList
        }

        @Test
        fun `throws when mutation returns null list fragment`() = runTest {
            // ----- Arrange -----
            val listBook = stubListBook(listBookId = 22)
            val mutationData = mockk<RemoveListBookMutation.Data>()
            val deleteListBook = mockk<RemoveListBookMutation.Data.Delete_list_book>()

            mockkObject(RemoveListBookMutation.Data.Delete_list_book.List.Companion)

            coEvery {
                apolloClient.safeMutation(mutation = any<RemoveListBookMutation>())
            } returns mutationData

            every {
                mutationData.delete_list_book
            } returns deleteListBook

            every {
                deleteListBook.list
            } returns null

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.removeListBook(book = listBook)
            }
        }
    }

    // ----- CreateList -----

    @Nested
    inner class CreateList {
        @Test
        fun `returns mapped BookList when response has a populated list and no errors`() = runTest {
            // ----- Arrange -----
            val name = "My Favourites"
            val expectedBookList = stubBookList()
            val mutationData = mockk<CreateListMutation.Data>()
            val insertList = mockk<CreateListMutation.Data.Insert_list>()
            val listEntry = mockk<CreateListMutation.Data.Insert_list.List>()
            val listFragment = mockk<ListFragment>()

            mockkObject(CreateListMutation.Data.Insert_list.List.Companion)

            coEvery {
                apolloClient.safeMutation(mutation = any<CreateListMutation>())
            } returns mutationData

            every {
                mutationData.insert_list
            } returns insertList

            every {
                insertList.errors
            } returns null

            every {
                insertList.list
            } returns listEntry

            with(CreateListMutation.Data.Insert_list.List.Companion) {
                every {
                    listEntry.listFragment()
                } returns listFragment
            }

            every {
                listFragment.toBookList()
            } returns expectedBookList

            // ----- Act -----
            val result = dataSource.createList(name = name)

            // ----- Assert -----
            result shouldBe expectedBookList
        }

        @Test
        fun `throws ListNameTakenException when server reports name already taken`() = runTest {
            // ----- Arrange -----
            val name = "Reading Log"
            val mutationData = mockk<CreateListMutation.Data>()
            val insertList = mockk<CreateListMutation.Data.Insert_list>()

            coEvery {
                apolloClient.safeMutation(mutation = any<CreateListMutation>())
            } returns mutationData

            every {
                mutationData.insert_list
            } returns insertList

            every {
                insertList.errors
            } returns "Name has already been taken"

            // ----- Act & Assert -----
            val thrown = shouldThrow<ListNameTakenException> {
                dataSource.createList(name = name)
            }

            thrown.name shouldBe name
        }

        @Test
        fun `throws ListNameTakenException for case-insensitive name-taken marker`() = runTest {
            // ----- Arrange -----
            val name = "Reading Log"
            val mutationData = mockk<CreateListMutation.Data>()
            val insertList = mockk<CreateListMutation.Data.Insert_list>()

            coEvery {
                apolloClient.safeMutation(mutation = any<CreateListMutation>())
            } returns mutationData

            every {
                mutationData.insert_list
            } returns insertList

            every {
                insertList.errors
            } returns "NAME HAS ALREADY BEEN TAKEN"

            // ----- Act & Assert -----
            val thrown = shouldThrow<ListNameTakenException> {
                dataSource.createList(name = name)
            }

            thrown.name shouldBe name
        }

        @Test
        fun `throws plain Exception when server returns some other error`() = runTest {
            // ----- Arrange -----
            val serverError = "Some other problem"
            val mutationData = mockk<CreateListMutation.Data>()
            val insertList = mockk<CreateListMutation.Data.Insert_list>()

            coEvery {
                apolloClient.safeMutation(mutation = any<CreateListMutation>())
            } returns mutationData

            every {
                mutationData.insert_list
            } returns insertList

            every {
                insertList.errors
            } returns serverError

            // ----- Act & Assert -----
            val thrown = shouldThrow<Exception> {
                dataSource.createList(name = "Sci-Fi")
            }

            thrown.message!!.contains(serverError) shouldBe true
        }

        @Test
        fun `throws plain Exception when list is null and errors is null`() = runTest {
            // ----- Arrange -----
            val mutationData = mockk<CreateListMutation.Data>()
            val insertList = mockk<CreateListMutation.Data.Insert_list>()

            mockkObject(CreateListMutation.Data.Insert_list.List.Companion)

            coEvery {
                apolloClient.safeMutation(mutation = any<CreateListMutation>())
            } returns mutationData

            every {
                mutationData.insert_list
            } returns insertList

            every {
                insertList.errors
            } returns null

            every {
                insertList.list
            } returns null

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.createList(name = "Sci-Fi")
            }
        }

        @Test
        fun `throws plain Exception when insert_list field is absent`() = runTest {
            // ----- Arrange -----
            val mutationData = mockk<CreateListMutation.Data>()

            coEvery {
                apolloClient.safeMutation(mutation = any<CreateListMutation>())
            } returns mutationData

            every {
                mutationData.insert_list
            } returns null

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.createList(name = "Sci-Fi")
            }
        }

        @Test
        fun `passes ListInput with expected defaults to CreateListMutation`() = runTest {
            // ----- Arrange -----
            val name = "History Books"
            val capturedMutation = slot<CreateListMutation>()
            val mutationData = mockk<CreateListMutation.Data>()
            val insertList = mockk<CreateListMutation.Data.Insert_list>()
            val listEntry = mockk<CreateListMutation.Data.Insert_list.List>()
            val listFragment = mockk<ListFragment>()

            mockkObject(CreateListMutation.Data.Insert_list.List.Companion)

            coEvery {
                apolloClient.safeMutation(mutation = capture(capturedMutation))
            } returns mutationData

            every {
                mutationData.insert_list
            } returns insertList

            every {
                insertList.errors
            } returns null

            every {
                insertList.list
            } returns listEntry

            with(CreateListMutation.Data.Insert_list.List.Companion) {
                every {
                    listEntry.listFragment()
                } returns listFragment
            }

            every {
                listFragment.toBookList()
            } returns stubBookList()

            // ----- Act -----
            dataSource.createList(name = name)

            // ----- Assert -----
            val input: ListInput = capturedMutation.captured.list

            input.name shouldBe Optional.Present(name)
            input.description shouldBe Optional.Present("")
            input.privacy_setting_id shouldBe Optional.Present(PrivacySetting.PUBLIC.code)
            input.ranked shouldBe Optional.Present(false)
            input.default_view shouldBe Optional.Present("card")
            input.url shouldBe Optional.Present("")
        }
    }

    @Nested
    inner class MarkEditionAsOwned {
        @Test
        fun `returns mapped ListBook when mutation succeeds`() = runTest {
            // ----- Arrange -----
            val edition = stubBookEdition(id = 55)
            val expectedListBook = stubListBook()
            val mutationData = mockk<MarkEditionAsOwnedMutation.Data>()
            val editionOwned = mockk<MarkEditionAsOwnedMutation.Data.Edition_owned>()
            val listBookEntry = mockk<MarkEditionAsOwnedMutation.Data.Edition_owned.List_book>()

            mockkObject(MarkEditionAsOwnedMutation.Data.Edition_owned.List_book.Companion)

            coEvery {
                apolloClient.safeMutation(mutation = any<MarkEditionAsOwnedMutation>())
            } returns mutationData

            every {
                mutationData.edition_owned
            } returns editionOwned

            every {
                editionOwned.list_book
            } returns listBookEntry

            with(MarkEditionAsOwnedMutation.Data.Edition_owned.List_book.Companion) {
                every {
                    listBookEntry.listBookFragment()
                } returns mockk {
                    every {
                        toListBook()
                    } returns expectedListBook
                }
            }

            // ----- Act -----
            val result = dataSource.markEditionAsOwned(edition = edition)

            // ----- Assert -----
            result shouldBe expectedListBook
        }

        @Test
        fun `throws when mutation returns null list_book`() = runTest {
            // ----- Arrange -----
            val edition = stubBookEdition(id = 55)
            val mutationData = mockk<MarkEditionAsOwnedMutation.Data>()
            val editionOwned = mockk<MarkEditionAsOwnedMutation.Data.Edition_owned>()

            mockkObject(MarkEditionAsOwnedMutation.Data.Edition_owned.List_book.Companion)

            coEvery {
                apolloClient.safeMutation(mutation = any<MarkEditionAsOwnedMutation>())
            } returns mutationData

            every {
                mutationData.edition_owned
            } returns editionOwned

            every {
                editionOwned.list_book
            } returns null

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.markEditionAsOwned(edition = edition)
            }
        }

        @Test
        fun `throws when toListBook maps the fragment to null`() = runTest {
            // ----- Arrange -----
            val edition = stubBookEdition(id = 55)
            val mutationData = mockk<MarkEditionAsOwnedMutation.Data>()
            val editionOwned = mockk<MarkEditionAsOwnedMutation.Data.Edition_owned>()
            val listBookEntry = mockk<MarkEditionAsOwnedMutation.Data.Edition_owned.List_book>()

            mockkObject(MarkEditionAsOwnedMutation.Data.Edition_owned.List_book.Companion)

            coEvery {
                apolloClient.safeMutation(mutation = any<MarkEditionAsOwnedMutation>())
            } returns mutationData

            every {
                mutationData.edition_owned
            } returns editionOwned

            every {
                editionOwned.list_book
            } returns listBookEntry

            with(MarkEditionAsOwnedMutation.Data.Edition_owned.List_book.Companion) {
                every {
                    listBookEntry.listBookFragment()
                } returns mockk {
                    every {
                        toListBook()
                    } returns null
                }
            }

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.markEditionAsOwned(edition = edition)
            }
        }
    }

    // ----- AddBookToList -----

    @Nested
    inner class UpdateListBookPositions {
        @Test
        fun `returns without calling Apollo when orderedListBookIds is empty`() = runTest {
            // ----- Arrange -----
            // No stub for safeMutation — it must not be called.

            // ----- Act -----
            dataSource.updateListBookPositions(
                listId = 1,
                startPosition = 0,
                orderedListBookIds = emptyList(),
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                apolloClient.safeMutation(mutation = any<UpdateListBookPositionsMutation>())
            }
        }

        @Test
        fun `calls Apollo with clearedPositions equal to startPosition until start plus size`() = runTest {
            // ----- Arrange -----
            val capturedMutation = slot<UpdateListBookPositionsMutation>()
            val mutationData = mockk<UpdateListBookPositionsMutation.Data>()

            coEvery {
                apolloClient.safeMutation(mutation = capture(capturedMutation))
            } returns mutationData

            // ----- Act -----
            dataSource.updateListBookPositions(
                listId = 7,
                startPosition = 3,
                orderedListBookIds = listOf(101, 102, 103),
            )

            // ----- Assert -----
            capturedMutation.captured.listId shouldBe 7
            capturedMutation.captured.clearedPositions shouldBe listOf(3, 4, 5)
        }

        @Test
        fun `sends one update entry per list book id with ascending positions`() = runTest {
            // ----- Arrange -----
            val capturedMutation = slot<UpdateListBookPositionsMutation>()
            val mutationData = mockk<UpdateListBookPositionsMutation.Data>()

            coEvery {
                apolloClient.safeMutation(mutation = capture(capturedMutation))
            } returns mutationData

            // ----- Act -----
            dataSource.updateListBookPositions(
                listId = 7,
                startPosition = 0,
                orderedListBookIds = listOf(10, 20),
            )

            // ----- Assert -----
            val updates = capturedMutation.captured.updates

            updates.size shouldBe 2
        }
    }

    @Nested
    inner class AddBookToList {
        @Test
        fun `returns mapped ListBook when mutation succeeds`() = runTest {
            // ----- Arrange -----
            val listId = 5
            val bookId = 3
            val editionId = 10
            val expectedListBook = stubListBook()
            val mutationData = mockk<CreateListBookMutation.Data>()
            val insertListBook = mockk<CreateListBookMutation.Data.Insert_list_book>()
            val listBookEntry = mockk<CreateListBookMutation.Data.Insert_list_book.List_book>()

            mockkObject(CreateListBookMutation.Data.Insert_list_book.List_book.Companion)

            coEvery {
                apolloClient.safeMutation(mutation = any<CreateListBookMutation>())
            } returns mutationData

            every {
                mutationData.insert_list_book
            } returns insertListBook

            every {
                insertListBook.list_book
            } returns listBookEntry

            with(CreateListBookMutation.Data.Insert_list_book.List_book.Companion) {
                every {
                    listBookEntry.listBookFragment()
                } returns mockk {
                    every {
                        toListBook()
                    } returns expectedListBook
                }
            }

            // ----- Act -----
            val result = dataSource.addBookToList(
                listId = listId,
                bookId = bookId,
                editionId = editionId,
            )

            // ----- Assert -----
            result shouldBe expectedListBook
        }

        @Test
        fun `throws when mutation returns null insert_list_book wrapper`() = runTest {
            // ----- Arrange -----
            val mutationData = mockk<CreateListBookMutation.Data>()

            coEvery {
                apolloClient.safeMutation(mutation = any<CreateListBookMutation>())
            } returns mutationData

            every {
                mutationData.insert_list_book
            } returns null

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.addBookToList(
                    listId = 5,
                    bookId = 3,
                    editionId = 10,
                )
            }
        }

        @Test
        fun `throws when mutation returns null list_book`() = runTest {
            // ----- Arrange -----
            val mutationData = mockk<CreateListBookMutation.Data>()
            val insertListBook = mockk<CreateListBookMutation.Data.Insert_list_book>()

            coEvery {
                apolloClient.safeMutation(mutation = any<CreateListBookMutation>())
            } returns mutationData

            every {
                mutationData.insert_list_book
            } returns insertListBook

            every {
                insertListBook.list_book
            } returns null

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.addBookToList(
                    listId = 5,
                    bookId = 3,
                    editionId = 10,
                )
            }
        }

        @Test
        fun `throws when toListBook maps the fragment to null`() = runTest {
            // ----- Arrange -----
            val mutationData = mockk<CreateListBookMutation.Data>()
            val insertListBook = mockk<CreateListBookMutation.Data.Insert_list_book>()
            val listBookEntry = mockk<CreateListBookMutation.Data.Insert_list_book.List_book>()

            mockkObject(CreateListBookMutation.Data.Insert_list_book.List_book.Companion)

            coEvery {
                apolloClient.safeMutation(mutation = any<CreateListBookMutation>())
            } returns mutationData

            every {
                mutationData.insert_list_book
            } returns insertListBook

            every {
                insertListBook.list_book
            } returns listBookEntry

            with(CreateListBookMutation.Data.Insert_list_book.List_book.Companion) {
                every {
                    listBookEntry.listBookFragment()
                } returns mockk {
                    every {
                        toListBook()
                    } returns null
                }
            }

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.addBookToList(
                    listId = 5,
                    bookId = 3,
                    editionId = 10,
                )
            }
        }
    }

    @Nested
    inner class SetListRanked {
        @Test
        fun `returns mapped BookList and passes correct mutation arguments`() = runTest {
            // ----- Arrange -----
            val listId = 42
            val ranked = true
            val expectedBookList = stubBookList()
            val capturedMutation = slot<UpdateListMutation>()
            val mutationData = mockk<UpdateListMutation.Data>()
            val listResponse = mockk<UpdateListMutation.Data.ListResponse>()
            val listEntry = mockk<UpdateListMutation.Data.ListResponse.List>()
            val listFragment = mockk<ListFragment>()

            mockkObject(UpdateListMutation.Data.ListResponse.List.Companion)

            coEvery {
                apolloClient.safeMutation(mutation = capture(capturedMutation))
            } returns mutationData

            every {
                mutationData.listResponse
            } returns listResponse

            every {
                listResponse.errors
            } returns null

            every {
                listResponse.list
            } returns listEntry

            with(UpdateListMutation.Data.ListResponse.List.Companion) {
                every {
                    listEntry.listFragment()
                } returns listFragment
            }

            every {
                listFragment.toBookList()
            } returns expectedBookList

            // ----- Act -----
            val result = dataSource.setListRanked(
                listId = listId,
                ranked = ranked,
            )

            // ----- Assert -----
            result shouldBe expectedBookList

            capturedMutation.captured.id shouldBe listId
            capturedMutation.captured.list.ranked shouldBe Optional.Present(true)
        }

        @Test
        fun `throws when response errors field is non-blank`() = runTest {
            // ----- Arrange -----
            val mutationData = mockk<UpdateListMutation.Data>()
            val listResponse = mockk<UpdateListMutation.Data.ListResponse>()

            coEvery {
                apolloClient.safeMutation(mutation = any<UpdateListMutation>())
            } returns mutationData

            every {
                mutationData.listResponse
            } returns listResponse

            every {
                listResponse.errors
            } returns "Something went wrong"

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.setListRanked(
                    listId = 1,
                    ranked = true,
                )
            }
        }

        @Test
        fun `throws when response list is null`() = runTest {
            // ----- Arrange -----
            val mutationData = mockk<UpdateListMutation.Data>()
            val listResponse = mockk<UpdateListMutation.Data.ListResponse>()

            mockkObject(UpdateListMutation.Data.ListResponse.List.Companion)

            coEvery {
                apolloClient.safeMutation(mutation = any<UpdateListMutation>())
            } returns mutationData

            every {
                mutationData.listResponse
            } returns listResponse

            every {
                listResponse.errors
            } returns null

            every {
                listResponse.list
            } returns null

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.setListRanked(
                    listId = 1,
                    ranked = false,
                )
            }
        }
    }
}
