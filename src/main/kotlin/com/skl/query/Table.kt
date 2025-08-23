package com.skl.query

import com.skl.model.Field

open class Table(val name: String) : Selectable {
  protected fun <T> field(column: String): Field<T> = Field(this, column)

  infix fun `as`(alias: String): AliasedTable = AliasedTable(this, alias)
}

data class AliasedTable(val table: Table, val alias: String)
