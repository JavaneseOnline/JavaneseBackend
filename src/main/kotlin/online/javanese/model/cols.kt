package online.javanese.model

import com.github.andrewoma.kwery.mapper.Table
import online.javanese.DefaultUuid
import online.javanese.Uuid
import online.javanese.UuidConverter
import java.time.LocalDateTime
import kotlin.reflect.KProperty1

internal fun <T : Any> Table<T, Uuid>.idCol(idProp: KProperty1<T, Uuid>) =
        col(idProp, name = "id", id = true, default = DefaultUuid, converter = UuidConverter)

internal fun <T : Any, C> Table<T, Uuid>.idCol(idProp: KProperty1<C, Uuid>, path: (T) -> C) =
        col(idProp, path, name = "id", id = true, default = DefaultUuid, converter = UuidConverter)

internal fun <T : Any> Table<T, Uuid>.uuidCol(idProp: KProperty1<T, Uuid>, name: String) =
        col(idProp, name = name, default = DefaultUuid, converter = UuidConverter)

internal fun <T : Any, C> Table<T, Uuid>.uuidCol(idProp: KProperty1<C, Uuid>, path: (T) -> C, name: String) =
        col(idProp, path, name = name, default = DefaultUuid, converter = UuidConverter)

internal fun <T : Any> Table<T, *>.metaTitleCol(metaProp: KProperty1<T, Meta>) =
        col(Meta::title, metaProp, name = "metaTitle")

internal fun <T : Any> Table<T, *>.metaDescriptionCol(metaProp: KProperty1<T, Meta>) =
        col(Meta::description, metaProp, name = "metaDescription")

internal fun <T : Any> Table<T, *>.metaKeywordsCol(metaProp: KProperty1<T, Meta>) =
        col(Meta::keywords, metaProp, name = "metaKeywords")

internal fun <T : Any> Table<T, *>.urlPathComponentCol(urlPathComponentProp: KProperty1<T, String>) =
        col(urlPathComponentProp, name = "urlPathComponent")

internal fun <T : Any, C> Table<T, *>.urlPathComponentCol(urlPathComponentProp: KProperty1<C, String>, path: (T) -> C) =
        col(urlPathComponentProp, path, name = "urlPathComponent")

internal fun <T : Any> Table<T, *>.lastModifiedCol(lastModifiedProp: KProperty1<T, LocalDateTime>) =
        col(lastModifiedProp, name = "lastModified", version = true)

internal fun <T : Any> Table<T, *>.sortIndexCol(sortIndexProp: KProperty1<T, Int>) =
        col(sortIndexProp, name = "sortIndex")
