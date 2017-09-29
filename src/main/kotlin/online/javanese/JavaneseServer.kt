package online.javanese

import com.github.andrewoma.kwery.core.DefaultSession
import com.github.andrewoma.kwery.core.dialect.PostgresDialect
import online.javanese.exception.NotFoundException
import online.javanese.model.PageDao
import online.javanese.route.createTopLevelRouteHandler
import online.javanese.template.IndexPageBinding
import org.jetbrains.ktor.application.install
import org.jetbrains.ktor.features.StatusPages
import org.jetbrains.ktor.host.embeddedServer
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.http.HttpStatusCode
import org.jetbrains.ktor.netty.Netty
import org.jetbrains.ktor.response.respondText
import org.jetbrains.ktor.routing.get
import org.jetbrains.ktor.routing.routing
import org.thymeleaf.templatemode.TemplateMode
import java.io.FileInputStream
import java.sql.DriverManager
import java.util.*

object JavaneseServer {

    @JvmStatic
    fun main(args: Array<String>) {

        org.postgresql.Driver::class.java
        val dbProps = Properties().apply { load(FileInputStream("local.properties")) }
        val database = dbProps["database"]
        val connection = DriverManager.getConnection("jdbc:postgresql:$database", dbProps)

        val session = DefaultSession(connection, PostgresDialect())

        val templateEngine = TemplateEngine(
                templateResolver = ClassLoaderTemplateResolver(
                        classLoader = javaClass.classLoader,
                        prefix = "/templates/",
                        suffix = ".html",
                        templateMode = TemplateMode.HTML,
                        charset = Charsets.UTF_8
                ),
                messageResolver = MessageResolver(
                        stream = javaClass.getResourceAsStream("/locale/messages_ru.properties")
                )
        )

        val indexPageBinding = IndexPageBinding(templateEngine, Locale.getDefault())

        embeddedServer(Netty, 8080) {
            routing {
                val pageDao = PageDao(session)

                val topLevelRoute =
                        createTopLevelRouteHandler(pageDao, indexPageBinding)

                get("/") { topLevelRoute(call, "") }
                get("/{query}/") { topLevelRoute(call, call.parameters["query"]!!) }

                install(StatusPages) {
                    exception<NotFoundException> {
                        call.respondText(it.message, ContentType.Text.Plain, HttpStatusCode.NotFound) // todo: error pages
                    }
                }
            }
        }.start(wait = true)
    }

}
