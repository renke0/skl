package com.skl.query

interface TableLike {
  operator fun get(column: Column): Column

  fun name(): String
}

open class Table<T : Table<T>>(
    val tableName: String,
    val tableSchema: String? = null,
    val tableDatabase: String? = null
) : TableLike, SelectExpression, FromExpression, JoinExpression {
  private val columns = mutableListOf<Column>()

  fun column(name: String): Column {
    val column = Column(this, name)
    columns.add(column)
    return column
  }

  override fun get(column: Column): Column {
    check(column in columns) { "Column ${column.name} does not belong to table" }
    return column
  }

  override fun name(): String =
      listOfNotNull(tableDatabase, tableSchema, tableName).joinToString(".")

  @Suppress("UNCHECKED_CAST")
  infix fun `as`(alias: String): AliasedTable<T> = AliasedTable(this as T, alias)
}

data class AliasedTable<T : Table<T>>(val table: T, val alias: String) :
    TableLike, SelectExpression, FromExpression, JoinExpression {
  override fun get(column: Column): Column = table[column].copy(owner = this)

  override fun name(): String = alias
}
