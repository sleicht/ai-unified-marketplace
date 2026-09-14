package example.client

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RecordApp(recordPort: RecordDataPort) {
    val scope = rememberCoroutineScope()
    val vm = remember(recordPort, scope) { RecordViewModel(recordPort, scope) }
    MaterialTheme { RecordBrowserScreen(vm.searchState, vm.searchActions) }
}

@Composable
fun RecordBrowserScreen(state: RecordSearchUiState, actions: RecordSearchActions) {
    Column(Modifier.padding(16.dp)) {
        Text("Record browser")
        OutlinedTextField(
            value = state.query,
            onValueChange = actions.onQueryChange,
            label = { Text("Search records") },
        )
        Button(onClick = actions.onSearch) { Text("Search") }
        state.error?.let { Text(it) }
        if (state.isLoading && state.records.isEmpty()) Text("Loading records")
        else if (!state.isLoading && state.records.isEmpty()) Text("No records")
        state.records.forEach { Text(it.displayName) }
    }
}
