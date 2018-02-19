package online.javanese.page

import kotlinx.html.*
import online.javanese.link.Link
import online.javanese.model.Meta
import online.javanese.model.Page


class CardsPage<T>(
        private val page: Page,
        private val contents: List<Card<T>>
): Layout.Page {

    override val meta: Meta get() = page.meta

    override fun additionalHeadMarkup(head: HEAD) = with(head) {
        unsafe { +page.headMarkup }
    }

    override fun bodyMarkup(body: BODY) = with(body) {
        unsafe { +page.bodyMarkup }

        main(classes = "content mdl-grid") {

            contents.forEach { card ->
                div(classes = "mdl-cell mdl-cell--3-col-desktop mdl-cell--4-col content-padding-v") {
                    card.link.renderCustom(this, card.t, "card mdl-card mdl-shadow--4dp-h") { title ->
                        h5 { +title }
                        div(classes = card.iconClasses)
                        p { +card.subtitle }
                    }
                }
            }
        }
    }

    override fun scripts(body: BODY) = with(body) {
        unsafe { +page.beforeBodyEndMarkup }
    }

    class Card<T>(
            val link: Link<T>,
            val t: T,
            val iconClasses: String,
            val subtitle: String
    )

}
