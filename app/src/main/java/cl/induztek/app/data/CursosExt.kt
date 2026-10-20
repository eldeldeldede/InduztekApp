package cl.induztek.app.data

import android.database.Cursor

// =====================================================================
//  CursorExt.kt — atajos para leer columnas de un Cursor POR NOMBRE
//  ---------------------------------------------------------------------
//  Un Cursor es la "tabla de resultados" de un SELECT: se recorre fila por fila.
//  Sin estos atajos, cada columna se leería así (como en MiRegistro):
//
//      cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
//
//  Con el atajo:   cursor.texto("nombre")
//
//  Las versiones "...ONulo" son para columnas que pueden ser NULL en la base
//  (por ejemplo telefono, o la fecha de cierre de una visita abierta).
//  Si usaras getString() en una columna NULL obtendrías null "a escondidas";
//  aquí el tipo de retorno (String?) te obliga a manejarlo.
// =====================================================================
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

// SQLite guarda los Boolean como 0 / 1.
fun Cursor.booleano(columna: String): Boolean = getInt(getColumnIndexOrThrow(columna)) == 1
