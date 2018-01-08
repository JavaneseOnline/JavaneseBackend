package online.javanese.template

import kotlinx.html.*
import online.javanese.model.Meta
import java.util.*

class ErrorPage(
        private val messages: Properties,
        private val statusCode: Int,
        reason: String
) : Layout.Page {

    private val reason = messages.getProperty("error.$statusCode.reason", reason)

    override val meta: Meta = Meta(
            reason,
            "Страница ошибки $statusCode.",
            "Шутите? Какие ключевые слова могут быть у страницы ошибки?"
    )

    override fun additionalHeadMarkup(head: HEAD) = Unit

    override fun bodyMarkup(body: BODY) = with(body) {
        contentCardDiv {
            nav {
                a(href ="/", titleAndText = messages.getProperty("index.title"))
            }

            main {
                h1(classes = "content-padding-v") {
                    +messages.getProperty("error")
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
