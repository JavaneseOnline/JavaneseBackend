package online.javanese.page

import kotlinx.html.*
import online.javanese.link.Link
import online.javanese.link.encodeForUrl
import online.javanese.locale.Language
import online.javanese.model.CodeReview
import online.javanese.model.Meta
import online.javanese.model.Page

class CodeReviewPage(
        private val indexPage: Page,
        private val model: Page,
        private val reviews: List<CodeReview>,
        private val pageLink: Link<Page>,
        private val language: Language
) : Layout.Page {

    override val meta: Meta get() = model.meta

    override fun additionalHeadMarkup(head: HEAD) = Unit

    override fun bodyMarkup(body: BODY) = with(body) {
        contentCardMain {
            nav {
                pageLink.render(this, indexPage)
            }

            div(classes = "no-pad mdl-tabs mdl-js-tabs mdl-js-ripple-effect content-padding-b") {

                menu(classes = "mdl-tabs__tab-bar mdl-color-text--grey-600") {
                    a(href = "#reviews", classes = "mdl-tabs__tab is-active") { +language.readCodeReviews }
                    a(href = "#submit", classes = "mdl-tabs__tab") { +language.submitCodeReview }
                }

                nav(classes = "mdl-tabs__panel is-active") {
                    id = "reviews"

                    ul {
                        reviews.forEach { review ->
                            li { // fixme: link as object
                                a(href = "/${model.urlSegment.encodeForUrl()}/${review.urlSegment}/") {
                                    +review.meta.title
                                }
                            }
                        }
                    }
                }

                nav(classes = "mdl-tabs__panel") {
                    id = "submit"

                    h2 {
                        +model.heading
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
