package online.javanese.social

import io.ktor.auth.OAuthAccessTokenResponse
import io.ktor.auth.OAuthServerSettings

class OAuthSource(
        val name: String,
        val oauth: OAuthServerSettings?,
        val resolveUserFor: suspend (OAuthAccessTokenResponse) -> User
)