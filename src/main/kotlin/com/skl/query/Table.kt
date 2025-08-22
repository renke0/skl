package com.skl.query

import com.skl.model.Field

open class Table(val name: String) : FromSource {
  protected fun <T> field(column: String): Field<T> = Field(this, column)
}

// Aliased table implementation
data class AliasedTable(val table: Table, val alias: String) : FromSource

@Suppress("FunctionName")
infix fun Table.`as`(alias: String): AliasedTable = AliasedTable(this, alias)
