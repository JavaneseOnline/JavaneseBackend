package online.javanese.repository

import online.javanese.model.Page
import online.javanese.model.PageDao

class PageRepository internal constructor(
        private val dao: PageDao
) {

    fun findByUrlPathComponent(component: String): Page? =
            dao.findByUrlPathComponent(component)

}
