package online.javanese

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authentication
import io.ktor.auth.basicAuthentication
import io.ktor.content.resources
import io.ktor.content.static
import io.ktor.features.StatusPages
import io.ktor.html.respondHtml
import io.ktor.http.HttpStatusCode
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import online.javanese.exception.NotFoundException
import online.javanese.handler.*
import online.javanese.krud.installAdmin
import online.javanese.krud.krudStaticResources
import online.javanese.krud.stat.HitStat
import online.javanese.krud.stat.InMemoryStatTable
import online.javanese.krud.stat.UserAgent
import online.javanese.krud.stat.installHitStatInterceptor
import online.javanese.model.*
import online.javanese.page.*
import online.javanese.route.OnePartRoute
import online.javanese.route.ThreePartsRoute
import online.javanese.route.TwoPartsRoute
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

        // fixme: deprecated objects start
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
        // fixme: deprecated objects end

        val layout = Layout(config.exposedStaticDir, messages)

        val pageLink =
                IndexOrSingleSegmDirLink(PageTable.UrlPathComponent.property, PageTable.MetaTitle.property)
        val courseLink =
                SingleSegmentDirLink(BasicCourseInfoTable.UrlPathComponent.property, BasicCourseInfoTable.LinkText.property)
        val chapterLink =
                TwoSegmentDirLink(BasicCourseInfoTable.UrlPathComponent.property, BasicChapterInfoTable.UrlPathComponent.property, { _, ch -> ch.linkText })

        val route1 =
                OnePartRoute(
                        pageDao,
                        courseDao,
                        PageHandler(
                                courseDao, articleDao,
                                { layout(this, IndexPage(it)) },
                                { pg, cs -> layout(this, TreePage(pg, cs, messages, urlOfCourseTree, urlOfChapter, urlOfLesson, urlOfTask)) },
                                { page, articles -> layout(this, ArticlesPage(page, articles, messages, config.exposedStaticDir, urlOfArticle)) },
                                { layout(this, CodeReviewPage(it, messages, codeReviewDao.findAll())) }
                        ),
                        CourseHandler(
                                pageDao, courseDao,
                                { tp, c, ct, p, n -> layout(this, CoursePage(tp, c, ct, p, n, pageLink, urlOfCourse, urlOfChapter, urlOfLesson, urlOfTask, messages)) }
                        )
                )

        val route2 =
                TwoPartsRoute(
                        pageDao, articleDao, courseDao, chapterDao, codeReviewDao,
                        { pg, ar, call ->
                            call.respondHtml { layout(this, ArticlePage(pg, ar, messages, urlOfPage)) }
                        },
                        ChapterHandler(
                                pageDao,
                                tree, layout,
                                { idx, tr, crs, chp, chpt, prev, next ->
                                    ChapterPage(idx, tr, crs, chp, chpt, prev, next, pageLink, courseLink, urlOfChapter, urlOfLesson, urlOfTask, messages)
                                }
                        ),
                        { page, review, call -> call.respondHtml { layout(this, CodeReviewDetailsPage(page, review, messages, urlOfPage)) } }
                )

        val route3 =
                ThreePartsRoute(
                        tree,
                        LessonHandler(
                                courseDao, chapterDao, lessonDao, pageDao, layout,
                                { idx, tr, crs, chp, l, lt, pr, nx -> LessonPage(idx, tr, crs, chp, l, lt, pr, nx, config.exposedStaticDir, pageLink, courseLink, chapterLink, urlOfLesson, messages) }
                        )
                )

        val errorHandler: suspend (ApplicationCall) -> Unit = { call ->
            val status = call.response.status()!!
            call.respondHtml(status) {
                layout(this, ErrorPage(messages, status.value, status.description))
            }
        }

        val articleRssHandler = ArticleRssHandler(articleDao)

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

        val noUa = UserAgent("", "", "")
        val stat = HitStat(InMemoryStatTable({ noUa }, { it.endsWith(".css") || it.endsWith(".js") }))

        embeddedServer(Netty, port = config.listenPort, host = config.listenHost) {
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

                installHitStatInterceptor(stat)

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

                static(config.exposedStaticDir) {
                    resources("static")
                }

                route("/${config.adminRoute}/") {
                    installAdmin(JavaneseAdminPanel(
                            config.adminRoute, session,
                            taskDao, lessonDao, chapterDao, courseDao, articleDao,
                            pageDao, taskErrorReportDao, codeReviewCandidateDao, codeReviewDao,
                            stat
                    ))

                    authentication {
                        basicAuthentication("Admin") { cred ->
                            if (cred.name == config.adminUsername && cred.password == config.adminPassword)
                                UserIdPrincipal("admin")
                            else null
                        }
                    }
                }

                krudStaticResources("admin-static")
            }

            Unit
        }.start(wait = true)
    }

}

// todo: rename all 'h1's to 'heading', 'urlPathComponent' to 'urlSegment' after refactoring
// todo: make DAO functions suspend
// todo: '200 Ok' with 'ok' body should be replaced with 'No content'
// todo: indices on urlSegment
