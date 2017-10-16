package online.javanese.template

import online.javanese.model.RssItem

class RssFeedTemplate(
        private val render: (templateName: String, params: Map<String, Any>) -> String
) : (List<RssItem>) -> String {

    override fun invoke(items: List<RssItem>): String = render(
            "rss",
            mapOf("items" to items)
    )

}
