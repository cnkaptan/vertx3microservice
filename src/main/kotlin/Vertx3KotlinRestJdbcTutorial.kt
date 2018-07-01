import io.vertx.core.Vertx
import io.vertx.ext.web.Router

/**
 * First, we get a Vertx instance, and create an HttpServer from it.
 * The server is not yet started, so we can keep configuring it to match our needs.
 * In this case, just handle GET / and return a classical Hello world!.
 */
object Vertx3KotlinRestJdbcTutorial {
    @JvmStatic
    fun main(args: Array<String>) {
        val vertx = Vertx.vertx()
        val server = vertx.createHttpServer()
        val port = 9000
        val router = Router.router(vertx)

        router.get("/").handler { it.response().end("Hello world!") }
        server.requestHandler { router.accept(it) }.listen(port){
            if (it.succeeded()) println("Server listening at $port")
            else println(it.cause())
        }
    }
}