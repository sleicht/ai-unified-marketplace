package example.server.di

import example.server.modules.record.recordModule
import org.koin.dsl.module

// Database is supplied by the host application at startup.
val appModule = module { includes(recordModule) }
