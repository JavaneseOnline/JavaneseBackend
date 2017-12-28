package online.javanese.template

import kotlinx.html.*
import online.javanese.model.Meta
import online.javanese.model.Page
import java.util.*

class CodeReviewPage(
        private val model: Page,
        private val locale: Properties
) : Layout.Page {

    override val meta: Meta get() = model.meta

    override fun additionalHeadMarkup(head: HEAD) = Unit

    override fun bodyMarkup(body: BODY) = with(body) {
        div(classes = "content card mdl-shadow--8dp") {
            nav {
                a(href = "/") {
                    +locale.getProperty("index.title")
                }
            }

            h1 {
                +model.h1
            }

            unsafe {
                +model.bodyMarkup
            }
        }
    }

    override fun scripts(body: BODY) = with(body) {
        unsafe {
            +model.beforeBodyEndMarkup
        }
    }

}
