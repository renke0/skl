package com.skl.query

import com.skl.model.Field
import com.skl.sql.SelectableElement

open class Table(val name: String) : SelectableElement {
  protected fun <T> field(column: String): Field<T> = Field(this, column)
}

// Aliased table implementation
data class AliasedTable(val table: Table, val alias: String)

infix fun Table.`as`(alias: String): AliasedTable = AliasedTable(this, alias)
