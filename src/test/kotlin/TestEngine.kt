import com.turbomates.testsupport.exposed.Config
import com.turbomates.testsupport.exposed.TestTransaction
import com.turbomates.testsupport.exposed.rollbackTransaction
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

fun integrationTest(test: suspend context(TestTransaction, ApplicationTestBuilder) (ApplicationTestBuilder) -> Unit) = rollbackTransaction {
    this.applicationTest(test)
}

@Suppress("unused")
fun TestTransaction.applicationTest(test: suspend context(TestTransaction, ApplicationTestBuilder) (ApplicationTestBuilder) -> Unit) = testApplication {
    initDatabaseConfig()
    configureTestApplication()
    test(this@TestTransaction, this, this)
}

@Suppress("LongMethod", "UnnecessaryOptInAnnotation", "ComplexMethod")
fun ApplicationTestBuilder.configureTestApplication() {
    // Here you should reuse configuration from main entrypoint
    install(ContentNegotiation) {
        json()
        json(json, ContentType.Application.Json)
    }
    routing {
        get("/api/users/{id}") {
            call.respond(UserView())
        }
        get("/api/users") {
            call.respond(listOf(UserView(), UserView("username2", 4)))
        }
        post("/api/users") {
            call.respond("ok")
        }
        patch("/api/users/{id}") {
            call.respond("ok")
        }
        put("/api/users/{id}") {
            call.respond("ok")
        }
        delete("/api/users") {
            call.respond("ok")
        }
    }
}

@Serializable
data class UserView(val name: String = "username", val rating: Int = 5, val isActive: Boolean = true)

private fun initDatabaseConfig() {
    Config.databaseUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false"
    Config.driver = "jdbc:h2:mem:test"
    Config.user = "jdbc:h2:mem:test"
    Config.password = "jdbc:h2:mem:test"
}

internal val json = Json { encodeDefaults = true }
