package online.javanese.model

import com.github.andrewoma.kwery.core.Session
import com.github.andrewoma.kwery.mapper.Table
import online.javanese.krud.kwery.Uuid

fun insertSql(table: Table<*, *>) = """INSERT INTO "${table.name}"
        |(${table.allColumns.joinToString { "\"${it.name}\"" }})
        |VALUES (${table.allColumns.joinToString { ":${it.name}" }})""".trimMargin()

fun <T : Any> insert(table: Table<T, Uuid>, session: Session, insertSql: String, value: T): T {
    val columns = table.dataColumns

    val id = Uuid.randomUUID()
    val idCol = table.idColumns.single()
    val parameters = table.objectMap(session, value, columns) + (idCol.name to id)
    val new = table.copy(value, mapOf(idCol to id))

    val count = session.update(insertSql, parameters)
    check(count == 1) { "failed to insert any rows" }

    return new
}
