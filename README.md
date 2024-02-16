![](https://turbomates.com/wp-content/uploads/2019/11/logo-e1573642672476.png)
[![Project Status: WIP – Initial development is in progress, but there has not yet been a stable, usable release suitable for the public.](https://www.repostatus.org/badges/latest/wip.svg)](https://www.repostatus.org/#wip)
![Build](https://github.com/turbomates/kotlin-test/actions/workflows/tests.yml/badge.svg)
![Detekt](https://github.com/turbomates/kotlin-test/actions/workflows/reviewdog.yml/badge.svg)

# Test Support
Provides infrastructure for Ktor + Exposed (mostly DAO option) based applications for creating elegant tests
Support for:
* Database (Exposed)
* Request building
* Response utils and assertions

```kotlin
@Test
fun `test`() = integrationTest {
        val user = UserMother.hasUser().build()

        handleGet("/api/users/${user.id}") {
        }.assert { // HTTP 200 is checked here
            mapTo<UserView>() shouldBe UserView()
        }
    }
```

# Test database usage
Either create testDatabase property with your DBMS credentials or just change the [Config](src/main/kotlin/com/turbomates/testsupport/exposed/TestTransactionManager.kt)
```kotlin
internal val testDatabase by lazy {
    Database.connect(
        Config.databaseUrl,
        user = Config.user,
        password = Config.password,
        driver = Config.driver,
        databaseConfig = DatabaseConfig { useNestedTransactions = true },
        manager = { database ->
            TransactionManager(
                database,
                Connection.TRANSACTION_READ_COMMITTED,
                DEFAULT_REPETITION_ATTEMPTS
            )
        }
    )
}
```
Use rollbackTransaction lambda in you tests
```kotlin
fun <T> rollbackTransaction(db: Database = testDatabase, statement: Transaction.() -> T): T {
    return transaction(db) { val result = statement(); rollback(); result }
}
```
You can [find](src/test/kotlin/Database.kt) examples in the tests of this project
