package online.javanese

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.content.files
import io.ktor.content.static
import io.ktor.content.staticRootFolder
import io.ktor.features.StatusPages
import io.ktor.html.respondHtml
import io.ktor.http.HttpStatusCode
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import nz.net.ultraq.thymeleaf.LayoutDialect
import online.javanese.exception.NotFoundException
import online.javanese.extensions.encodeForUrl
import online.javanese.handler.*
import online.javanese.model.*
import online.javanese.route.OnePartRoute
import online.javanese.route.ThreePartsRoute
import online.javanese.route.TwoPartsRoute
import online.javanese.template.*
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

        val articleDao = ArticleDao(session)

        val taskDao = TaskDao(session)
        val lessonDao = LessonDao(session, taskDao)
        val chapterDao = ChapterDao(session, lessonDao)
        val courseDao = CourseDao(session, chapterDao)
        val codeReviewDao = CodeReviewDao(session)

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

        val tree = courseDao.findTreeSortedBySortIndex() // fixme: may not fetch whole tree
        // (after removing Thymeleaf this refactoring would be easy)

        val urlOfPage = { p: Page ->
            "/${p.urlPathComponent.encodeForUrl()}/"
        }

        val urlOfCourse = { c: Course.BasicInfo ->
            "/${c.urlPathComponent.encodeForUrl()}/"
        }
        val urlOfCourseTree = { c: CourseTree ->
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

        val urlOfArticle = { p: Page, a: Article.BasicInfo ->
            "/${p.urlPathComponent.encodeForUrl()}/${a.urlPathComponent.encodeForUrl()}/"
        }

        val urlOfCodeReview = { p: Page, r: CodeReview ->
            "/${p.urlPathComponent.encodeForUrl()}/${r.urlSegment.encodeForUrl()}/"
        }

        val layout = Layout(config.exposedStaticDir, messages)

        val route1 =
                OnePartRoute(
                        pageDao,
                        courseDao,
                        PageHandler(
                                courseDao, articleDao,
                                IndexPageTemplate(render),
                                TreePageTemplate(render),
                                ArticlesPageTemplate(render),
                                { layout(this, CodeReviewPage(it, messages, codeReviewDao.findAll())) }
                        ),
                        CourseHandler(
                                courseDao,
                                CoursePageTemplate(urlOfCourse, render)
                        )
                )

        val route2 =
                TwoPartsRoute(
                        pageDao, articleDao, courseDao, chapterDao, codeReviewDao,
                        ArticleHandler(
                                ArticlePageTemplate(render)
                        ),
                        ChapterHandler(
                                tree,
                                ChapterPageTemplate(urlOfChapter, render)
                        ),
                        { page, review, call -> call.respondHtml { layout(this, CodeReviewDetailsPage(page, review, messages, urlOfPage)) } }
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

        val articleRssHandler = ArticleRssHandler(
                config.siteUrl,
                articleDao,
                RssFeedTemplate(render))

        val taskErrorReportDao = TaskErrorReportDao(session)

        val submitTaskErrorReport =
                SubmitTaskErrorReportHandler(taskErrorReportDao)

        val codeReviewCandidateDao = CodeReviewCandidateDao(session)

        val submitCodeReviewCandidate =
                SubmitCodeReviewCandidateHandler(codeReviewCandidateDao)

        val sitemap =
                SitemapHandler(config.siteUrl,
                        urlOfPage, urlOfCourseTree, urlOfChapter, urlOfLesson, urlOfArticle, urlOfCodeReview,
                        tree, pageDao, courseDao, chapterDao, lessonDao, articleDao, codeReviewDao
                )

        val robots =
                RobotsHandler(config)

        val sandboxWebSocketHandler =
                SandboxWebSocketHandler(config, taskDao)

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

            install(WebSockets)

            routing {

                // todo: addresses as objects

                get("/") { route1(call, "") }
                get1(route1)
                get2(route2)
                get3(route3)

                get("/articles.rss", articleRssHandler)

                post("/task/report", submitTaskErrorReport)
                post("/codeReview/add", submitCodeReviewCandidate)

                get("/sitemap.xml", sitemap)
                get("/robots.txt", robots)

                webSocket(path = "/sandbox/ws", handler = sandboxWebSocketHandler)

                if (config.localStaticDir != null) {
                    static(config.exposedStaticDir) {
                        val localStaticDirFile = File(config.localStaticDir)
                        staticRootFolder = localStaticDirFile.parentFile
                        files(localStaticDirFile.name)
                    }
                }
            }

            Unit
        }.start(wait = true)
    }

}

// todo: rename all 'h1's to 'heading' or 'title', remove linkText and metaTitle if unused
// todo: make DAO functions suspend
// todo: '200 Ok' with 'ok' body should be replaced with 'No content'
