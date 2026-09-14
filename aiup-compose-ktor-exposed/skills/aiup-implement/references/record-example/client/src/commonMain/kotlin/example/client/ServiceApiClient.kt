package example.client

import example.shared.RecordListItem
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun HttpClientConfig<*>.configureServiceClient() {
    expectSuccess = true
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
}

// The application owns and closes the injected client.
class ServiceApiClient(
    baseUrl: String,
    private val accessTokenProvider: AccessTokenProvider,
    private val httpClient: HttpClient,
) : RecordDataPort {
    private val apiBase = "${baseUrl.trimEnd('/')}/api/v1"

    override suspend fun listRecords(query: String, limit: Int): List<RecordListItem> {
        require(limit in 1..100)
        val token = accessTokenProvider.currentAccessToken()
        return httpClient
            .get("$apiBase/records") {
                if (token != null) bearerAuth(token)
                parameter("query", query)
                parameter("limit", limit)
            }
            .body()
    }
}
