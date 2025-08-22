package com.skl.query

import com.skl.model.Field

open class Table(val name: String) {
  protected fun <T> field(column: String): Field<T> = Field(this, column)
}

// Aliased table implementation
data class AliasedTable(val table: Table, val alias: String)

infix fun Table.`as`(alias: String): AliasedTable = AliasedTable(this, alias)
