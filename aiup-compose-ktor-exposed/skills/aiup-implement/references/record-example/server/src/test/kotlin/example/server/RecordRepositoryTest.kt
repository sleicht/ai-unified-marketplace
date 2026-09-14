package example.server

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import example.server.modules.record.application.RecordService
import example.server.modules.record.domain.model.Record
import example.server.modules.record.infrastructure.persistence.ExposedRecordRepository
import example.server.modules.record.infrastructure.persistence.JdbcTransactionRunner
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer

@Tag("postgres")
class RecordRepositoryTest {
    @Test
    fun migrationsRoundTripAndRollback() = runBlocking {
        PostgreSQLContainer("postgres:17-alpine").use { postgres ->
            postgres.start()
            HikariDataSource(
                    HikariConfig().apply {
                        jdbcUrl = postgres.jdbcUrl
                        username = postgres.username
                        password = postgres.password
                        maximumPoolSize = 2
                    }
                )
                .use { ds ->
                    Flyway.configure()
                        .dataSource(ds)
                        .schemas("fresh")
                        .defaultSchema("fresh")
                        .load()
                        .migrate()
                    Flyway.configure().dataSource(ds).target("1").load().migrate()
                    ds.connection.use { connection ->
                        connection.createStatement().use {
                            it.executeUpdate(
                                "INSERT INTO record(external_reference, category, display_name) VALUES ('seed', 'Standard', 'Seed')"
                            )
                        }
                    }
                    Flyway.configure().dataSource(ds).load().migrate()
                    val runner = JdbcTransactionRunner(Database.connect(ds))
                    val repository = ExposedRecordRepository(runner)
                    val created = repository.create(Record("REC-1", "Standard", "Ada"))
                    assertNotNull(created.createdAt)
                    assertNotNull(created.updatedAt)
                    assertEquals(created, repository.findById(requireNotNull(created.id)))
                    val updated =
                        repository.update(
                            created.copy(displayName = "Ada Lovelace", active = false)
                        )
                    assertEquals("Ada Lovelace", updated.displayName)
                    assertEquals(false, updated.active)
                    assertEquals(created.createdAt, updated.createdAt)
                    assertTrue(updated.updatedAt!! > created.updatedAt)
                    assertEquals(listOf(updated), repository.findAll("Lovelace", 10))
                    assertNotNull(repository.findAll("Seed", 10).single().createdAt)
                    val service = RecordService(repository, runner)
                    assertFailsWith<Exception> {
                        service.createPair(
                            Record("ROLLBACK", "Standard", "First"),
                            Record("REC-1", "Standard", "Duplicate"),
                        )
                    }
                    assertTrue(repository.findAll("First", 10).isEmpty())
                    for (value in listOf<String?>(null, "", "   ", "\t", "x".repeat(51))) {
                        ds.connection.use { connection ->
                            connection
                                .prepareStatement(
                                    "INSERT INTO record(external_reference, category, display_name) VALUES (?, 'Standard', 'Invalid')"
                                )
                                .use {
                                    it.setString(1, value)
                                    assertFailsWith<java.sql.SQLException> { it.executeUpdate() }
                                }
                        }
                    }
                    assertFailsWith<IllegalArgumentException> { Record(" ", "Standard", "Invalid") }
                    assertFailsWith<IllegalArgumentException> {
                        Record("x".repeat(51), "Standard", "Invalid")
                    }
                    // This test owns the whole disposable database; closing it removes all rows.
                }
        }
    }
}
