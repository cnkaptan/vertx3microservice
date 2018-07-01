import com.google.gson.Gson
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import kotlin.reflect.KClass

/**
 * So, we want to get rid of boilerplate code such router., .handler { ctx -> , and ctx.request().getParam().
 * This code just obfuscate what we try to express in the REST API definitions.
 * This is particularly evident when there’s a bunch of business packages with lots of REST endpoints each.
 * Then, the simpler the definitions, the better the maintenance tasks.
 *
 * How do we get that cleaner and much more expressive code? Of course, with Kotlin sugar for DSL definitions.
 * You can find the key idea at Type Safe Builders in the main Kotlin site.
 * We use those ideas and define the following extension methods:
 */
val GSON = Gson()

fun Router.get(path: String, rctx: RoutingContext.() -> Unit) = get(path).handler { it.rctx() }
fun Router.post(path: String, rctx: RoutingContext.()-> Unit) = post(path).handler{it.rctx()}
fun Router.put(path: String, rctx: RoutingContext.() -> Unit) = put(path).handler { it.rctx() }
fun Router.delete(path: String, rctx: RoutingContext.() -> Unit) = delete(path).handler { it.rctx() }

fun RoutingContext.param(name: String): String = request().getParam(name)

fun <T> RoutingContext.bodyAs(clazz: KClass<out Any>): T = GSON.fromJson(bodyAsString,clazz.java) as T

fun <T> RoutingContext.send(future: Future<T>){
    future.setHandler {
        if (it.succeeded()){
            val res = if(it.result() == null) "" else GSON.toJson(it.result())
            response().end(res)
        }else{
            response().setStatusCode(500).end(it.cause().toString())
        }
    }
}

fun HttpServer.restAPI(vertx: Vertx, body: Router.() -> Unit): HttpServer{
    val router = Router.router(vertx)
    router.route().handler(BodyHandler.create()) // Required for RoutingContext.bodyAsString
    router.body()
    requestHandler { router.accept(it) }
    return this
}