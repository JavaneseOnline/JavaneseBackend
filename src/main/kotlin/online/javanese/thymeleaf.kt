package online.javanese

import org.thymeleaf.TemplateEngine
import org.thymeleaf.messageresolver.IMessageResolver
import org.thymeleaf.messageresolver.StandardMessageResolver
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.thymeleaf.templateresolver.ITemplateResolver
import java.io.InputStream
import java.nio.charset.Charset

fun ClassLoaderTemplateResolver(
        classLoader: ClassLoader,
        prefix: String,
        suffix: String,
        templateMode: TemplateMode,
        charset: Charset
) = ClassLoaderTemplateResolver(classLoader).also {
    it.prefix = prefix
    it.suffix = suffix
    it.templateMode = templateMode
    it.characterEncoding = charset.name()
}

fun MessageResolver(stream: InputStream) = StandardMessageResolver().also {
    it.defaultMessages.load(stream)
}

fun TemplateEngine(
        templateResolver: ITemplateResolver,
        messageResolver: IMessageResolver
) = TemplateEngine().also {
    it.addTemplateResolver(templateResolver)
    it.addMessageResolver(messageResolver)
}
