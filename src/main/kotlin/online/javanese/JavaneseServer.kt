package online.javanese

import nz.net.ultraq.thymeleaf.LayoutDialect
import online.javanese.exception.NotFoundException
import online.javanese.handler.*
import online.javanese.model.*
import online.javanese.repository.CourseRepository
import online.javanese.route.OnePartRoute
import online.javanese.route.ThreePartsRoute
import online.javanese.route.TwoPartsRoute
import online.javanese.template.*
import org.jetbrains.ktor.application.install
import org.jetbrains.ktor.content.files
import org.jetbrains.ktor.content.static
import org.jetbrains.ktor.content.staticRootFolder
import org.jetbrains.ktor.features.StatusPages
import org.jetbrains.ktor.host.embeddedServer
import org.jetbrains.ktor.http.HttpStatusCode
import org.jetbrains.ktor.netty.Netty
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

        val config = Config(
                Properties().also {
                    it.load(FileInputStream("local.properties").bufferedReader())
                }
        )

        val session = PostgreSqlSession(
                dbName = config.dbName,
                user = config.dbUser,
                password = config.dbPassword
        )

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

        val messages = Properties().apply {
            load(JavaneseServer::class.java.getResourceAsStream("/locale/messages_ru.properties").bufferedReader())
        }

        val pageDao = PageDao(session)
        val courseDao = CourseDao(session)
        val taskDao = TaskDao(session)
        val lessonDao = LessonDao(session, taskDao)
        val chapterDao = ChapterDao(session, lessonDao)
        val articleDao = ArticleDao(session)

        val courseRepo = CourseRepository(courseDao, chapterDao)

        val locale = Locale.Builder().setLanguage("ru").setScript("Cyrl").build()
        val staticDirPair = "static" to config.exposedStaticDir
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

        val urlOfLesson = { l: LessonTree ->
            val ch = l.chapter
            val co = ch.course
            "/${co.urlPathComponent.encodeForUrl()}/${ch.urlPathComponent.encodeForUrl()}/${l.urlPathComponent.encodeForUrl()}/"
        }

        val urlOfTask = { t: TaskTree ->
            val l = t.lesson
            val ch = l.chapter
            val co = ch.course
            "/${co.urlPathComponent.encodeForUrl()}/${ch.urlPathComponent.encodeForUrl()}/${l.urlPathComponent.encodeForUrl()}/#${t.urlPathComponent.encodeForUrl()}"
        }

        val route1 =
                OnePartRoute(
                        pageDao,
                        courseRepo,
                        PageHandler(
                                courseRepo, articleDao,
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
                        pageDao, articleDao, courseRepo, chapterDao,
                        ArticleHandler(
                                ArticlePageTemplate(render)
                        ),
                        ChapterHandler(
                                tree,
                                ChapterPageTemplate(urlOfChapter, render)
                        )
                )

        val route3 =
                ThreePartsRoute(
                        tree,
                        LessonHandler(
                                lessonDao,
                                LessonPageTemplate(urlOfLesson, urlOfTask, render)
                        )
                )

        val errorHandler = ErrorHandler(
                ErrorPageTemplate(
                        { key, default -> messages.getProperty(key) ?: default },
                        render
                )
        )

        embeddedServer(Netty, 8080) {
            install(StatusPages) {
                exception<NotFoundException> {
                    call.response.status(HttpStatusCode.NotFound)
                    errorHandler(call)
                }
                status(HttpStatusCode.NotFound) {
                    call.response.status(HttpStatusCode.NotFound)
                    errorHandler(call)
                }
            }

            routing {

                // todo: addresses as objects

                get("/") {
                    route1(call, "")
                }
                get("/{f}/") {
                    route1(call, call.parameters["f"]!!)
                }
                get("/{f}/{s}/") {
                    route2(call, call.parameters["f"]!!, call.parameters["s"]!!)
                }
                get("/{f}/{s}/{t}/") {
                    route3(call, call.parameters["f"]!!, call.parameters["s"]!!, call.parameters["t"]!!)
                }

                if (config.localStaticDir != null) {
                    static(config.exposedStaticDir) {
                        val localStaticDirFile = File(config.localStaticDir)
                        staticRootFolder = localStaticDirFile.parentFile
                        files(localStaticDirFile.name)
                    }
                }
            }
        }.start(wait = true)
    }

}

// todo: rename all 'h1's to 'heading'
// todo: urlPathComponent -> urlComponent or what??
