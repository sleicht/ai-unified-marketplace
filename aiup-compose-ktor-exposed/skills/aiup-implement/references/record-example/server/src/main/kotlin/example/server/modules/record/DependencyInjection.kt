package example.server.modules.record

import example.server.modules.record.application.RecordService
import example.server.modules.record.application.TransactionRunner
import example.server.modules.record.domain.repository.RecordRepository
import example.server.modules.record.infrastructure.persistence.ExposedRecordRepository
import example.server.modules.record.infrastructure.persistence.JdbcTransactionRunner
import org.koin.dsl.module

val recordModule = module {
    single<TransactionRunner> { JdbcTransactionRunner(get()) }
    single<RecordRepository> { ExposedRecordRepository(get()) }
    single { RecordService(get(), get()) }
}
