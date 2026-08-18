package dev.arsngrobg.paralax

import io.javalin.Javalin
import io.javalin.config.JavalinConfig
import io.javalin.http.Handler
import io.javalin.http.staticfiles.Location

const val PARALAX_HOST: String = "localhost"
const val PARALAX_PORT: Int    = 7070

fun startServer() {
    val app = Javalin.create { config: JavalinConfig ->
        config.staticFiles.add("C:/Users/James Armstrong/Documents/Projects/Paralax/paralax-panel/src/main/resources/static", Location.EXTERNAL)
        config.routes.post("/") {
            ctx -> ctx.redirect("index.html")
        }
    }.start(PARALAX_HOST, PARALAX_PORT)
}

fun main() {
    startServer()
}
