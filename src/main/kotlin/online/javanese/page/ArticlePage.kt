package online.javanese.page

import kotlinx.html.BODY
import kotlinx.html.FlowContent
import kotlinx.html.HEAD
import kotlinx.html.article
import kotlinx.html.h1
import kotlinx.html.h4
import kotlinx.html.id
import kotlinx.html.script
import kotlinx.html.section
import kotlinx.html.unsafe
import online.javanese.link.HtmlBlock
import online.javanese.locale.Language
import online.javanese.model.Article
import online.javanese.model.Meta


class ArticlePage(
        private val article: Article,
        private val language: Language,
        private val static: String,
        private val highlightScript: String,
        private val beforeContent: HtmlBlock,
        private val renderComments: FlowContent.(fragment: String) -> Unit
) : Layout.Page {

    override val meta: Meta get() = article.basicInfo.meta

    override fun additionalHeadMarkup(head: HEAD) = Unit

    override fun bodyMarkup(body: BODY) = with(body) {
        contentCardMain {

            beforeContent.render(this)

            h1(classes = "content-padding-v") { +article.heading }

            article {
                unsafe {
                    +article.bodyMarkup
                }
            }

            vkAndTgPosts(article.vkPostInfo, article.tgPost, true)
        }

        section(classes = "content container-margin-t") {
            h4 {
                id = "comments_section"
                +language.articleComments
            }
            card(moreClasses = "no-pad") {
                renderComments("comments_section")
            }
        }
    }

    override fun scripts(body: BODY) = with(body) {
        script(src = "$static/$highlightScript") {}
    }

}
