package online.javanese.template

import online.javanese.model.Article
import online.javanese.model.Page

class ArticlePageTemplate(
        private val render: (templateName: String, args: Map<String, Any>) -> String
) : (Page, Article) -> String {

    override fun invoke(page: Page, article: Article): String =
            render(
                    "article",
                    mapOf(
                            "articlesPage" to page,
                            "article" to article
                    )
            )

}
