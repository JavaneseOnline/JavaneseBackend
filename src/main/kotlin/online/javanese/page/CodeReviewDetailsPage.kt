package online.javanese.page

import kotlinx.html.*
import online.javanese.link.HtmlBlock
import online.javanese.model.CodeReview
import online.javanese.model.Meta


class CodeReviewDetailsPage(
        private val codeReview: CodeReview,
        private val static: String,
        private val highlightScript: String,
        private val beforeContent: HtmlBlock
) : Layout.Page {

    override val meta: Meta get() = codeReview.meta

    override fun additionalHeadMarkup(head: HEAD) = Unit

    override fun bodyMarkup(body: BODY) = with(body) {
        contentCardMain {

            beforeContent.render(this)

            h1 {
                +codeReview.meta.title
            }

            blockQuote {
                +codeReview.problemStatement
            }
            p(classes = "mdl-typography--text-right") {
                style = "margin-top: -12px; margin: 0 40px"

                +codeReview.senderName
            }

            hr()

            unsafe {
                +codeReview.reviewMarkup
            }

            vkAndTgPosts(codeReview.vkPostInfo, codeReview.tgPost, true)
        }
    }

    override fun scripts(body: BODY) = with(body) {
        script(src = "$static/$highlightScript") {}
    }

}
