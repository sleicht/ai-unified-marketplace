package example.server.modules.record.application

import example.server.modules.record.domain.model.Record
import example.server.modules.record.domain.repository.RecordRepository

interface TransactionRunner {
    suspend fun <T> run(block: suspend () -> T): T
}

class RecordService(
    private val repository: RecordRepository,
    private val transactions: TransactionRunner,
) {
    suspend fun createPair(first: Record, second: Record): List<Record> =
        transactions.run { listOf(repository.create(first), repository.create(second)) }
}
