package com.skl.util

fun sqlLiteral(value: Any): String =
    when (value) {
      is String -> "'" + value.replace("'", "''") + "'"
      is Number -> value.toString()
      is Boolean -> if (value) "TRUE" else "FALSE"
      else -> "'" + value.toString().replace("'", "''") + "'"
    }

fun sqlNonNullLiteral(value: Any?): String =
    when (value) {
      null -> error("NULL not allowed here; use eq(null) or isNull()")
      else -> sqlLiteral(value)
    }
