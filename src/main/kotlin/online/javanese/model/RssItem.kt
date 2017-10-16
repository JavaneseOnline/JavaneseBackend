package online.javanese.model

import java.time.LocalDateTime

class RssItem(
        val title: String,
        val description: String,
        val link: String,
        val pubDate: LocalDateTime
)