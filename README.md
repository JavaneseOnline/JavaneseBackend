
[Javanese.Online](http://javanese.online/) website source code.

Stack:
* JDK 13
* [Kotlin](https://github.com/JetBrains/kotlin) 1.4
* [Ktor](https://github.com/kotlin/ktor) — idiomatic web-framework by JetBrains
* [kwery](https://github.com/andrewoma/kwery/) — JDBC wrapper for Kotlin
* [kotlinx.html](https://github.com/Kotlin/kotlinx.html) — fast template engine
* SCSS
* PostgreSQL

Running.
  * debug.
    * VM options: `-Xss256K -Xms16M -Xmx32M -XX:+UnlockExperimentalVMOptions -XX:+TrustFinalNonStaticFields`
    * program arguments: `--single-thread`
  * release. VM options: `-Xss256K -Xms32M -XmxAsMuchAsYouCanGive -XX:+UnlockExperimentalVMOptions -XX:+TrustFinalNonStaticFields`

You can find database structure in `model` package;
database contents are not publicly available.
