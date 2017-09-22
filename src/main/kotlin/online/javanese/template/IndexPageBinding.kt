package online.javanese.template

import online.javanese.model.Page
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.util.*

class IndexPageBinding(
        private val templateEngine: TemplateEngine,
        private val locale: Locale
): (Page) -> String {

    override fun invoke(page: Page): String {
        return templateEngine.process(
                "index",
                Context(locale,
                        mapOf(
                                "page" to page
                        )
                )
        )
    }

}
