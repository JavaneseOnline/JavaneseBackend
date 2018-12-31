package online.javanese.page

import kotlinx.html.*
import online.javanese.link.HtmlBlock
import online.javanese.link.Link
import online.javanese.locale.Language
import online.javanese.model.Article
import online.javanese.model.Meta


class ArticlePage(
        private val siteUrl: String,
        private val articleLink: Link<Article.BasicInfo, *>,
        private val article: Article,
        private val language: Language,
        private val static: String,
        private val highlightScript: String,
        private val beforeContent: HtmlBlock
) : Layout.Page {

    override val meta: Meta get() = article.basicInfo.meta

    override fun additionalHeadMarkup(head: HEAD) = Unit

    override fun bodyMarkup(body: BODY) = with(body) {
        contentCardMain {

            beforeContent.render(this)

            h1(classes = "content-padding-v") {
                +article.heading
            }

            vkOpenApiScript() // required also for comments
            // the script is invisible, so don't let it be :last-child, because this will eat indent

            article {
                unsafe {
                    +article.bodyMarkup
                }
            }

            vkAndTgPosts(article.vkPostInfo, article.tgPost, false)
        }

        section(classes = "content container-margin-t") {
            h4 {
                +language.articleComments
            }
            vkComments(
                    siteUrl, articleLink.url(article.basicInfo), article.basicInfo.id.toString(),
                    init = true, classes = "no-pad container-margin-t mdl-shadow--8dp"
            )
        }
    }

    override fun scripts(body: BODY) = with(body) {
        script(src = "$static/$highlightScript") {}
    }

}
