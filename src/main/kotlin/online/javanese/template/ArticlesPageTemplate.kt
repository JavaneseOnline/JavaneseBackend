package online.javanese.template

import online.javanese.model.Article
import online.javanese.model.Page

class ArticlesPageTemplate(
        private val render: (String, Map<String, Any>) -> String
): (Page, List<Article.BasicInfo>) -> String {

    override fun invoke(page: Page, articles: List<Article.BasicInfo>): String {
        return render(
                "articles",
                mapOf(
                        "page" to page,
                        "articles" to articles
                )
        )
    }

}
