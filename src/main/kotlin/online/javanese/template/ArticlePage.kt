package online.javanese.template

import kotlinx.html.*
import online.javanese.model.Article
import online.javanese.model.Meta
import online.javanese.model.Page
import java.util.*

class ArticlePage(
        private val articlesPage: Page,
        private val article: Article,
        private val messages: Properties,
        private val urlOfPage: (Page) -> String
) : Layout.Page {

    override val meta: Meta get() = article.meta

    override fun additionalHeadMarkup(head: HEAD) = Unit

    override fun bodyMarkup(body: BODY) = with(body) {
        contentCardMain {
            nav {
                a(href = "/", titleAndText = messages.getProperty("index.title"))
                +" / "
                a(href = urlOfPage(articlesPage), titleAndText = articlesPage.meta.title) // todo: should I use linkText?
            }

            h1(classes = "content-padding-v") {
                +article.heading
            }

            script(src = "//vk.com/js/api/openapi.js?136") {
                attributes["id"] = "vk_openapi_js"
            }

            article {
                unsafe {
                    +article.bodyMarkup
                }
            }

            article.vkPostInfo?.let { postInfo ->
                section {
                    div {
                        id = "vk_post_${article.vkPostInfo.id}"
                    }
                    script {
                        val vkPostId = postInfo.id
                        val vkPostHash = postInfo.hash
                        val vkPostIdParts = vkPostId.split('_')
                        +"""VK.Widgets.Post('vk_post_$vkPostId', '${vkPostIdParts[0]}', '${vkPostIdParts[1]}', '$vkPostHash');"""
                    }
                }
            }
        }

        section(classes = "content container-margin-t") {
            h4 {
                +messages.getProperty("article.comments")
            }
            div(classes = "no-pad container-margin-t mdl-shadow--8dp") {
                id = "vk_comments"
            }
            script {
                +"""VK.init({ apiId: 5748800, onlyWidgets: true });"""
                +"""VK.Widgets.Comments("vk_comments", {}, '${article.basicInfo.id}');"""
            }
        }
    }

    override fun scripts(body: BODY) = Unit

}
