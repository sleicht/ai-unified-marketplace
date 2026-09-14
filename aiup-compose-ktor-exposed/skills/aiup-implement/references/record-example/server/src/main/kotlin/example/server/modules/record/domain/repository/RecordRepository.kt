package example.server.modules.record.domain.repository

import example.server.modules.record.domain.model.Record

interface RecordRepository {
    suspend fun create(record: Record): Record

    suspend fun update(record: Record): Record

    suspend fun findById(id: Long): Record?

    suspend fun findAll(query: String, limit: Int): List<Record>
}
