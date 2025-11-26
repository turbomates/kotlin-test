package com.turbomates.testsupport.response

import com.turbomates.testsupport.transaction
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction

context(transaction: JdbcTransaction)
suspend fun HttpResponse.assert(
    statusCode: HttpStatusCode = HttpStatusCode.OK,
    block: suspend context(JdbcTransaction, HttpResponse) () -> Unit = {}
) {
    transaction {
        withClue(bodyAsText()) {
            assertSoftly {
                status shouldBe statusCode
                block(transaction, contextOf<HttpResponse>())
            }
        }
    }
}

context(transaction: JdbcTransaction)
suspend fun HttpResponse.assertIsOk() = assert()

fun HttpResponse.containsHeader(header: String, value: Any) {
    this.headers[header] shouldContain value.toString()
}

suspend fun HttpResponse.contains(value: CharSequence) {
    this.bodyAsText() shouldContain value.toString()
}

suspend fun HttpResponse.notContains(value: CharSequence) {
    this.bodyAsText() shouldNotContain value.toString()
}
