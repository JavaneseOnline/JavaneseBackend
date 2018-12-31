package online.javanese

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.OAuthServerSettings
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.auth.digest
import io.ktor.features.StatusPages
import io.ktor.html.respondHtml
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.pipeline.PipelineContext
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
import java.io.FileInputStream
import java.util.*


object JavaneseServer {

    /**
     * Entry point. Satisfies all dependencies and starts the web-server.
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

        val taskErrorReportDao = TaskErrorReportDao(session)


        val pageLink = Link(
                IndexOrSingleSegmDirAddress(PageTable.UrlSegment.property) { addr ->
                    if (addr == null) pageDao.findByMagic(Page.Magic.Index)
                    else pageDao.findByUrlSegment(addr)
                },
                PageTable.MetaTitle.property
        )
        val findPageBySegm = pageDao::findByUrlSegment
        val lessonsLink = Link(
                SingleSegmentDirAddress(PageTable.UrlSegment.property, findPageBySegm),
                Just(language.lessonsTreeTab), Just("lessons")
        )
        val tasksLink = Link(
                SingleSegmentDirAddress(PageTable.UrlSegment.property, findPageBySegm),
                Just(language.tasksTreeTab), Just("tasks")
        )

        val courseLink = Link(
                SingleSegmentDirAddress(BasicCourseInfoTable.UrlSegment.property, courseDao::findBasicByUrlSegment),
                BasicCourseInfoTable.LinkText.property
        )
        val chapterLink = Link(
                TwoSegmentDirAddress(
                        { courseDao.findBasicById(it.courseId)!!.urlSegment },
                        BasicChapterInfoTable.UrlSegment.property,
                        { crs, chp -> courseDao.findBasicByUrlSegment(crs)?.let { chapterDao.findBasicByUrlSegment(it.id, chp) } }
                ),
                BasicChapterInfoTable.LinkText.property
        )
        val lessonLink = Link(
                HierarchicalThreeSegmentDirAddress(
                        Identity(),
                        { chapterDao.findBasicById(it.chapterId)!! },
                        { courseDao.findBasicById(it.courseId)!! },
                        BasicCourseInfoTable.UrlSegment.property,
                        BasicChapterInfoTable.UrlSegment.property,
                        BasicLessonInfoTable.UrlSegment.property,
                        courseDao::findBasicByUrlSegment,
                        { crs, chp -> chapterDao.findBasicByUrlSegment(crs.id, chp) },
                        { _, chp, les -> lessonDao.findByUrlSegment(chp.id, les)?.basicInfo /* fixme */ }
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
                        BasicLessonInfoTable.UrlSegment.property,
                        courseDao::findBasicByUrlSegment,
                        { crs, chp -> chapterDao.findBasicByUrlSegment(crs.id, chp) },
                        { _, chp, les -> lessonDao.findByUrlSegment(chp.id, les)?.basicInfo /* fixme */ }
                ),
                BasicTaskInfoTable.LinkText.property,
                BasicTaskInfoTable.UrlPathComponent.property
        )
        val articleLink = Link(
                TwoSegmentDirAddress(
                        { pageDao.findByMagic(Page.Magic.Articles)!!.urlSegment },
                        BasicArticleInfoTable.UrlSegment.property,
                        { _, article -> articleDao.findByUrlSegment(article)?.basicInfo }
                ),
                BasicArticleInfoTable.LinkText.property
        )
        val codeReviewLink = Link(
                TwoSegmentDirAddress(
                        { pageDao.findByMagic(Page.Magic.CodeReview)!!.urlSegment },
                        CodeReviewTable.UrlSegment.property,
                        { _, review -> codeReviewDao.findByUrlSegment(review) }
                ),
                CodeReviewTable.MetaTitle.property
        )

        val reportTaskAction =
                Action(TwoSegmentFileAddress(Just("task"), Just("report")) { dir, file ->
                    if (dir == "task" && file == "report") Unit else null
                })

        // fixme: returning Unit from Action bySegments may become a common pitfall

        val codeReviewAction =
                Action(TwoSegmentFileAddress(Just("codeReview"), Just("add")) { dir, file ->
                    if (dir == "codeReview" && file == "add") Unit else null }
                )


        val mainStyle = "main.min.css?4"
        val codeMirrorStyle = "codemirror_ambiance.min.css"

        val mainScript = "vue_zepto_mdl_dialog_scroll_unfocus_tabs_form.min.js?1"
        val sandboxScript = "highlight_trace_codemirror_clike_sandbox_switcher.min.js?2"

        val layout = MainLayout(config.exposedStaticDir, mainStyle, mainScript, language)

        val betweenLinks: BetweenBlocks = { +" / " }
        val breadCrumbsToIndex = navOf(pageLink)
        val breadCrumbsToPage = betweenLinks.navOf(pageLink, pageLink)
        val breadCrumbsToCourse = betweenLinks.navOf(pageLink, pageLink, courseLink)
        val breadCrumbsToChapter = betweenLinks.navOf(pageLink, pageLink, courseLink, chapterLink)

        val routing = Routing("parts") {
            pageLink x PageHandler(
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
                                breadCrumbsToIndex(indexPage), lessonsLink, tasksLink
                        )
                    },
                    articlesPage = { idx, ar, articles ->
                        ArticlesPage(ar, articles, config.exposedStaticDir, articleLink, breadCrumbsToIndex(idx))
                    },
                    codeReview = { idx, cr ->
                        CodeReviewPage(cr, codeReviewDao.findAll(), codeReviewLink, language, breadCrumbsToIndex(idx))
                    }
            )

            courseLink x CourseHandler(
                    pageDao, courseDao, chapterDao, lessonDao, taskDao, layout,
                    coursePage = { idx, tr, c, ct, pn ->
                        CoursePage(
                                c, ct, pn, courseLink, chapterLink,
                                lessonLink, taskLink, language, breadCrumbsToPage(idx, tr)
                        )
                    }
            )

            articleLink x { basicArticle ->
                val article = articleDao.findById(basicArticle.id)!!
                val index = pageDao.findByMagic(Page.Magic.Index)!!
                val articles = pageDao.findByMagic(Page.Magic.Articles)!!
                respondHtml { layout(this, ArticlePage(
                        config.siteUrl, articleLink, article, language, config.exposedStaticDir,
                        highlightScript = sandboxScript, beforeContent = breadCrumbsToPage(index, articles)
                )) }
            }

            chapterLink x ChapterHandler(
                    pageDao, courseDao, chapterDao, lessonDao, taskDao, layout
            ) { idx, tr, crs, chp, chpt, prevNext ->
                ChapterPage(
                        chp, chpt, prevNext, chapterLink, lessonLink, taskLink,
                        language, breadCrumbsToCourse(idx, tr, crs)
                )
            }

            codeReviewLink x { review ->
                val idx = pageDao.findByMagic(Page.Magic.Index)!!
                val cr = pageDao.findByMagic(Page.Magic.CodeReview)!!
                respondHtml {
                    layout(this, CodeReviewDetailsPage(
                            review, config.exposedStaticDir, highlightScript = sandboxScript, beforeContent = breadCrumbsToPage(idx, cr)
                    ))
                }
            }

            lessonLink x LessonHandler(
                    courseDao, chapterDao, lessonDao, taskDao, pageDao, layout
            ) { idx, tr, crs, chp, l, lt, prNx -> LessonPage(
                    config.siteUrl, l, lt, prNx, config.exposedStaticDir, lessonLink, reportTaskAction, language, sandboxScript,
                    codeMirrorStyle, breadCrumbsToChapter(idx, tr, crs, chp)
            ) }

            reportTaskAction x SubmitTaskErrorReportHandler(taskErrorReportDao)

            codeReviewAction x SubmitCodeReviewCandidateHandler(codeReviewCandidateDao)
        }

        val errorHandler: suspend (ApplicationCall) -> Unit = { call ->
            val status = call.response.status()!!
            call.respondHtml(status) {
                layout(this, ErrorPage(language, status.value, status.description))
            }
        }

        val articleRssHandler =
                ArticleRssHandler(pageDao, articleDao, pageLink, articleLink, config.siteUrl, language.articlesFeedInfo)
        // TODO: RSS for code reviews

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
        val stat = HitStat(InMemoryStatTable(Just(noUa)))

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
                val errHandler: suspend PipelineContext<*, ApplicationCall>.(Any?) -> Unit = {
                    call.response.status(HttpStatusCode.NotFound)
                    errorHandler(call)
                }
                exception<NotFoundException>(errHandler)
                status(HttpStatusCode.NotFound, handler = errHandler)
            }

            install(WebSockets)

            routing {

                installHitStatInterceptor(stat)

                get("/{parts...}/") {
                    routing.get(call)
                }
                post("/{parts...}/") {
                    routing.post(call)
                }

                get("/articles.rss") { articleRssHandler(call) }

                get("/sitemap.xml") { sitemap(call) }
                get("/robots.txt") { robots(call) }

                webSocket(path = "/sandbox/ws", handler = sandboxWebSocketHandler)

                route("/${config.adminRoute}/") {
                    authentication {
                        digest(name = "admin") {
                            realm = "Admin"
                            userNameRealmPasswordDigestProvider = { userName, realm ->
                                when (userName) {
                                    config.adminUsername -> {
                                        digester.reset()
                                        digester.update("$userName:$realm:${config.adminPassword}".toByteArray())
                                        digester.digest()
                                    }
                                    else -> null
                                }
                            }
                        }
                    }

                    authenticate("admin") {
                        installAdmin(JavaneseAdminPanel(
                                config.adminRoute, session,
                                taskDao, lessonDao, chapterDao, courseDao, articleDao,
                                pageDao, taskErrorReportDao, codeReviewCandidateDao, codeReviewDao,
                                stat
                        ))
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
