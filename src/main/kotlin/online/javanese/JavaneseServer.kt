package online.javanese

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authentication
import io.ktor.auth.basicAuthentication
import io.ktor.content.files
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
import online.javanese.locale.Russian
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

        val language = Russian

        val pageDao = PageDao(session)

        val articleDao = ArticleDao(session)

        val taskDao = TaskDao(session)
        val lessonDao = LessonDao(session)
        val chapterDao = ChapterDao(session)
        val courseDao = CourseDao(session)

        val codeReviewCandidateDao = CodeReviewCandidateDao(session)
        val codeReviewDao = CodeReviewDao(session)


        val pageLink =
                IndexOrSingleSegmDirLink(
                        PageTable.UrlSegment.property,
                        PageTable.MetaTitle.property
                )
        val lessonsLink =
                SingleSegmentDirLinkWithFragment(
                        PageTable.UrlSegment.property,
                        { "lessons" }, { language.lessonsTreeTab }
                )
        val tasksLink =
                SingleSegmentDirLinkWithFragment(
                        PageTable.UrlSegment.property,
                        { "tasks" }, { language.tasksTreeTab }
                )

        val courseLink =
                SingleSegmentDirLink(
                        BasicCourseInfoTable.UrlSegment.property,
                        BasicCourseInfoTable.LinkText.property
                )
        val chapterLink =
                TwoSegmentDirLink(
                        { courseDao.findBasicById(it.courseId)!!.urlSegment },
                        BasicChapterInfoTable.UrlSegment.property,
                        BasicChapterInfoTable.LinkText.property
                )
        val lessonLink =
                ThreeSegmentDirLink( // fixme: round-tripping
                        { courseDao.findBasicById(chapterDao.findBasicById(it.chapterId)!!.courseId)!!.urlSegment },
                        { chapterDao.findBasicById(it.chapterId)!!.urlSegment },
                        BasicLessonInfoTable.UrlSegment.property,
                        BasicLessonInfoTable.LinkText.property
                )
        val taskLink =
                ThreeSegmentDirLinkWithFragment(  // fixme: so ugly round-tripping
                        { courseDao.findBasicById(chapterDao.findBasicById(lessonDao.findBasicById(it.lessonId)!!.chapterId)!!.courseId)!!.urlSegment },
                        { chapterDao.findBasicById(lessonDao.findBasicById(it.lessonId)!!.chapterId)!!.urlSegment },
                        { lessonDao.findBasicById(it.lessonId)!!.urlSegment },
                        BasicTaskInfoTable.UrlPathComponent.property,
                        BasicTaskInfoTable.LinkText.property
                )
        val articleLink =
                TwoSegmentDirLink(
                        { pageDao.findByMagic(Page.Magic.Articles)!!.urlSegment },
                        BasicArticleInfoTable.UrlSegment.property,
                        BasicArticleInfoTable.LinkText.property
                )
        val codeReviewLink =
                TwoSegmentDirLink(
                        { pageDao.findByMagic(Page.Magic.CodeReview)!!.urlSegment },
                        CodeReviewTable.UrlSegment.property,
                        CodeReviewTable.MetaTitle.property
                )


        val mainStyle = "main.min.css?2"
        val codeMirrorStyle = "codemirror_ambiance.min.css"

        val mainScript = "vue_zepto_mdl_dialog_scroll_unfocus_tabs_form.min.js"
        val sandboxScript = "highlight_trace_codemirror_clike_sandbox.min.js"

        val layout = MainLayout(config.exposedStaticDir, mainStyle, mainScript, language)

        // fixme: eliminate these OnePart, TwoPart, ThreePart route handlers by more generic things
        val route1 =
                OnePartRoute(
                        pageDao,
                        courseDao,
                        PageHandler(
                                pageDao, courseDao, chapterDao, lessonDao, taskDao, articleDao, layout,
                                indexPage = { CardsPage(it, pageDao.findAll().toCards(lessonsLink, tasksLink, pageLink, language.indexCardDescriptions)) },
                                treePage = { idx, tr, cs -> TreePage(idx, tr, cs, language, pageLink, courseLink, chapterLink, lessonLink, taskLink) },
                                articlesPage = { idx, ar, articles -> ArticlesPage(idx, ar, articles, config.exposedStaticDir, pageLink, articleLink) },
                                codeReview = { idx, cr -> CodeReviewPage(idx, cr, codeReviewDao.findAll(), pageLink, language) }
                        ),
                        CourseHandler(
                                pageDao, courseDao, chapterDao, lessonDao, taskDao, layout,
                                { idx, tr, c, ct, pn -> CoursePage(
                                        idx, tr, c, ct, pn,
                                        pageLink, courseLink, chapterLink, lessonLink, taskLink, language
                                ) }
                        )
                )

        val route2 =
                TwoPartsRoute(
                        pageDao, articleDao, courseDao, chapterDao, codeReviewDao,
                        { idx, ar, ars, call -> call.respondHtml { layout(this, ArticlePage(
                                idx, ar, ars, language, pageLink,
                                config.exposedStaticDir, highlightScript = sandboxScript
                        )) } },
                        ChapterHandler(
                                pageDao, chapterDao, lessonDao, taskDao, layout,
                                { idx, tr, crs, chp, chpt, prevNext -> ChapterPage(
                                        idx, tr, crs, chp, chpt,
                                        prevNext, pageLink, courseLink, chapterLink, lessonLink, taskLink, language
                                ) }
                        ),
                        { idx, cr, review, call -> call.respondHtml { layout(this, CodeReviewDetailsPage(
                                idx, cr, review, pageLink, config.exposedStaticDir, highlightScript = sandboxScript
                        )) } }
                )

        val route3 =
                ThreePartsRoute(
                        courseDao, chapterDao, lessonDao,
                        LessonHandler(
                                courseDao, chapterDao, lessonDao, taskDao, pageDao, layout,
                                { idx, tr, crs, chp, l, lt, prNx -> LessonPage(
                                        idx, tr, crs, chp, l, lt, prNx, config.exposedStaticDir,
                                        pageLink, courseLink, chapterLink, lessonLink, language, sandboxScript,
                                        codeMirrorStyle
                                ) }
                        )
                )

        val errorHandler: suspend (ApplicationCall) -> Unit = { call ->
            val status = call.response.status()!!
            call.respondHtml(status) {
                layout(this, ErrorPage(language, status.value, status.description))
            }
        }

        val articleRssHandler =
                ArticleRssHandler(pageDao, articleDao, pageLink, articleLink, config.siteUrl, language.articlesFeedInfo)
        // TODO: RSS for code reviews; RSS icon on index

        val taskErrorReportDao = TaskErrorReportDao(session)

        val submitTaskErrorReport =
                SubmitTaskErrorReportHandler(taskErrorReportDao)

        val submitCodeReviewCandidate =
                SubmitCodeReviewCandidateHandler(codeReviewCandidateDao)

        val sitemap =
                SitemapHandler(config.siteUrl,
                        pageLink, courseLink, chapterLink, lessonLink, articleLink, codeReviewLink,
                        pageDao, courseDao, chapterDao, lessonDao, articleDao, codeReviewDao
                )

        val robots =
                RobotsHandler(config)

        val sandboxWebSocketHandler =
                SandboxWebSocketHandler(config, taskDao)

        val noUa = UserAgent("", "", "")
        val stat = HitStat(InMemoryStatTable({ noUa }))

        val serveResourcesAs = (config.listenHost + ":" + config.listenPort).let { hostAndPort ->
            val idx = config.exposedStaticDir.indexOf(hostAndPort)

            if (idx < 0) null else config.exposedStaticDir.substring(idx + hostAndPort.length)
        }

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

                serveResourcesAs?.let {
                    println("Serving static resources from a directory is intended for debug only. Exposing as $it")
                    static(it) {
                        files("./etc/static-prepared/")
                    }
                }
                krudStaticResources("admin-static")
            }

            Unit
        }.start(wait = true)
    }

}
