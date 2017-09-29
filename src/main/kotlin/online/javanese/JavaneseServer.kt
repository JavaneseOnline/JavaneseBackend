package online.javanese

import com.github.andrewoma.kwery.core.DefaultSession
import com.github.andrewoma.kwery.core.dialect.PostgresDialect
import nz.net.ultraq.thymeleaf.LayoutDialect
import online.javanese.exception.NotFoundException
import online.javanese.model.PageDao
import online.javanese.route.createTopLevelRouteHandler
import online.javanese.template.IndexPageBinding
import org.jetbrains.ktor.application.install
import org.jetbrains.ktor.content.files
import org.jetbrains.ktor.content.static
import org.jetbrains.ktor.content.staticRootFolder
import org.jetbrains.ktor.features.StatusPages
import org.jetbrains.ktor.host.embeddedServer
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.http.HttpStatusCode
import org.jetbrains.ktor.netty.Netty
import org.jetbrains.ktor.response.respondText
import org.jetbrains.ktor.routing.get
import org.jetbrains.ktor.routing.routing
import org.thymeleaf.templatemode.TemplateMode
import java.io.File
import java.io.FileInputStream
import java.sql.DriverManager
import java.util.*

object JavaneseServer {

    @JvmStatic
    fun main(args: Array<String>) {

        org.postgresql.Driver::class.java
        val localProps = Properties().apply {
            load(FileInputStream("local.properties"))
            /* Must contain 'database', 'user', 'password', 'localStaticDir', optional 'exposedStaticDir' */
        }
        val database = localProps["database"]
        val connection = DriverManager.getConnection("jdbc:postgresql:$database", localProps)

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
                ),
                dialects = LayoutDialect()
        )

        val localStaticDir = localProps["localStaticDir"] as String?
        val exposedStaticDir = localProps["exposedStaticDir"] as String
        val indexPageBinding = IndexPageBinding(exposedStaticDir, templateEngine, Locale.getDefault())

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

                if (localStaticDir != null) {
                    static(exposedStaticDir) {
                        val localStaticDirFile = File(localStaticDir)
                        staticRootFolder = localStaticDirFile.parentFile
                        files(localStaticDirFile.name)
                    }
                }
            }
        }.start(wait = true)
    }

}
