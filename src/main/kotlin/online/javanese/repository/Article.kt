package online.javanese.repository

import online.javanese.model.Article
import online.javanese.model.ArticleDao

class ArticleRepository internal constructor(
        private val articleDao: ArticleDao
) {

    fun findAllBasicOrderBySortIndex(): List<Article.BasicInfo> =
            articleDao.findAllBasicOrderBySortIndex()

    fun findByUrlComponent(component: String): Article? =
            articleDao.findByUrlComponent(component)

}
