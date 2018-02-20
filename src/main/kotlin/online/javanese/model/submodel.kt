package online.javanese.model

class Meta(
        val title: String,
        val description: String,
        val keywords: String
)

class VkPostInfo(
        val id: String,
        val hash: String
) {

    companion object {
        val Empty = VkPostInfo("", "")

        fun fromComponentsOrNull(id: String, hash: String): VkPostInfo? {
            if (id.isBlank()) return null
            if (hash.isBlank()) return null
            return VkPostInfo(
                    id = id,
                    hash = hash
            )
        }
    }

}

