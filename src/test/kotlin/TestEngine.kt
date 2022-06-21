import com.turbomates.testsupport.exposed.Config
import com.turbomates.testsupport.exposed.rollbackTransaction
import com.turbomates.testsupport.exposed.testDatabase
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction

fun integrationTest(test: suspend ApplicationTestBuilder.() -> Unit): Unit = rollbackTransaction {
    applicationTest(test)
}

@Suppress("unused")
fun Transaction.applicationTest(test: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
    initDatabaseConfig()
    configureTestApplication(testDatabase)
    test()
}

@Suppress("LongMethod", "UnnecessaryOptInAnnotation", "ComplexMethod")
fun ApplicationTestBuilder.configureTestApplication(database: Database) {
    // Here you should reuse configuration from main entrypoint
    install(ContentNegotiation) {
        json()
    }
    routing {
        get("/api/users/{id}") {
            call.respond(UserView())
        }
        get("/api/users") {
            call.respond(listOf(UserView(), UserView("username2", 4)))
        }
    }
}

@Serializable
data class UserView(val name: String = "username", val rating: Int = 5, val isActive: Boolean = true)

private fun initDatabaseConfig() {
    Config.databaseUrl = "jdbc:h2:mem:test"
    Config.driver = "jdbc:h2:mem:test"
    Config.user = "jdbc:h2:mem:test"
    Config.password = "jdbc:h2:mem:test"
}

internal val json = Json { encodeDefaults = true }
