package online.javanese

import java.util.*

class Config(
        props: Properties
) {

    val dbName: String = props.getProperty("database")
    val dbUser: String = props.getProperty("user")
    val dbPassword: String = props.getProperty("password")

    val localStaticDir: String? = props.getProperty("localStaticDir")
    val exposedStaticDir: String = props.getProperty("exposedStaticDir")

}
