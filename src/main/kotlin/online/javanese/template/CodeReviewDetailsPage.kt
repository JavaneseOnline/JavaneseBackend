package online.javanese.template

import kotlinx.html.*
import online.javanese.extensions.contentDiv
import online.javanese.model.CodeReview
import online.javanese.model.Meta
import online.javanese.model.Page
import java.util.*

class CodeReviewDetailsPage(
        private val codeReviewsPage: Page,
        private val codeReview: CodeReview,
        private val locale: Properties,
        private val urlOfPage: (Page) -> String
) : Layout.Page {

    override val meta: Meta get() = codeReview.meta

    override fun additionalHeadMarkup(head: HEAD) = Unit

    override fun bodyMarkup(body: BODY) = with(body) {
        contentDiv {

            nav {
                a(href = "/") { +locale.getProperty("index.title") }
                +" / "
                a(href = urlOfPage(codeReviewsPage)) { +codeReviewsPage.meta.title }
            }

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

        }
    }

    override fun scripts(body: BODY) = Unit

}
