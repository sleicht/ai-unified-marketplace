package com.example.app.modules.record.infrastructure.rest

import com.example.app.configureRouting
import com.example.app.infrastructure.plugins.configureSerialization
import com.example.app.modules.record.domain.model.Record
import com.example.app.modules.record.domain.repository.RecordRepository
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

class ExampleRouteTest {

    private val sampleRecord =
        Record(
            id = 1L,
            externalReference = "REC-1001",
            category = "Standard",
            displayName = "Example",
            active = true,
        )

    private fun fakeRecordRepository(records: List<Record> = listOf(sampleRecord)) =
        object : RecordRepository {
            override suspend fun create(record: Record) = record

            override suspend fun update(record: Record) = record

            override suspend fun findById(id: Long) = records.find { it.id == id }

            override suspend fun findAll(limit: Int) = records.take(limit)
        }

    private fun ApplicationTestBuilder.configureTestApp(
        recordRepo: RecordRepository = fakeRecordRepository(),
    ) {
        install(Koin) { modules(module { single<RecordRepository> { recordRepo } }) }
        application {
            configureSerialization()
            configureRouting()
        }
    }

    private fun employeeToken(): String = "test-token"

    @Test
    fun `GET records returns list`() = testApplication {
        configureTestApp()

        client
            .get("/api/v1/records") {
                header(HttpHeaders.Authorization, "Bearer ${employeeToken()}")
            }
            .apply {
                assertEquals(HttpStatusCode.OK, status)
                val arr = Json.parseToJsonElement(bodyAsText()).jsonArray
                assertEquals(1, arr.size)
                assertEquals("REC-1001", arr[0].jsonObject["externalReference"]!!.jsonPrimitive.content)
            }
    }

    @Test
    fun `GET record by id returns 404 when not found`() = testApplication {
        configureTestApp()

        client
            .get("/api/v1/records/999") {
                header(HttpHeaders.Authorization, "Bearer ${employeeToken()}")
            }
            .apply { assertEquals(HttpStatusCode.NotFound, status) }
    }
}
