package example.server.modules.record.infrastructure.persistence

import example.server.modules.record.application.TransactionRunner
import example.server.modules.record.domain.model.Record
import example.server.modules.record.domain.repository.RecordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update

object RecordTable : Table("record") {
    val id = long("id").autoIncrement()
    val externalReference = varchar("external_reference", 50)
    val category = varchar("category", 50)
    val displayName = varchar("display_name", 100)
    val active = bool("active").default(true)
    val createdAt = timestampWithTimeZone("created_at").databaseGenerated()
    val updatedAt = timestampWithTimeZone("updated_at").databaseGenerated()
    override val primaryKey = PrimaryKey(id)
}

class JdbcTransactionRunner(private val database: Database) : TransactionRunner {
    override suspend fun <T> run(block: suspend () -> T): T =
        withContext(Dispatchers.IO) { suspendTransaction(db = database) { block() } }
}

class ExposedRecordRepository(private val transactions: TransactionRunner) : RecordRepository {
    override suspend fun create(record: Record): Record =
        transactions.run {
            val id = RecordTable.insert { it.assign(record) }[RecordTable.id]
            checkNotNull(findById(id))
        }

    override suspend fun update(record: Record): Record =
        transactions.run {
            val id = requireNotNull(record.id)
            check(RecordTable.update({ RecordTable.id eq id }) { it.assign(record) } == 1)
            checkNotNull(findById(id))
        }

    override suspend fun findById(id: Long): Record? =
        transactions.run {
            RecordTable.selectAll().where { RecordTable.id eq id }.singleOrNull()?.toRecord()
        }

    override suspend fun findAll(query: String, limit: Int): List<Record> =
        transactions.run {
            require(limit in 1..100)
            // Literal substring matching; escape SQL LIKE metacharacters.
            val escaped = query.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
            RecordTable.selectAll()
                .where { RecordTable.displayName like "%$escaped%" }
                .orderBy(RecordTable.id, SortOrder.ASC)
                .limit(limit)
                .map { it.toRecord() }
        }

    private fun UpdateBuilder<*>.assign(record: Record) {
        this[RecordTable.externalReference] = record.externalReference
        this[RecordTable.category] = record.category
        this[RecordTable.displayName] = record.displayName
        this[RecordTable.active] = record.active
    }

    private fun ResultRow.toRecord() =
        Record(
            id = this[RecordTable.id],
            externalReference = this[RecordTable.externalReference],
            category = this[RecordTable.category],
            displayName = this[RecordTable.displayName],
            active = this[RecordTable.active],
            createdAt = this[RecordTable.createdAt],
            updatedAt = this[RecordTable.updatedAt],
        )
}
