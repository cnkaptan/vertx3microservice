import com.google.gson.Gson
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import kotlin.reflect.KClass

/**
 * First, we get a Vertx instance, and create an HttpServer from it.
 * The server is not yet started, so we can keep configuring it to match our needs.
 * In this case, just handle GET / and return a classical Hello world!.
 */
object Vertx3KotlinRestJdbcTutorial {
    @JvmStatic
    fun main(args: Array<String>) {
        val vertx = Vertx.vertx()
        val port = 9000
        val userService = MemoryUserService()

        vertx.createHttpServer().restAPI(vertx) {
            get("/:userId") {
                send(userService.getUser(param("userId")))
            }

            post("/") {
                send(userService.addUser(bodyAs(User::class)))
            }
            delete("/:userId") {
                send(userService.remUser(param("userId")))
            }
        }.listen(port){
            if (it.succeeded()) println("Server listening at $port")
            else println(it.cause().toString())
        }

//        router.get("/:userId").handler { ctx ->
//            val userId = ctx.request().getParam("userId")
//            jsonResponse(ctx, userService.getUser(userId))
//        }
//
//        router.post("/").handler { ctx ->
//            val user = jsonRequest<User>(ctx, User::class)
//            jsonResponse(ctx, userService.addUser(user))
//        }
//
//        router.delete("/:userId").handler {
//            val userId = it.request().getParam("userId")
//            jsonResponse(it,userService.remUser(userId))
//        }

//        server.requestHandler { router.accept(it) }.listen(port) {
//            if (it.succeeded()) println("Server listening at $port")
//            else println(it.cause())
//        }
    }


}