package example.server

import example.server.modules.record.domain.model.Record
import example.server.modules.record.domain.repository.RecordRepository
import example.server.modules.record.infrastructure.rest.RecordUser
import example.server.modules.record.infrastructure.rest.configureRecordApi
import example.shared.RecordListItem
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json

class RecordRoutesTest {
    // Stateful fake; writes must persist to support subsequent read assertions.
    private class FakeRepository : RecordRepository {
        private val records = mutableMapOf(1L to Record("REC-1", "Standard", "Ada", id = 1))
        private var nextId = 2L

        override suspend fun create(record: Record): Record =
            record.copy(id = nextId++).also { records[requireNotNull(it.id)] = it }

        override suspend fun update(record: Record): Record =
            record.also { check(records.replace(requireNotNull(it.id), it) != null) }

        override suspend fun findById(id: Long) = records[id]

        override suspend fun findAll(query: String, limit: Int) =
            records.values.filter { query in it.displayName }.take(limit)
    }

    @Test
    fun listAndInvalidRequestsUseTheProductionRoutes() = testApplication {
        application {
            configureRecordApi(FakeRepository()) { token ->
                when (token) {
                    "test-reader" -> RecordUser(true)
                    "test-other-role" -> RecordUser(false)
                    else -> null
                }
            }
        }
        val response = client.get("/api/v1/records?query=Ada&limit=1") { bearerAuth("test-reader") }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(
            listOf(RecordListItem(1, "REC-1", "Ada", "Standard")),
            Json.decodeFromString<List<RecordListItem>>(response.bodyAsText()),
        )
        for (path in listOf("/api/v1/records", "/api/v1/records/1")) {
            assertEquals(HttpStatusCode.Unauthorized, client.get(path).status)
            assertEquals(
                HttpStatusCode.Forbidden,
                client.get(path) { bearerAuth("test-other-role") }.status,
            )
        }
        for (path in
            listOf(
                "/api/v1/records/not-a-number",
                "/api/v1/records/0",
                "/api/v1/records?limit=-1",
                "/api/v1/records?limit=abc",
            )) {
            assertEquals(
                HttpStatusCode.BadRequest,
                client.get(path) { bearerAuth("test-reader") }.status,
            )
        }
        assertEquals(
            HttpStatusCode.NotFound,
            client.get("/api/v1/records/999") { bearerAuth("test-reader") }.status,
        )
    }
}
