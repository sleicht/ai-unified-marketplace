package example.server

import example.server.di.appModule
import kotlin.test.Test
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.test.verify.verify

@OptIn(org.koin.core.annotation.KoinExperimentalAPI::class)
class DependencyInjectionTest {
    @Test
    fun completeGraphHasOnlyTheHostDatabaseAsAnExternalDependency() {
        appModule.verify(extraTypes = listOf(Database::class))
    }
}
