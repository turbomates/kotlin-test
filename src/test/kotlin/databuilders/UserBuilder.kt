package databuilders

import com.turbomates.testsupport.Builder
import com.turbomates.testsupport.DbAssertive
import com.turbomates.testsupport.RequestSerializable
import com.turbomates.testsupport.ResponseSerializable
import io.kotest.matchers.collections.shouldNotBeEmpty
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.util.UUID
import kotlin.properties.Delegates

object UserMother {
    fun hasUser(block: UserBuilder.() -> Unit = {}) = UserBuilder().apply {
        block()
        name = "username"
        rating = 5
        isActive = true
    }

    fun hasDeactivatedUser(block: UserBuilder.() -> Unit = {}) = UserBuilder().apply {
        block()
        name = "username"
        rating = 5
        isActive = false
    }
}

class UserBuilder :
    Builder<User>,
    RequestSerializable<UserBuilder, JsonElement>,
    ResponseSerializable<UserBuilder, JsonElement>,
    DbAssertive<User> {
    var name by Delegates.notNull<String>()
    var rating by Delegates.notNull<Int>()
    var isActive by Delegates.notNull<Boolean>()
    lateinit var id: UUID

    override fun build(): User {
        val user = User.create(name, rating)
        if (!isActive) user.deactivate()
        id = user.id.value
        return user
    }

    override fun toRequest(): JsonElement {
        return buildJsonObject {
            put("name", name)
            put("rating", rating)
            put("isActive", isActive)
        }
    }

    override fun toResponse(): JsonElement {
        return buildJsonObject {
            put("name", name)
            put("rating", rating)
            put("isActive", isActive)
        }
    }

    override fun seeInDb() {
        UserTable.select {
            UserTable.name eq name and
                (UserTable.rating eq rating) and
                (UserTable.isActive eq isActive)
        }.shouldNotBeEmpty()
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
