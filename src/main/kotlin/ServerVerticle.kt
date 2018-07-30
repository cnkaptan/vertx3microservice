import io.vertx.ext.web.Router
import io.vertx.ext.web.Router.router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.coroutines.CoroutineVerticle

/**
 * Now come across a problem, though. Our code resides in the Main.kt file, which grows bigger and bigger.
 * We can start splitting it by using verticles.
 *
 * You can think of a verticle as a lightweight actor
 */
class ServerVerticle: CoroutineVerticle(){

    override suspend fun start() {
        val router = router()

        /**
         *  Now connect your router to the server.
         *  You can do that by replacing the previous server instantiation with the following line:
         */
        vertx.createHttpServer().requestHandler(router::accept).listen(8080)
    }

    private fun router(): Router{
        val router = Router.router(vertx)
        // Our router code comes here now
        /**
         * This will tell Vert.x to parse the request body into JSON for any request.
         */
        router.route().handler(BodyHandler.create())


        router.get("/alive").asyncHandler {
            // Some response comes here
            // We now can use any suspending function in this context
            val json = """
            {alive: true}
            """.trim()
            it.response().end(json)
        }

        router.post("/api/v1/cats").asyncHandler { ctx ->
            // Some code of adding a cat comes here
        }

        router.get("/api/v1/cats").asyncHandler { ctx ->
            // Code for getting all the cats
        }

        router.get("/api/v1/cats/:id").asyncHandler {ctx ->
            // Fethces spesific cat
        }

        return router
    }
}