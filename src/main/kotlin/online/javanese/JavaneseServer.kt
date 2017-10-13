package online.javanese

import nz.net.ultraq.thymeleaf.LayoutDialect
import online.javanese.exception.NotFoundException
import online.javanese.handler.ArticleHandler
import online.javanese.handler.ChapterHandler
import online.javanese.handler.CourseHandler
import online.javanese.handler.PageHandler
import online.javanese.model.*
import online.javanese.repository.*
import online.javanese.route.OnePartRoute
import online.javanese.route.TwoPartsRoute
import online.javanese.template.*
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
import org.thymeleaf.context.Context
import org.thymeleaf.templatemode.TemplateMode
import java.io.File
import java.io.FileInputStream
import java.util.*

object JavaneseServer {

    @JvmStatic
    fun main(args: Array<String>) {

        val (session, staticDirs) = parseProperties()
        val (localStaticDir, exposedStaticDir) = staticDirs

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
                dialects = *arrayOf(LayoutDialect())
        )

        val pageDao = PageDao(session)
        val courseDao = CourseDao(session)
        val chapterDao = ChapterDao(session)
        val lessonDao = LessonDao(session)
        val taskDao = TaskDao(session)
        val articleDao = ArticleDao(session)

        // todo: repositories may be removed in favour of DAOs
        val pageRepo = PageRepository(pageDao)
        val taskRepo = TaskRepository(taskDao)
        val lessonRepo = LessonRepository(lessonDao, taskRepo)
        val chapterRepo = ChapterRepository(chapterDao, lessonRepo)
        val courseRepo = CourseRepository(courseDao, chapterRepo)
        val articleRepo = ArticleRepository(articleDao)

        val locale = Locale.Builder().setLanguage("ru").setScript("Cyrl").build()
        val staticDirPair = "static" to exposedStaticDir
        val render = { templateName: String, parameters: Map<String, Any> ->
            templateEngine.process(
                    templateName,
                    Context(
                            locale,
                            parameters + staticDirPair
                    )
            )
        }

        val tree = courseRepo.findTreeSortedBySortIndex() // fixme: may not fetch whole tree
        // (after removing Thymeleaf this refactoring would be easy)

        val urlOfCourse = { c: Course.BasicInfo ->
            "/${c.urlPathComponent.encodeForUrl()}/"
        }

        val urlOfChapter = { ch: ChapterTree ->
            "/${ch.course.urlPathComponent.encodeForUrl()}/${ch.urlPathComponent.encodeForUrl()}/"
        }

        val route1 =
                OnePartRoute(
                        pageRepo,
                        courseRepo,
                        PageHandler(
                                courseRepo, articleRepo,
                                IndexPageTemplate(render),
                                TreePageTemplate(render),
                                ArticlesPageTemplate(render),
                                PageTemplate(render)
                        ),
                        CourseHandler(
                                courseRepo,
                                CoursePageTemplate(urlOfCourse, render)
                        )
                )

        val route2 =
                TwoPartsRoute(
                        pageRepo, articleRepo, courseRepo, chapterRepo,
                        ArticleHandler(
                                ArticlePageTemplate(render)
                        ),
                        ChapterHandler(
                                tree,
                                ChapterPageTemplate(urlOfChapter, render)
                        )
                )

        embeddedServer(Netty, 8080) {
            routing {

                // todo: addresses as objects

                get("/") { route1(call, "") }
                get("/{query}/") { route1(call, call.parameters["query"]!!) }
                get("/{first}/{second}/") { route2(call, call.parameters["first"]!!, call.parameters["second"]!!) }

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

    private fun parseProperties() = Properties().let {
        it.load(FileInputStream("local.properties"))

        Pair(
                PostgreSqlSession(
                        dbName = it["database"] as String,
                        user = it["user"] as String,
                        password = it["password"] as String
                ),
                Pair(
                        // e. g. '/home/<user>/IdeaProjects/javanese/src/main/resources/static' for development
                        it["localStaticDir"] as String?,

                        // e. g. '/static' for development, 'http://static.javanese.online/' for production
                        it["exposedStaticDir"] as String
                )
        )
    }

}

// todo: rename all 'h1's to 'heading'
// todo: urlPathComponent -> urlComponent or what??
