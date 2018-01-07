package online.javanese.template

import kotlinx.html.BODY
import kotlinx.html.HEAD
import kotlinx.html.unsafe
import online.javanese.model.Meta
import online.javanese.model.Page

class IndexPage(
        private val page: Page
): Layout.Page {

    override val meta: Meta get() = page.meta

    override fun additionalHeadMarkup(head: HEAD) = with(head) {
        unsafe { +page.headMarkup }
    }

    override fun bodyMarkup(body: BODY) = with(body) {
        unsafe { +page.bodyMarkup }
    }

    override fun scripts(body: BODY) = with(body) {
        unsafe { +page.beforeBodyEndMarkup }
    }

}
