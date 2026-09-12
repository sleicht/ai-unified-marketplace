package example.server.modules.record.infrastructure.rest

import example.server.modules.record.domain.model.Record
import example.server.modules.record.domain.repository.RecordRepository
import example.shared.RecordListItem
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.bearer
import io.ktor.server.auth.principal
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

data class RecordUser(val canRead: Boolean)

// Supply the existing project's verifier; never replace it with fixture token comparisons.
fun Application.configureRecordApi(
    repository: RecordRepository,
    verifyToken: suspend (String) -> RecordUser?,
) {
    install(ContentNegotiation) { json() }
    install(Authentication) { bearer("records") { authenticate { verifyToken(it.token) } } }
    routing {
        authenticate("records") {
            route("/api/v1/records") {
                get {
                    if (call.principal<RecordUser>()?.canRead != true) {
                        call.respond(HttpStatusCode.Forbidden)
                        return@get
                    }
                    val rawLimit = call.request.queryParameters["limit"]
                    val limit = if (rawLimit == null) 50 else rawLimit.toIntOrNull()
                    if (limit == null || limit !in 1..100) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@get
                    }
                    val query = call.request.queryParameters["query"].orEmpty()
                    call.respond(repository.findAll(query, limit).map { it.toListItem() })
                }
                get("/{id}") {
                    if (call.principal<RecordUser>()?.canRead != true) {
                        call.respond(HttpStatusCode.Forbidden)
                        return@get
                    }
                    val id = call.parameters["id"]?.toLongOrNull()
                    if (id == null || id <= 0) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@get
                    }
                    val record = repository.findById(id)
                    if (record == null) call.respond(HttpStatusCode.NotFound)
                    else call.respond(record.toListItem())
                }
            }
        }
    }
}

private fun Record.toListItem() =
    RecordListItem(requireNotNull(id), externalReference, displayName, category, active)
