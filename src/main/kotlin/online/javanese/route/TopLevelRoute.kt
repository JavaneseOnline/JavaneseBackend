package online.javanese.route

import online.javanese.model.Page
import online.javanese.model.PageDao
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.response.respondText

/*class TopLevelRoute(
        private val pageDao: PageDao,
        private val indexTemplateBinding: (Page) -> String
) : (ApplicationCall, String) -> Unit {

    override fun invoke(call: ApplicationCall, query: String) {
        val page = pageDao.findByUrlPathComponent(query)
        call.respondText(indexTemplateBinding(page!!), ContentType.Text.Html)
    }

}*/


fun topLevelRouteFactory(
        pageDao: PageDao, indexPageBinding: (Page) -> String
) : suspend (ApplicationCall, String) -> Unit = { call, query ->
    val page = pageDao.findByUrlPathComponent(query)!! // todo: handle other cases
    call.respondText(indexPageBinding(page), ContentType.Text.Html)
}
