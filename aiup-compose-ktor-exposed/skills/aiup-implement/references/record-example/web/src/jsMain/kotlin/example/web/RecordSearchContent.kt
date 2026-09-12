package example.web

import androidx.compose.runtime.Composable
import example.client.RecordSearchActions
import example.client.RecordSearchUiState
import org.jetbrains.compose.web.attributes.ButtonType
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.forId
import org.jetbrains.compose.web.attributes.type
import org.jetbrains.compose.web.dom.*

@Composable
fun RecordSearchContent(state: RecordSearchUiState, actions: RecordSearchActions) {
    Main(
        attrs = {
            style {
                property("max-width", "48rem")
                property("margin", "auto")
                property("padding", "1rem")
            }
        }
    ) {
        H1 { Text("Record browser") }
        Form(
            attrs = {
                addEventListener("submit") {
                    it.preventDefault()
                    actions.onSearch()
                }
            }
        ) {
            Label(attrs = { forId("record-query") }) { Text("Search records") }
            Input(
                InputType.Search,
                attrs = {
                    id("record-query")
                    value(state.query)
                    onInput { actions.onQueryChange(it.value) }
                },
            )
            Button(attrs = { type(ButtonType.Submit) }) { Text("Search") }
        }
        state.error?.let { message -> P(attrs = { attr("role", "alert") }) { Text(message) } }
        if (state.isLoading && state.records.isEmpty()) P { Text("Loading records") }
        else if (!state.isLoading && state.records.isEmpty()) P { Text("No records") }
        Ul {
            state.records.forEach { record ->
                Li { A(href = "/records/${record.id}") { Text(record.displayName) } }
            }
        }
    }
}
