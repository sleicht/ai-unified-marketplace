package example.web.pages.records

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.PageContext
import org.jetbrains.compose.web.dom.*

@Page("{id}")
@Composable
fun RecordPage(ctx: PageContext) {
    val id = ctx.route.params["id"]?.toLongOrNull()
    Main {
        if (id == null || id <= 0) H1 { Text("Invalid record ID") } else H1 { Text("Record $id") }
        A(href = "/") { Text("Back to records") }
    }
}
