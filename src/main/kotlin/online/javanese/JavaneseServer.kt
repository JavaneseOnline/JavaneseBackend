package online.javanese

import org.jetbrains.ktor.host.embeddedServer
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.netty.Netty
import org.jetbrains.ktor.response.respondText
import org.jetbrains.ktor.routing.get
import org.jetbrains.ktor.routing.routing

object JavaneseServer {

    @JvmStatic
    fun main(args: Array<String>) {
        embeddedServer(Netty, 8080) {
            routing {
                get("/") {
                    call.respondText("Hello, world!", ContentType.Text.Html)
                }
            }
        }.start(wait = true)
    }

}
