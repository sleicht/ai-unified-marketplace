package com.example.app.ui.api

import com.example.app.shared.RecordListItem
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ExampleApiClientTest {

    @Test
    fun `listRecords sends bearer token and decodes response`() = runTest {
        lateinit var request: HttpRequestData
        val httpClient =
            HttpClient(
                MockEngine { capturedRequest ->
                    request = capturedRequest
                    respond(
                        content =
                            Json.encodeToString(
                                listOf(
                                    RecordListItem(
                                        id = 1,
                                        externalReference = "REC-1001",
                                        displayName = "Example",
                                        category = "Record",
                                        active = true,
                                    )
                                )
                            ),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            ) { install(ContentNegotiation) { json() } }

        val client = ServiceApiClient(baseUrl = testBaseUrl, accessTokenProvider = FakeAccessTokenProvider("test-token"), httpClient = httpClient)

        val records = client.listRecords(limit = 100)

        assertEquals("Bearer test-token", request.headers[HttpHeaders.Authorization])
        assertEquals("/api/v1/records", request.url.encodedPath)
        assertEquals("100", request.url.parameters["limit"])
        assertEquals(1, records.size)
        assertEquals("Example", records.single().displayName)
    }
}
