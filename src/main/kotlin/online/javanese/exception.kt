package online.javanese.exception

import io.ktor.http.HttpStatusCode

abstract class HttpException(
        val status: HttpStatusCode,
        override val message: String  // nullable overridden with non-null
) : Exception()

class BadRequestException(message: String) : HttpException(HttpStatusCode.BadRequest, message)
class UnauthorizedException(message: String) : HttpException(HttpStatusCode.Unauthorized, message)
class ForbiddenException(message: String) : HttpException(HttpStatusCode.Forbidden, message)
class NotFoundException(message: String) : HttpException(HttpStatusCode.NotFound, message)
