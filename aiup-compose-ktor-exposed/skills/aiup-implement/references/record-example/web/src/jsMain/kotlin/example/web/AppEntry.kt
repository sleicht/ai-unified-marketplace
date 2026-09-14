package example.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.varabyte.kobweb.core.App
import example.client.AccessTokenProvider
import example.client.RecordDataPort
import example.client.ServiceApiClient
import example.client.configureServiceClient
import io.ktor.client.HttpClient
import kotlinx.browser.window

val LocalRecordPort = staticCompositionLocalOf<RecordDataPort> { error("Record port not provided") }

// Anonymous read UI: production applications supply their existing OIDC token provider here.
@App
@Composable
fun AppEntry(content: @Composable () -> Unit) {
    val http = remember { HttpClient { configureServiceClient() } }
    DisposableEffect(http) { onDispose { http.close() } }
    val port =
        remember(http) {
            ServiceApiClient(window.location.origin, AccessTokenProvider { null }, http)
        }
    CompositionLocalProvider(LocalRecordPort provides port) { content() }
}
