package online.javanese.template

import online.javanese.model.Page

class IndexPageTemplate(
        private val render: (String, Map<String, Any>) -> String
): (Page) -> String {

    override fun invoke(page: Page): String =
            render("index", mapOf("page" to page))

}
