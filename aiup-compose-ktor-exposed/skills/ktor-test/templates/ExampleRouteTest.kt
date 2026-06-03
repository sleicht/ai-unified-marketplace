package com.example.app.modules.patient.infrastructure.rest

import com.example.app.configureRouting
import com.example.app.infrastructure.plugins.configureSerialization
import com.example.app.modules.patient.domain.model.Patient
import com.example.app.modules.patient.domain.repository.PatientRepository
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

    private val samplePatient =
        Patient(
            id = 1L,
            partnerContractNumber = "P-1001",
            lastName = "Muster",
            firstName = "Max",
            active = true,
        )

    private fun fakePatientRepository(patients: List<Patient> = listOf(samplePatient)) =
        object : PatientRepository {
            override suspend fun create(patient: Patient) = patient

            override suspend fun update(patient: Patient) = patient

            override suspend fun findById(id: Long) = patients.find { it.id == id }

            override suspend fun findAll(limit: Int) = patients.take(limit)
        }

    private fun ApplicationTestBuilder.configureTestApp(
        patientRepo: PatientRepository = fakePatientRepository(),
    ) {
        install(Koin) { modules(module { single<PatientRepository> { patientRepo } }) }
        application {
            configureSerialization()
            configureRouting()
        }
    }

    private fun employeeToken(): String = "test-token"

    @Test
    fun `GET patients returns list`() = testApplication {
        configureTestApp()

        client
            .get("/api/v1/patients") {
                header(HttpHeaders.Authorization, "Bearer ${employeeToken()}")
            }
            .apply {
                assertEquals(HttpStatusCode.OK, status)
                val arr = Json.parseToJsonElement(bodyAsText()).jsonArray
                assertEquals(1, arr.size)
                assertEquals("P-1001", arr[0].jsonObject["partnerContractNumber"]!!.jsonPrimitive.content)
            }
    }

    @Test
    fun `GET patient by id returns 404 when not found`() = testApplication {
        configureTestApp()

        client
            .get("/api/v1/patients/999") {
                header(HttpHeaders.Authorization, "Bearer ${employeeToken()}")
            }
            .apply { assertEquals(HttpStatusCode.NotFound, status) }
    }
}
