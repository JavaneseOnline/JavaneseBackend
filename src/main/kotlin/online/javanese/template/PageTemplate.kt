package online.javanese.template

import online.javanese.model.Page

class PageTemplate(
        private val render: (String, Map<String, Any>) -> String
): (Page) -> String {

    override fun invoke(page: Page): String =
            render("page", mapOf("page" to page))

}
