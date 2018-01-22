package online.javanese.page

import kotlinx.html.*
import online.javanese.locale.Language
import online.javanese.model.Meta


interface Layout : (HTML, Layout.Page) -> Unit {

    interface Page {
        val meta: Meta
        fun additionalHeadMarkup(head: HEAD)
        fun bodyMarkup(body: BODY)
        fun scripts(body: BODY)
    }

}

class MainLayout(
        private val static: String,
        private val mainStyle: String,
        private val mainScript: String,
        private val language: Language
) : Layout {

    override fun invoke(root: HTML, page: Layout.Page) = with(root) {
        head {
            unsafe {
                +"\n    <meta charset=\"utf-8\" />"
                +"\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />"
            }

            title("${page.meta.title} — ${language.siteTitle}")

            // Tenor Sans, PT Sans, Material Icons; mdl, dialog polyfill, highlight darkula, custom styles, sandbox, trace
            styleLink("$static/$mainStyle")

            link(rel = "alternate", type = "application/rss+xml", href = "/articles.rss") {
                attributes["title"] = "Статьи"
            }

            meta(name = "description", content = page.meta.description)
            meta(name = "keywords", content = page.meta.keywords)

            page.additionalHeadMarkup(this)
        }

        body {

            // fixme: move to locale-dependent place
            header {
                div(classes = "content content-padding-h") {
                    h1(classes = "no-pad-top") { +"Javanese Online" }

                    ul {
                        li { a(href = "javascript:showGitHubRepos();", title = "Javanese.Online в GitHub") {
                                div(classes = "ic_github_white24")
                        } }
                        li { a(href = "https://vk.com/javanese.online", title = "группа ВКонтакте, где публикуются ссылки на статьи") {
                            div(classes = "ic_vk_white24")
                        } }
                        /*li { a(href = "https://docs.google.com/forms/d/e/1FAIpQLSeNweNKyWk8lU61mRgXrRspOfCTCZpTi58J1N82j6K_IBK_Yg/viewform", title = "русскоязычный Slack о Java и Android") {
                            div(classes = "ic_slack_white24")
                        } }*/
                        li { a(href = "javascript:showTelegramChatsAndChannels();", title = "Чаты и каналы в Telegram") {
                            div(classes = "ic_tg_white24")
                        } }
                        li { a(href = "javascript:showRssFeeds();", title = "RSS-лента") {
                            div(classes = "ic_rss_white24")
                        } }
                    }
                    p(classes = "no-pad")
                }
            }

            page.bodyMarkup(this)

            button(classes = "mdl-button mdl-js-button mdl-button--fab") {
                id = "up-button"

                i("material-icons") { +"keyboard_arrow_up" }
            }

            // Vue.js, Zepto, Material Design Lite, dialog polyfill, highlight.js, trace, scroll to top,
            // blur link on click, mdl tabs fix to work with url #fragment, async form handling
            script(src = "$static/$mainScript")

            div(classes = "mdl-js-snackbar mdl-snackbar") {
                id = "toast-container"

                div(classes="mdl-snackbar__text")
                button(classes="mdl-snackbar__action")
            }

            page.scripts(this)

            materialDialog("github-dialog") {
                materialDialogTitle { +"Javanese.Online в GitHub" }
                materialDialogBodyUl {
                    li { a(href = "https://github.com/JavaneseOnline", titleAndText = "Исходный код Javanese.Online") }
                    li { a(href = "https://github.com/JavaneseOnlineCodeReviews", titleAndText = "Форки разобранного кода") }
                }
                materialDialogActions {
                    materialButton(ButtonType.button, "close") {
                        onClick = "this.parentNode.parentNode.close();"
                        +"Закрыть"
                    }
                }
            }

            materialDialog("telegram-dialog") {
                materialDialogTitle { +"Чаты и каналы в Telegram" }
                materialDialogBodyUl {
                    li {
                        a(href = "http://t.me/javanese_online", titleAndText = "Канал Javanese.Online")
                        +" — новости, статьи, кодревью"
                    }
                    li {
                        a(href = "http://t.me/javanese_questions", titleAndText = "Javanese Questions")
                        +" — чат строго в формате вопрос-ответ"
                    }
                    li {
                        a(href = "http://t.me/kotlin_lang", titleAndText = "Kotlin Community")
                        +", "
                        a(href = "http://t.me/kotlin_jvm", titleAndText = "Kotlin JVM")
                        +", "
                        a(href = "http://t.me/kotlin_mobile", titleAndText = "Kotlin Android")
                        +" — чаты о Kotlin"
                    }
                }
                materialDialogActions {
                    materialButton(ButtonType.button, "close") {
                        onClick = "this.parentNode.parentNode.close();"
                        +"Закрыть"
                    }
                }
            }

            materialDialog("rss-dialog") {
                materialDialogTitle { +"RSS-лента" }
                materialDialogBodyUl {
                    li { a(href = "//javanese.online/articles.rss", titleAndText = "RSS-лента со статьями") }
                }
                materialDialogActions {
                    materialButton(ButtonType.button, "close") {
                        onClick = "this.parentNode.parentNode.close();"
                        +"Закрыть"
                    }
                }
            }

            script {
                unsafe {
                    +"'use strict';"
                    +"var githubDialog = document.getElementById('github-dialog');"
                    +"var telegramDialog = document.getElementById('telegram-dialog');"
                    +"var rssFeedsDialog = document.getElementById('rss-dialog');"
                    +"if (!githubDialog.showModal) {"
                        +"dialogPolyfill.registerDialog(githubDialog);"
                        +"dialogPolyfill.registerDialog(telegramDialog);"
                        +"dialogPolyfill.registerDialog(rssFeedsDialog);"
                    +"}"

                    +"function showGitHubRepos () { githubDialog.showModal(); }"
                    +"function showTelegramChatsAndChannels () { telegramDialog.showModal() }"
                    +"function showRssFeeds () { rssFeedsDialog.showModal() }"
                }
            }
        }
    }

}
