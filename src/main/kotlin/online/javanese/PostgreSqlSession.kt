package online.javanese

import com.github.andrewoma.kwery.core.DefaultSession
import com.github.andrewoma.kwery.core.Session
import com.github.andrewoma.kwery.core.dialect.PostgresDialect
import java.sql.DriverManager
import java.util.*

fun PostgreSqlSession(
        dbName: String, user: String, password: String
): Session {
    org.postgresql.Driver::class.java
    val connection = DriverManager.getConnection(
            "jdbc:postgresql:$dbName",
            Properties().also {
                it["user"] = user
                it["password"] = password
            }
    )
    return DefaultSession(
            connection,
            PostgresDialect()
    )
}
