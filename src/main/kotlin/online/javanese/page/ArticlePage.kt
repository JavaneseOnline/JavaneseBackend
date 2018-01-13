package online.javanese.page

import kotlinx.html.*
import online.javanese.locale.Language
import online.javanese.model.Article
import online.javanese.model.Meta
import online.javanese.model.Page

class ArticlePage(
        private val indexPage: Page,
        private val articlesPage: Page,
        private val article: Article,
        private val language: Language,
        private val pageLink: Link<Page>
) : Layout.Page {

    override val meta: Meta get() = article.meta

    override fun additionalHeadMarkup(head: HEAD) = Unit

    override fun bodyMarkup(body: BODY) = with(body) {
        contentCardMain {
            nav {
                pageLink.render(this, indexPage)
                +" / "
                pageLink.render(this, articlesPage)
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
                        unsafe {
                            +"""VK.Widgets.Post('vk_post_$vkPostId', '${vkPostIdParts[0]}', '${vkPostIdParts[1]}', '$vkPostHash');"""
                        }
                    }
                }
            }
        }

        section(classes = "content container-margin-t") {
            h4 {
                +language.articleComments
            }
            vkComments(
                    article.basicInfo.id.toString(), init = true, classes = "no-pad container-margin-t mdl-shadow--8dp"
            )
        }
    }

    override fun scripts(body: BODY) = Unit

}
