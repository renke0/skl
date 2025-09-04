package com.skl.query

import com.skl.printer.Printable

interface TableLike : SelectExpression, FromExpression, JoinExpression {
  operator fun get(column: Column): Column

  fun name(): String
}

open class Table<T : Table<T>>(
    val tableName: String,
    val tableSchema: String? = null,
    val tableDatabase: String? = null
) : TableLike {
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

  override fun printOn(clause: Clause): Printable =
      when (clause) {
        Clause.SELECT -> Printable.of { qb -> qb.append(qb.ctx.aliasFor(this) ?: name()).dotStar() }
        Clause.FROM,
        Clause.JOIN -> Printable.of { qb -> qb.append(name()) }
        else -> error("Table cannot be used in $clause")
      }

  @Suppress("UNCHECKED_CAST")
  infix fun `as`(alias: String): AliasedTable<T> = AliasedTable(this as T, alias)
}

data class AliasedTable<T : Table<T>>(val table: T, val alias: String) :
    TableLike, SelectExpression, FromExpression, JoinExpression {
  override fun get(column: Column): Column = table[column].copy(owner = this)

  override fun name(): String = alias

  override fun printOn(clause: Clause): Printable =
      when (clause) {
        Clause.SELECT -> Printable.of { qb -> qb.append(alias).dotStar() }
        Clause.FROM,
        Clause.JOIN -> Printable.of { qb -> qb.append(table.name()).space().append(alias) }
        else -> error("Aliased table cannot be used in $clause")
      }
}
