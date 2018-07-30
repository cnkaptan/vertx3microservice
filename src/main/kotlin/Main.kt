import io.vertx.core.Vertx
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.experimental.launch

fun main(vararg args: String) {
    val vertx = Vertx.vertx()

    /**
     * Now we need to start this verticle. There are different ways of doing that,
     * but the simplest way is to pass the instance of this class to the deployVerticle() method:
     */
    vertx.deployVerticle(ServerVerticle())
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