package online.javanese.page

import kotlinx.html.*
import online.javanese.model.Article
import online.javanese.model.Meta
import online.javanese.model.Page


class ArticlesPage(
        private val indexPage: Page,
        private val page: Page,
        private val articles: List<Article.BasicInfo>,
        private val static: String,
        private val pageLink: Link<Page>,
        private val articleLink: Link<Article.BasicInfo>
) : Layout.Page {

    override val meta: Meta get() = page.meta

    override fun additionalHeadMarkup(head: HEAD) = Unit

    override fun bodyMarkup(body: BODY) = with(body) {
        contentCardMain {
            nav {
                pageLink.render(this, indexPage)
            }

            main {
                h1(classes = "content-padding-v") {
                    +page.heading
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

                            articleLink.render(this, article)
                        }
                    }
                }
            }
        }
    }

    override fun scripts(body: BODY) = Unit

}
