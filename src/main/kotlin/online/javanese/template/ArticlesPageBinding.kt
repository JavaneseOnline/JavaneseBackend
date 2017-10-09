package online.javanese.template

import online.javanese.model.Article
import online.javanese.model.Page
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.util.*

class ArticlesPageBinding(
        private val staticResourcesDir: String,
        private val templateEngine: TemplateEngine,
        private val locale: Locale
): (Page, List<Article.BasicInfo>) -> String {

    override fun invoke(page: Page, articles: List<Article.BasicInfo>): String {
        return templateEngine.process(
                "articles",
                Context(locale,
                        mapOf(
                                "page" to page,
                                "articles" to articles,
                                "static" to staticResourcesDir
                        )
                )
        )
    }

}
