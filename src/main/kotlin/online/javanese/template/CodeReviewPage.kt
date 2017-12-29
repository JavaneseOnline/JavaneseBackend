package online.javanese.template

import kotlinx.html.*
import online.javanese.extensions.contentDiv
import online.javanese.extensions.encodeForUrl
import online.javanese.extensions.menu
import online.javanese.model.CodeReview
import online.javanese.model.Meta
import online.javanese.model.Page
import java.util.*

class CodeReviewPage(
        private val model: Page,
        private val locale: Properties,
        private val reviews: List<CodeReview>
) : Layout.Page {

    override val meta: Meta get() = model.meta

    override fun additionalHeadMarkup(head: HEAD) = Unit

    override fun bodyMarkup(body: BODY) = with(body) {
        contentDiv {
            nav {
                a(href = "/") {
                    +locale.getProperty("index.title")
                }
            }

            div(classes = "no-pad mdl-tabs mdl-js-tabs mdl-js-ripple-effect content-padding-b") {

                menu(classes = "mdl-tabs__tab-bar mdl-color-text--grey-600") {
                    a(href = "#reviews", classes = "mdl-tabs__tab is-active") { +"Читать" } // todo: move to locale
                    a(href = "#submit", classes = "mdl-tabs__tab") { +"Разберите мой код!" }
                }

                nav(classes = "mdl-tabs__panel is-active") {
                    id = "reviews"

                    ul {
                        reviews.forEach { review ->
                            li {
                                a(href = "/${model.urlPathComponent.encodeForUrl()}/${review.urlSegment}/") {
                                    +review.meta.title
                                }
                            }
                        }
                    }
                }

                nav(classes = "mdl-tabs__panel") {
                    id = "submit"

                    h2 {
                        +model.h1
                    }

                    unsafe {
                        +model.bodyMarkup
                    }
                }
            }
        }
    }

    override fun scripts(body: BODY) = with(body) {
        unsafe {
            +model.beforeBodyEndMarkup
        }
    }

}
