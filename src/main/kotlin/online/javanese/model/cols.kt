package online.javanese.model

import com.github.andrewoma.kwery.mapper.Table
import online.javanese.krud.kwery.DefaultUuid
import online.javanese.krud.kwery.Uuid
import online.javanese.krud.kwery.UuidConverter
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

internal fun <T : Any> Table<T, *>.linkTextCol(linkTextProp: KProperty1<T, String>) =
        col(linkTextProp, name = "linkText")

internal fun <T : Any, C> Table<T, *>.linkTextCol(linkTextProp: KProperty1<C, String>, path: (T) -> C) =
        col(linkTextProp, path, name = "linkText")

internal fun <T : Any> Table<T, *>.metaTitleCol(metaProp: (T) -> Meta) =
        col(Meta::title, metaProp, name = "metaTitle")

internal fun <T : Any> Table<T, *>.metaDescriptionCol(metaProp: (T) -> Meta) =
        col(Meta::description, metaProp, name = "metaDescription")

internal fun <T : Any> Table<T, *>.metaKeywordsCol(metaProp: (T) -> Meta) =
        col(Meta::keywords, metaProp, name = "metaKeywords")

internal fun <T : Any> Table<T, *>.urlSegmentCol(urlPathComponentProp: KProperty1<T, String>) =
        col(urlPathComponentProp, name = "urlSegment")

internal fun <T : Any, C> Table<T, *>.urlSegmentCol(urlPathComponentProp: KProperty1<C, String>, path: (T) -> C) =
        col(urlPathComponentProp, path, name = "urlSegment")

internal fun <T : Any> Table<T, *>.headingCol(headingProp: KProperty1<T, String>) =
        col(headingProp, name = "heading")

internal fun <T : Any> Table<T, *>.lastModifiedCol(lastModifiedProp: KProperty1<T, LocalDateTime>) =
        col(lastModifiedProp, name = "lastModified", version = true)

internal fun <T : Any> Table<T, *>.sortIndexCol(sortIndexProp: KProperty1<T, Int>) =
        col(sortIndexProp, name = "sortIndex")

internal inline fun <T : Any> Table<T, *>.vkPostIdCol(crossinline vkPostInfoProp: (T) -> VkPostInfo?) =
        col(VkPostInfo::id, { vkPostInfoProp(it) ?: VkPostInfo.Empty }, name = "vkPostId")

internal inline fun <T : Any> Table<T, *>.vkPostHashCol(crossinline vkPostInfoProp: (T) -> VkPostInfo?) =
        col(VkPostInfo::hash, { vkPostInfoProp(it) ?: VkPostInfo.Empty }, name = "vkPostHash")

internal fun <T : Any> Table<T, *>.tgPostCol(prop: KProperty1<T, String>) =
        col(prop, name = "tgPost")
