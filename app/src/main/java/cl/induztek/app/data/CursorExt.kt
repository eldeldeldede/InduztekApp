package cl.induztek.app.data

import android.database.Cursor

fun Cursor.texto(columna: String): String = getString(getColumnIndexOrThrow(columna))

fun Cursor.textoONulo(columna: String): String? =
    getColumnIndexOrThrow(columna).let { if (isNull(it)) null else getString(it) }

fun Cursor.largo(columna: String): Long = getLong(getColumnIndexOrThrow(columna))

fun Cursor.largoONulo(columna: String): Long? =
    getColumnIndexOrThrow(columna).let { if (isNull(it)) null else getLong(it) }

fun Cursor.entero(columna: String): Int = getInt(getColumnIndexOrThrow(columna))

fun Cursor.enteroONulo(columna: String): Int? =
    getColumnIndexOrThrow(columna).let { if (isNull(it)) null else getInt(it) }

fun Cursor.decimal(columna: String): Double = getDouble(getColumnIndexOrThrow(columna))

fun Cursor.decimalONulo(columna: String): Double? =
    getColumnIndexOrThrow(columna).let { if (isNull(it)) null else getDouble(it) }

fun Cursor.booleano(columna: String): Boolean = getInt(getColumnIndexOrThrow(columna)) == 1
