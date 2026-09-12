package example.client

import example.shared.RecordListItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class RecordViewModelTest {
    private val ada = RecordListItem(1, "REC-1", "Ada", "Standard")

    @Test
    fun latestQueryWinsEvenWhenOldTransportIgnoresCancellation() = runTest {
        val pending = mutableMapOf<String, CompletableDeferred<List<RecordListItem>>>()
        val port = RecordDataPort { query, _ ->
            val result = CompletableDeferred<List<RecordListItem>>()
            pending[query] = result
            withContext(NonCancellable) { result.await() }
        }
        val vm = RecordViewModel(port, this)
        vm.searchActions.onQueryChange("old")
        vm.loadRecords()
        runCurrent()
        assertTrue(vm.searchState.isLoading)
        vm.searchActions.onQueryChange("Ada")
        vm.loadRecords()
        runCurrent()
        pending.getValue("old").complete(emptyList())
        runCurrent()
        assertTrue(vm.searchState.isLoading)
        pending.getValue("Ada").complete(listOf(ada))
        runCurrent()
        assertEquals(listOf(ada), vm.searchState.records)
        assertFalse(vm.searchState.isLoading)
        assertNull(vm.searchState.error)
    }

    @Test
    fun olderResponseCannotReplaceAlreadyDisplayedNewerResults() = runTest {
        val pending = mutableMapOf<String, CompletableDeferred<List<RecordListItem>>>()
        val port = RecordDataPort { query, limit ->
            assertEquals(100, limit)
            val result = CompletableDeferred<List<RecordListItem>>()
            pending[query] = result
            withContext(NonCancellable) { result.await() }
        }
        val vm = RecordViewModel(port, this)
        vm.searchActions.onQueryChange("old")
        vm.searchActions.onSearch()
        runCurrent()
        vm.searchActions.onQueryChange("new")
        vm.searchActions.onSearch()
        runCurrent()
        pending.getValue("new").complete(listOf(ada))
        runCurrent()
        pending.getValue("old").complete(emptyList())
        runCurrent()
        assertEquals(listOf(ada), vm.searchState.records)
        assertFalse(vm.searchState.isLoading)
        assertNull(vm.searchState.error)
    }

    @Test
    fun failureRetainsContentAndRetryClearsError() = runTest {
        var fail = false
        val vm =
            RecordViewModel(
                RecordDataPort { _, _ ->
                    if (fail) error("private diagnostic")
                    listOf(ada)
                },
                this,
            )
        vm.loadRecords()
        runCurrent()
        fail = true
        vm.loadRecords()
        assertEquals(listOf(ada), vm.searchState.records)
        runCurrent()
        assertEquals(listOf(ada), vm.searchState.records)
        assertEquals("Could not load records. Try again.", vm.searchState.error)
        fail = false
        vm.loadRecords()
        runCurrent()
        assertNull(vm.searchState.error)
    }

    @Test
    fun leavingTheScreenDoesNotShowAnError() = runTest {
        val child = CoroutineScope(coroutineContext + Job(coroutineContext[Job]))
        val pending = CompletableDeferred<List<RecordListItem>>()
        val vm = RecordViewModel(RecordDataPort { _, _ -> pending.await() }, child)
        vm.loadRecords()
        runCurrent()
        child.cancel()
        runCurrent()
        assertNull(vm.searchState.error)
        assertFalse(vm.searchState.isLoading)
    }
}
