import io.vertx.core.Future

// So, we have Users and a service to get, add, and remove them.
data class User(val id: String, val fname: String, val lname: String)

/**
 * Notice that we’re doing asynchronous programming, so we can’t return directly a User or Unit.
 * Instead, we must provide some kind of callback or return a Future<T> result that allows you listen to success / fail operation events.
 */
interface UserService{
    fun getUser(id: String): Future<User>
    fun addUser(user: User): Future<Unit>
    fun remUser(id: String): Future<Unit>
}