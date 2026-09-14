package example.client

import example.shared.RecordListItem
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.JsonConvertException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json

class ServiceApiClientTest {
    @Test
    fun requestAndResponseMatchTheContract() = runTest {
        var request: HttpRequestData? = null
        val expected = RecordListItem(1, "REC-1", "Ada", "Standard")
        val engine = MockEngine {
            request = it
            respond(
                Json.encodeToString(listOf(expected)),
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val http = HttpClient(engine) { configureServiceClient() }
        try {
            val client = ServiceApiClient("https://service.invalid/", { "test-token" }, http)
            assertEquals(listOf(expected), client.listRecords("Ada & Co", 100))
            val captured = requireNotNull(request)
            assertEquals(HttpMethod.Get, captured.method)
            assertEquals(
                "https://service.invalid/api/v1/records?query=Ada+%26+Co&limit=100",
                captured.url.toString(),
            )
            assertEquals("Bearer test-token", captured.headers[HttpHeaders.Authorization])
        } finally {
            http.close()
        }
    }

    @Test
    fun errorStatusesNeverBecomeSuccessfulLists() = runTest {
        for (status in
            listOf(
                HttpStatusCode.Unauthorized,
                HttpStatusCode.Forbidden,
                HttpStatusCode.InternalServerError,
            )) {
            val http =
                HttpClient(
                    MockEngine {
                        respond(
                            "[]",
                            status,
                            headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    }
                ) {
                    configureServiceClient()
                }
            try {
                val client = ServiceApiClient("https://service.invalid", { null }, http)
                assertFailsWith<ResponseException> { client.listRecords("", 50) }
            } finally {
                http.close()
            }
        }
    }

    @Test
    fun absentTokenAndMalformedSuccessfulJson() = runTest {
        val http =
            HttpClient(
                MockEngine {
                    assertNull(it.headers[HttpHeaders.Authorization])
                    respond(
                        "not-json",
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            ) {
                configureServiceClient()
            }
        try {
            val client = ServiceApiClient("https://service.invalid", { null }, http)
            assertFailsWith<JsonConvertException> { client.listRecords("", 50) }
        } finally {
            http.close()
        }
    }
}
