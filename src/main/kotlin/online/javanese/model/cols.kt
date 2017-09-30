package online.javanese.model

import com.github.andrewoma.kwery.mapper.Table
import online.javanese.DefaultUuid
import online.javanese.Uuid
import online.javanese.UuidConverter
import java.time.LocalDateTime
import kotlin.reflect.KProperty1

fun <T : Any> Table<T, Uuid>.uuidCol(idProp: KProperty1<T, Uuid>) =
        col(idProp, name = "id", id = true, default = DefaultUuid, converter = UuidConverter)

fun <T : Any> Table<T, *>.metaTitleCol(metaProp: KProperty1<T, Meta>) =
        col(Meta::title, metaProp, name = "metaTitle")

fun <T : Any> Table<T, *>.metaDescriptionCol(metaProp: KProperty1<T, Meta>) =
        col(Meta::description, metaProp, name = "metaDescription")

fun <T : Any> Table<T, *>.metaKeywordsCol(metaProp: KProperty1<T, Meta>) =
        col(Meta::keywords, metaProp, name = "metaKeywords")

fun <T : Any> Table<T, *>.urlPathComponentCol(urlPathComponentProp: KProperty1<T, String>) =
        col(urlPathComponentProp, name = "urlPathComponent")

fun <T : Any> Table<T, *>.lastModifiedCol(lastModifiedProp: KProperty1<T, LocalDateTime>) =
        col(lastModifiedProp, name = "lastModified", version = true)
