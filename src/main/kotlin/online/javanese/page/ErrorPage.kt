package online.javanese.page

import kotlinx.html.*
import online.javanese.locale.Language
import online.javanese.model.Meta

class ErrorPage(
        private val language: Language,
        private val statusCode: Int,
        reason: String
) : Layout.Page {

    private val reason = language.httpErrors[statusCode] ?: reason

    override val meta: Meta = Meta(
            reason, // todo: move to Locale
            "Страница ошибки $statusCode.",
            "Шутите? Какие ключевые слова могут быть у страницы ошибки?"
    )

    override fun additionalHeadMarkup(head: HEAD) = Unit

    override fun bodyMarkup(body: BODY) = with(body) {
        contentCardMain {
            nav {
                a(href ="/", titleAndText = language.siteTitle)
            }

            main {
                h1(classes = "content-padding-v") {
                    +language.error
                    +" "
                    +statusCode.toString()
                }

                p {
                    +reason
                }
            }
        }
    }

    override fun scripts(body: BODY) = Unit

}
