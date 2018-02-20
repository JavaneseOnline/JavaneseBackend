package online.javanese.page

import kotlinx.html.*
import online.javanese.link.HtmlBlock
import online.javanese.link.Link
import online.javanese.link.NoHtmlBlock
import online.javanese.model.Meta
import online.javanese.model.Page


class CardsPage<T>(
        private val page: Page,
        private val beforeContent: HtmlBlock = NoHtmlBlock,
        private val contents: List<Card<T>>,
        private val dCount: CountOnDesktop = CountOnDesktop.Four,
        private val tCount: CountOnTablet = CountOnTablet.Two
): Layout.Page {

    override val meta: Meta get() = page.meta

    override fun additionalHeadMarkup(head: HEAD) = with(head) {
        unsafe { +page.headMarkup }
    }

    override fun bodyMarkup(body: BODY) = with(body) {
        unsafe { +page.bodyMarkup }


        main(classes = "content mdl-grid") {

            beforeContent.render(this, "mdl-cell mdl-cell--12-col")

            contents.forEach { card ->
                div(classes = "mdl-cell mdl-cell--${dCount.cols}-col-desktop mdl-cell--${tCount.cols}-col content-padding-v") {
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

    enum class CountOnDesktop(val cols: Int) {
        Three(4),
        Four(3)
    }

    enum class CountOnTablet(val cols: Int) {
        One(8),
        Two(4)
    }

}
