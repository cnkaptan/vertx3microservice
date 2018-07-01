import io.vertx.core.Future

/**
 * Notice that we’re doing asynchronous programming, so we can’t return directly a User or Unit.
 * Instead, we must provide some kind of callback or return a Future<T> result that allows you listen to success / fail operation events.
 */
class MemoryUserService(): UserService {
    val _users = HashMap<String, User>()
    init {
        addUser(User("1","user1_fname","user1_lname"))
    }

    override fun getUser(id: String): Future<User> {
        return if (_users.containsKey(id)) Future.succeededFuture(_users[id])
        else Future.failedFuture(IllegalArgumentException("Unknown user $id"))
    }

    override fun addUser(user: User): Future<Unit> {
        _users.put(user.id, user)
        return Future.succeededFuture()
    }

    override fun remUser(id: String): Future<Unit> {
        _users.remove(id)
        return Future.succeededFuture()
    }

}