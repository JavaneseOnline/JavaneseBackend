package online.javanese.template

import kotlinx.html.*
import online.javanese.model.Article
import online.javanese.model.Meta
import online.javanese.model.Page
import java.util.*

class ArticlesPage(
        private val page: Page,
        private val articles: List<Article.BasicInfo>,
        private val messages: Properties,
        private val static: String,
        private val urlOfArticle: (Page, Article.BasicInfo) -> String
) : Layout.Page {

    override val meta: Meta get() = page.meta

    override fun additionalHeadMarkup(head: HEAD) = Unit

    override fun bodyMarkup(body: BODY) = with(body) {
        contentCardDiv {
            nav {
                a(href = "/", titleAndText = messages.getProperty("index.title"))
            }

            main {
                h1(classes = "content-padding-v") {
                    +page.h1
                }

                unsafe {
                    +page.bodyMarkup
                }
            }

            nav {
                ul {
                    articles.forEach { article ->
                        li {
                            if (article.pinned)
                                img(src = "$static/pin.png")

                            a(href = urlOfArticle(page, article), titleAndText = article.linkText)
                        }
                    }
                }
            }
        }
    }

    override fun scripts(body: BODY) = Unit

}
