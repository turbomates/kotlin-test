package databuilders

import com.turbomates.testsupport.Builder
import com.turbomates.testsupport.DbAssertive
import com.turbomates.testsupport.Fixture
import com.turbomates.testsupport.RequestSerializable
import com.turbomates.testsupport.ResponseSerializable
import com.turbomates.testsupport.exposed.testDatabase
import io.kotest.matchers.collections.shouldNotBeEmpty
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID
import kotlin.properties.Delegates
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object UserMother {
    fun one(block: UserBuilder.() -> Unit = {}) = UserBuilder().apply {
        block()
        name = "username"
        rating = 5
        isActive = true
    }

    fun deactivatedUser(block: UserBuilder.() -> Unit = {}) = UserBuilder().apply {
        block()
        name = "username"
        rating = 5
        isActive = false
    }
}

class UserBuilder :
    Builder<User>(),
    RequestSerializable<JsonObject>,
    ResponseSerializable<JsonObject>,
    DbAssertive<User> {
    var name by Delegates.notNull<String>()
    var rating by Delegates.notNull<Int>()
    var isActive by Delegates.notNull<Boolean>()
    lateinit var id: UUID

    override fun build(): User {
        val user = User.create(name, rating).also { id = it.id.value }
        if (!isActive) user.deactivate()
        return user
    }

    override fun toRequest(): JsonObject {
        return buildJsonObject {
            put("name", name)
            put("rating", rating)
            put("isActive", isActive)
        }
    }

    override fun toResponse(): JsonObject {
        return buildJsonObject {
            put("name", name)
            put("rating", rating)
            put("isActive", isActive)
        }
    }

    override fun seeInDb() {
        UserTable
            .selectAll()
            .where {
                UserTable.name eq name and
                        (UserTable.rating eq rating) and
                        (UserTable.isActive eq isActive)
            }
            .shouldNotBeEmpty()
    }
}

class User constructor(id: EntityID<UUID>) : UUIDEntity(id) {
    private var name by UserTable.name
    private var rating by UserTable.rating
    private var isActive by UserTable.isActive

    fun deactivate() {
        this.isActive = false
    }

    companion object : UUIDEntityClass<User>(UserTable) {
        fun create(name: String, rating: Int): User {
            return User.new {
                this.name = name
                this.rating = rating
                this.isActive = true
            }
        }
    }
}

object UserTable : UUIDTable("users") {
    val name = text("name")
    val rating = integer("rating")
    val isActive = bool("active").default(true)
}

object UserFixture : Fixture<UserBuilder> {
    override fun load(block: UserBuilder.() -> Unit): UserBuilder {
        SchemaUtils.create(UserTable)
        val builder = UserMother.one(block)
        builder.build()
        return builder
    }

    override fun rollback() {
        transaction(testDatabase) {
            UserTable.deleteAll()
        }
    }
}
