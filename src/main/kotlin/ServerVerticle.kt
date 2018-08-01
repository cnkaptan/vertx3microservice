import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.Router.router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.CoroutineVerticle

/**
 * Now come across a problem, though. Our code resides in the Main.kt file, which grows bigger and bigger.
 * We can start splitting it by using verticles.
 *
 * You can think of a verticle as a lightweight actor
 */

private val insert = """insert into cats (name, age)
            |values (?, ?::integer) RETURNING *""".trimMargin()

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
        val dbClient = getDbClient()

        /**
         * This will tell Vert.x to parse the request body into JSON for any request.
         */
        router.route("/*").handler(BodyHandler.create())

        router.get("/alive").asyncHandler {
            val dbAlive = dbClient.query("select true as alive")
            // Some response comes here
            // We now can use any suspending function in this context
            val json = json {
                obj(
                        "alive" to true,
                        "db" to dbAlive.await()["rows"]
                )
            }
            it.respond(json.toString())
        }

        router.mountSubRouter("/api/v1",apiRouter())

        return router
    }

    private fun apiRouter(): Router{
        val router = Router.router(vertx)
        val db = getDbClient()

        router.post("/cats").asyncHandler { ctx ->
            // Some code of adding a cat comes here
            db.queryWithParams(insert, ctx.bodyAsJson.toCat()) {
                it.handle({
                    //We''l always have one result here, since it'' our row
                    ctx.respond(it.result().rows[0].toString(),201)
                },{
                    ctx.respond(status = 500)
                })
            }
        }

        router.get("/cats").asyncHandler { ctx ->
            // Code for getting all the cats
        }

        router.get("/cats/:id").asyncHandler {ctx ->
            // Fethces spesific cat
        }

        return router

    }

    // We also defined our own function that parses the request body, which is JsonObject,
    // to JsonArray, which is expected by the JDBCClient:
    private fun JsonObject.toCat() = JsonArray().apply {
        /**
         * Notice that we have two different versions of this here. One refers to the inner scope of the apply() function.
         * The other refers to the outer scope of the toCat() function. To refer to outer scopes, we use the @scopeName notation.
         */
        add(this@toCat.getString("name"))
        add(this@toCat.getInteger("age"))
    }
}