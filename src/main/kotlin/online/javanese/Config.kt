package online.javanese

import java.util.*

class Config(
        props: Properties
) {

    val dbName: String = props.getProperty("database")
    val dbUser: String = props.getProperty("user")
    val dbPassword: String = props.getProperty("password")

    // //static.javanese.online in production,
    // //$listenHost:$listenPort/s/t/a/t/i/c (e. g. //localhost:8080/s/t/a/t/i/c) to serve static content from classpath
    val exposedStaticDir: String = props.getProperty("exposedStaticDir")

    // http://javanese.online
    val listenHost: String = props.getProperty("listenHost")
    val listenPort: Int = props.getProperty("listenPort").toInt()
    val siteHost: String = props.getProperty("siteHost")
    val siteUrl: String = "http://$siteHost"

    // /usr/lib/jvm/java-9-oracle/bin/java
    val sandboxJavaLocation: String = props.getProperty("sandbox.javaLocation")
    // /home/<user>/IdeaProjects/javanese/etc/sandbox
    val sandboxLocation: String = props.getProperty("sandbox.location")
    // /home/miha/IdeaProjects/javanese/etc/commons-cli-1.3.1.jar FIXME: may write zero-dependency CLI parser
    val sandboxCommonsCli: String = props.getProperty("sandbox.commons.cli")

    val adminRoute: String = props.getProperty("admin.route")
    val adminUsername: String = props.getProperty("admin.username")
    val adminPassword: String = props.getProperty("admin.password")

}
