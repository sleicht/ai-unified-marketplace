package example.web.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.varabyte.kobweb.core.AppGlobals
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.isExporting
import example.client.RecordViewModel
import example.web.LocalRecordPort
import example.web.RecordSearchContent

@Page
@Composable
fun IndexPage() {
    val port = LocalRecordPort.current
    val scope = rememberCoroutineScope()
    val vm = remember(port, scope) { RecordViewModel(port, scope) }
    if (!AppGlobals.isExporting) {
        LaunchedEffect(vm) { vm.loadRecords() }
    }
    RecordSearchContent(vm.searchState, vm.searchActions)
}
