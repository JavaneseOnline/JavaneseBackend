package online.javanese.exception

class NotFoundException(
        override val message: String // nullable overridden with non-null
) : Exception(message)
