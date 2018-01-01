
Это исходники новой, ещё не готовой Javanese.Online.

Старая версия была написана на
Java 8 + Kotlin, Spring, Hibernate, Thymeleaf, SCSS.

Стек новой версии:
* JVM/JDK 9
* [Kotlin](https://github.com/JetBrains/kotlin)
* [Ktor — каноничный веб-фреймворк от JetBrains](https://github.com/kotlin/ktor)
* [kwery — обёртка над JDBC для Kotlin](https://github.com/andrewoma/kwery/)
* [Thymeleaf](https://github.com/thymeleaf/thymeleaf) с
  [layout dialect](https://github.com/ultraq/thymeleaf-layout-dialect) (временно)
* [kotlinx.html](https://github.com/Kotlin/kotlinx.html) в качестве быстрого и гибкого шаблонизатора
* SCSS (временно)
* PostgreSQL

Сделано:
* Сайт переписан на этом стеке и работает так же, как старая версия;
* сделаны новые фичи, в частности, кодревью и закреплённые статьи.

Ещё предстоит сделать:
* вычистить код
  * отказаться от Thymeleaf и SCSS в пользу типобезопасных DSL;
  * сделать что-то с модельками,
    которые у себя в конструкторе делают выборку субмоделек, передавая this
  * разделить всё на модули Java 9

VM options.
  * debug: `-Xss175K -Xms16M -Xmx32M`
  * release: `-Xss256K -Xms20M -XmxСколькоНеЖалко`
