package example.client

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import example.shared.RecordListItem
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class RecordSearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val records: List<RecordListItem> = emptyList(),
)

data class RecordSearchActions(val onQueryChange: (String) -> Unit, val onSearch: () -> Unit)

class RecordViewModel(private val recordPort: RecordDataPort, private val scope: CoroutineScope) {
    var searchState by mutableStateOf(RecordSearchUiState())
        private set

    private var requestNumber = 0
    private var load: Job? = null

    val searchActions =
        RecordSearchActions(
            onQueryChange = { searchState = searchState.copy(query = it) },
            onSearch = ::loadRecords,
        )

    // Actions are called on the UI thread. Latest submitted query wins.
    fun loadRecords() {
        val request = ++requestNumber
        val query = searchState.query
        load?.cancel()
        searchState = searchState.copy(isLoading = true, error = null)
        load =
            scope.launch {
                try {
                    val records = recordPort.listRecords(query, limit = 100)
                    if (request == requestNumber) searchState = searchState.copy(records = records)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    if (request == requestNumber) {
                        searchState = searchState.copy(error = "Could not load records. Try again.")
                    }
                } finally {
                    if (request == requestNumber) searchState = searchState.copy(isLoading = false)
                }
            }
    }
}
