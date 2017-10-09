package online.javanese.template

import online.javanese.model.Page
import online.javanese.repository.CourseTree

class TreePageTemplate(
        private val render: (String, Map<String, Any>) -> String
): (Page, List<CourseTree>) -> String {

    override fun invoke(page: Page, courses: List<CourseTree>): String =
            render(
                    "tree",
                    mapOf(
                            "page" to page,
                            "courses" to courses
                    )
            )

}
