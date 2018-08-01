import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.launch

fun main(vararg args: String) {
    val vertx = Vertx.vertx()

    /**
     * Now we need to start this verticle. There are different ways of doing that,
     * but the simplest way is to pass the instance of this class to the deployVerticle() method:
     */
    vertx.deployVerticle(ServerVerticle())
    vertx.deployVerticle(CatVerticle())
}


/**
 * Router lets you specify handlers for different HTTP methods and URLs.
 * But, by default, it doesn't support coroutines. Let's fix that by creating an extension function:
 */
fun Route.asyncHandler(fn: suspend (RoutingContext) -> Unit) {
    handler { ctx ->
        launch(ctx.vertx().dispatcher()) {
            try {
                fn(ctx)
            } catch (e: Exception) {
                ctx.fail(e)
            }
        }
    }
}

fun RoutingContext.respond(responseBody: String = "", status: Int = 200) {
    this.response()
            .setStatusCode(200)
            .end(responseBody)
}

// We now will create JDBCClient by using this configuration code:
fun CoroutineVerticle.getDbClient(): JDBCClient {
    val postgreSQLClientConfig = JsonObject(
            "url" to "jdbc:postgresql://${Config.Db.host}:5432/${Config.Db.database}",
            "username" to Config.Db.username,
            "password" to Config.Db.password)

    return JDBCClient.createShared(vertx, postgreSQLClientConfig)
}

// o simplify working with the JDBCClient, we'll add a method called query() to it:
fun JDBCClient.query(q: String, vararg params: Any): Deferred<JsonObject> {
    val deferred = CompletableDeferred<JsonObject>()
    this.getConnection { conn ->
        conn.handle({
            result().queryWithParams(q, params.toJsonArray()) { res ->
                res.handle({
                    deferred.complete(res.result().toJson())
                }, {
                    deferred.completeExceptionally(res.cause())
                })
            }
        }, {
            deferred.completeExceptionally(conn.cause())
        })
    }
    return deferred
}

// We'll also add the toJsonArray() method since that's what our JDBCClient works with:
private fun <T> Array<T>.toJsonArray(): JsonArray {
    val json = JsonArray()
    for (e in this) {
        json.add(e)
    }
    return json
}

// Note here how Kotlin generics are being used to simplify the conversion while staying type-safe.

//And we'll add a handle() function, which will provide us with a simple API to handle asynchronous errors:
inline fun <T> AsyncResult<T>.handle(success: AsyncResult<T>.() -> Unit, failure: () -> Unit) {
    if (this.succeeded()) {
        success()
    } else {
        this.cause().printStackTrace()
        failure()
    }
}