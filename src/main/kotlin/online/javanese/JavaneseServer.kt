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
import online.javanese.link.*
import online.javanese.locale.Russian
import online.javanese.model.*
import online.javanese.model.Page.Magic.Tree
import online.javanese.page.*
import online.javanese.route.OnePartRoute
import online.javanese.route.ThreePartsRoute
import online.javanese.route.TwoPartsRoute
import java.io.FileInputStream
import java.util.*


object JavaneseServer {

    /**
     * Entry point. Satisfies all dependencies and starts web-server.
     */
    @JvmStatic
    fun main(args: Array<String>) {
        val singleThread = "--single-thread" in args

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


        val pageLink = Link(
                IndexOrSingleSegmDirAddress(PageTable.UrlSegment.property),
                PageTable.MetaTitle.property
        )
        val lessonsLink = Link(
                SingleSegmentDirAddress(PageTable.UrlSegment.property),
                { language.lessonsTreeTab }, { "lessons" }
        )
        val tasksLink = Link(
                SingleSegmentDirAddress(PageTable.UrlSegment.property),
                { language.tasksTreeTab }, { "tasks" }
        )

        val courseLink = Link(
                SingleSegmentDirAddress(BasicCourseInfoTable.UrlSegment.property),
                BasicCourseInfoTable.LinkText.property
        )
        val chapterLink = Link(
                TwoSegmentDirAddress(
                        { courseDao.findBasicById(it.courseId)!!.urlSegment },
                        BasicChapterInfoTable.UrlSegment.property
                ),
                BasicChapterInfoTable.LinkText.property
        )
        val lessonLink = Link(
                HierarchicalThreeSegmentDirAddress(
                        { it },
                        { chapterDao.findBasicById(it.chapterId)!! },
                        { courseDao.findBasicById(it.courseId)!! },
                        BasicCourseInfoTable.UrlSegment.property,
                        BasicChapterInfoTable.UrlSegment.property,
                        BasicLessonInfoTable.UrlSegment.property
                ),
                BasicLessonInfoTable.LinkText.property
        )
        val taskLink = Link(
                HierarchicalThreeSegmentDirAddress(
                        { lessonDao.findBasicById(it.lessonId)!! },
                        { chapterDao.findBasicById(it.chapterId)!! },
                        { courseDao.findBasicById(it.courseId)!! },
                        BasicCourseInfoTable.UrlSegment.property,
                        BasicChapterInfoTable.UrlSegment.property,
                        BasicLessonInfoTable.UrlSegment.property
                ),
                BasicTaskInfoTable.LinkText.property,
                BasicTaskInfoTable.UrlPathComponent.property
        )
        val articleLink = Link(
                TwoSegmentDirAddress(
                        { pageDao.findByMagic(Page.Magic.Articles)!!.urlSegment },
                        BasicArticleInfoTable.UrlSegment.property
                ),
                BasicArticleInfoTable.LinkText.property
        )
        val codeReviewLink = Link(
                TwoSegmentDirAddress(
                        { pageDao.findByMagic(Page.Magic.CodeReview)!!.urlSegment },
                        CodeReviewTable.UrlSegment.property
                ),
                CodeReviewTable.MetaTitle.property
        )

        val reportTaskAction =
                Action(TwoSegmentFileAddress<Unit>({ "task" }, { "report" }))


        val mainStyle = "main.min.css?3"
        val codeMirrorStyle = "codemirror_ambiance.min.css"

        val mainScript = "vue_zepto_mdl_dialog_scroll_unfocus_tabs_form.min.js?1"
        val sandboxScript = "highlight_trace_codemirror_clike_sandbox_switcher.min.js?1"

        val layout = MainLayout(config.exposedStaticDir, mainStyle, mainScript, language)

        val betweenLinks: BetweenBlocks = { +" / " }
        val breadCrumbsToIndex = navOf(pageLink)
        val breadCrumbsToPage = betweenLinks.navOf(pageLink, pageLink)
        val breadCrumbsToCourse = betweenLinks.navOf(pageLink, pageLink, courseLink)
        val breadCrumbsToChapter = betweenLinks.navOf(pageLink, pageLink, courseLink, chapterLink)

        // fixme: eliminate these OnePart, TwoPart, ThreePart route handlers by more generic things
        val route1 =
                OnePartRoute(
                        pageDao,
                        courseDao,
                        PageHandler(
                                pageDao, courseDao, chapterDao, lessonDao, taskDao, articleDao, layout,
                                indexPage = {
                                    CardsPage(
                                            page = it,
                                            contents = pageDao.findAllSecondary().map {
                                                CardsPage.Card(if (it.magic == Tree) tasksLink else pageLink, it, it.icon, it.subtitle)
                                            }
                                    )
                                },
                                coursesPage = { indexPage, coursesPage, courses ->
                                    CardsPage(
                                            page = coursesPage,
                                            beforeContent = breadCrumbsToIndex(indexPage),
                                            contents = courses.map { CardsPage.Card(courseLink, it, it.icon, it.subtitle) },
                                            dCount = CardsPage.CountOnDesktop.Three,
                                            tCount = CardsPage.CountOnTablet.One
                                    )
                                },
                                treePage = { indexPage, coursesPage, courses ->
                                    TreePage(
                                            coursesPage, courses, language, courseLink, chapterLink, lessonLink, taskLink,
                                            breadCrumbsToIndex(indexPage)
                                    )
                                },
                                articlesPage = { idx, ar, articles ->
                                    ArticlesPage(ar, articles, config.exposedStaticDir, articleLink, breadCrumbsToIndex(idx))
                                },
                                codeReview = { idx, cr ->
                                    CodeReviewPage(cr, codeReviewDao.findAll(), codeReviewLink, language, breadCrumbsToIndex(idx))
                                }
                        ),
                        CourseHandler(
                                pageDao, courseDao, chapterDao, lessonDao, taskDao, layout,
                                coursePage = { idx, tr, c, ct, pn ->
                                    CoursePage(
                                            c, ct, pn, courseLink, chapterLink,
                                            lessonLink, taskLink, language, breadCrumbsToPage(idx, tr)
                                    )
                                }
                        )
                )

        val route2 =
                TwoPartsRoute(
                        pageDao, articleDao, courseDao, chapterDao, codeReviewDao,
                        { idx, ar, ars, call -> call.respondHtml { layout(this, ArticlePage(
                                ars, language, config.exposedStaticDir, highlightScript = sandboxScript, beforeContent = breadCrumbsToPage(idx, ar)
                        )) } },
                        ChapterHandler(
                                pageDao, chapterDao, lessonDao, taskDao, layout,
                                { idx, tr, crs, chp, chpt, prevNext -> ChapterPage(
                                        chp, chpt, prevNext, chapterLink, lessonLink, taskLink,
                                        language, breadCrumbsToCourse(idx, tr, crs)
                                ) }
                        ),
                        { idx, cr, review, call -> call.respondHtml { layout(this, CodeReviewDetailsPage(
                                review, config.exposedStaticDir, highlightScript = sandboxScript, beforeContent = breadCrumbsToPage(idx, cr)
                        )) } }
                )

        val route3 =
                ThreePartsRoute(
                        courseDao, chapterDao, lessonDao,
                        LessonHandler(
                                courseDao, chapterDao, lessonDao, taskDao, pageDao, layout,
                                { idx, tr, crs, chp, l, lt, prNx -> LessonPage(
                                        l, lt, prNx, config.exposedStaticDir, lessonLink, reportTaskAction, language, sandboxScript,
                                        codeMirrorStyle, breadCrumbsToChapter(idx, tr, crs, chp)
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
        // TODO: RSS for code reviews

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

        embeddedServer(Netty, port = config.listenPort, host = config.listenHost, configure = {
            if (singleThread) {
                connectionGroupSize = 1
                workerGroupSize = 1
                callGroupSize = 1
            }
        }) {
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
