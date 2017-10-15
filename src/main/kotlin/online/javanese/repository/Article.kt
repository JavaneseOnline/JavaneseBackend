package online.javanese.repository

import online.javanese.model.Article
import online.javanese.model.ArticleDao

class ArticleRepository internal constructor(
        private val articleDao: ArticleDao
) {

    fun findAllBasicPublishedOrderBySortIndex(): List<Article.BasicInfo> =
            articleDao.findAllBasicPublishedOrderBySortIndex()

    fun findByUrlComponent(component: String): Article? =
            articleDao.findByUrlComponent(component)

}
