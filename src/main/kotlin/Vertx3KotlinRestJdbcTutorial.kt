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
    val gson = Gson()
    @JvmStatic
    fun main(args: Array<String>) {
        val vertx = Vertx.vertx()
        val server = vertx.createHttpServer()
        val port = 9000
        val router = Router.router(vertx)
        router.route().handler(BodyHandler.create())
        val userService = MemoryUserService()

        router.get("/:userId").handler { ctx ->
            val userId = ctx.request().getParam("userId")
            jsonResponse(ctx, userService.getUser(userId))
        }

        router.post("/").handler { ctx ->
            val user = jsonRequest<User>(ctx, User::class)
            jsonResponse(ctx, userService.addUser(user))
        }

        router.delete("/:userId").handler {
            val userId = it.request().getParam("userId")
            jsonResponse(it,userService.remUser(userId))
        }

        server.requestHandler { router.accept(it) }.listen(port){
            if (it.succeeded()) println("Server listening at $port")
            else println(it.cause())
        }
    }

    fun <T> jsonRequest(ctx: RoutingContext, clazz: KClass<out Any>): T =
            gson.fromJson(ctx.bodyAsString, clazz.java) as T

    fun <T> jsonResponse(ctx: RoutingContext, future: Future<T>){
        future.setHandler {
            if (it.succeeded()){
                val res = if (it.result() == null) "" else gson.toJson(it.result())
                ctx.response().end(res)
            }else{
                ctx.response().setStatusCode(500).end(it.cause().toString())
            }
        }
    }
}