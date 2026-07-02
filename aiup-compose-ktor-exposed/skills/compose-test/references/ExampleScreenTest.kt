package com.example.app.ui.api

import com.example.app.shared.PatientListItem
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
    fun `listPatients sends bearer token and decodes response`() = runTest {
        lateinit var request: HttpRequestData
        val httpClient =
            HttpClient(
                MockEngine { capturedRequest ->
                    request = capturedRequest
                    respond(
                        content =
                            Json.encodeToString(
                                listOf(
                                    PatientListItem(
                                        id = 1,
                                        partnerContractNumber = "P-1001",
                                        firstName = "Ada",
                                        lastName = "Lovelace",
                                        active = true,
                                    )
                                )
                            ),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            ) { install(ContentNegotiation) { json() } }

        val client = ServiceApiClient(baseUrl = "http://localhost:5600", httpClient = httpClient)

        val patients = client.listPatients(limit = 100)

        assertEquals("Bearer $DEFAULT_POC_EMPLOYEE_TOKEN", request.headers[HttpHeaders.Authorization])
        assertEquals("/api/v1/patients", request.url.encodedPath)
        assertEquals("100", request.url.parameters["limit"])
        assertEquals(1, patients.size)
        assertEquals("Ada", patients.single().firstName)
    }
}
