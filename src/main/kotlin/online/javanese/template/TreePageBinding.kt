package online.javanese.template

import online.javanese.model.Page
import online.javanese.repository.CourseTree
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.util.*

class TreePageBinding(
        private val staticResourcesDir: String,
        private val templateEngine: TemplateEngine,
        private val locale: Locale
): (Page, List<CourseTree>) -> String {

    override fun invoke(page: Page, courses: List<CourseTree>): String {
        return templateEngine.process(
                "tree",
                Context(locale,
                        mapOf(
                                "page" to page,
                                "courses" to courses,
                                "static" to staticResourcesDir
                        )
                )
        )
    }

}
