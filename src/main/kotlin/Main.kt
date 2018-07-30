import io.vertx.core.Vertx
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.experimental.launch

fun main(vararg args: String) {
    val vertx = Vertx.vertx()
    val router = Router.router(vertx)

//    vertx.createHttpServer().requestHandler { req ->
//        req.response().end("OK")
//    }.listen(8080)

    router.get("/alive").asyncHandler {
        // Some response comes here
        // We now can use any suspending function in this context
        val json = """
            {alive: true}
            """.trim()
        it.response().end(json)
    }

    /**
     *  Now connect your router to the server.
     *  You can do that by replacing the previous server instantiation with the following line:
     */
    vertx.createHttpServer().requestHandler(router::accept).listen(8080)
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