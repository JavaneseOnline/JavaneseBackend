import java.io.File
import java.io.OutputStreamWriter
import java.net.URL

val main = "js/vue_zepto_mdl_dialog_highlight_trace_scroll_unfocus_tabs_form"
val nl = charArrayOf('\n')


jsFile(main).also { check(!it.exists()) }.writer().use {
    appendFrom(URL("https://cdnjs.cloudflare.com/ajax/libs/vue/1.0.25/vue.min.js"), it) //todo: upgrade
    appendFrom(URL("http://zeptojs.com/zepto.min.js"), it)
    appendFrom(URL("https://raw.githubusercontent.com/madrobby/zepto/master/src/fx.js"), it)
    appendFrom(URL("https://raw.githubusercontent.com/madrobby/zepto/master/src/fx_methods.js"), it)

    it.write(
            URL("https://raw.githubusercontent.com/suprMax/ZeptoScroll/master/static/zepto.scroll.js")
                    .openConnection().getInputStream().reader().readText()
                    .replace("$.os.android ? 1 : 0", "0") // we're not using Zepto.detect
    )
    it.write(nl)

    appendFrom(URL("https://code.getmdl.io/1.1.3/material.min.js"), it) // TODO: upgrade whole MDL
    appendFrom(URL("https://raw.githubusercontent.com/GoogleChrome/dialog-polyfill/master/dialog-polyfill.js"), it)
    appendFrom(File("js/trace.js"), it)
    appendFrom(File("js/highlight.pack.js"), it)
    appendFrom(File("js/scroll_unfocus_tabs.js"), it)
    appendFrom(File("js/form.js"), it)
}
minify(main)


fun jsFile(name: String) = File("$name.js")
fun minJsFile(name: String) = File("$name.min.js")

fun appendFrom(address: URL, output: OutputStreamWriter) {
    print("Downloading from $address...")
    address.openConnection().getInputStream().reader().copyTo(output)
    output.write(nl)
    println(" Done.")
}

fun appendFrom(file: File, output: OutputStreamWriter) {
    print("Appending from $file...")
    file.reader().use { it.copyTo(output) }
    output.write(nl)
    println(" Done.")
}

fun minify(name: String) {
    val src = jsFile(name)

    println("minification...")
    val minified = minJsFile(name)
    if (minified.exists() && minified.isFile) {
        check(minified.delete())
    }
    check(Runtime.getRuntime().exec(arrayOf("uglifyjs", "-o", minified.path, src.path)).waitFor() == 0)

    print(minified.name)
    print(" has been created. Press Enter to remove temporary ")
    print(src.name)
    println("...")

    System.`in`.read()

    src.delete()
}
