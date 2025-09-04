package com.skl.query

import com.skl.printer.Printable
import com.skl.printer.QueryStringBuilder

class FromClause(val source: FromArgument) : QueryClause {
  val keyword = Keyword.FROM

  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.print(keyword).space().print(source)
}

class FromStep internal constructor(override val context: QueryContext) :
    JoinSupport, WhereSupport, GroupBySupport, OrderBySupport, LimitSupport

interface FromSupport : QueryStep {
  fun from(block: () -> FromExpression): FromStep =
      context.from(
          FromClause(
              when (val result = block()) {
                is Table<*> -> FromTable(result)
                is AliasedTable<*> -> FromAliasedTable(result)
              // TODO support derived tables
              // TODO support CTEs
              // TODO support Table Functions
              // TODO support VALUES clause
              },
          ),
      )
}

sealed interface FromExpression

sealed interface FromArgument : Printable

data class FromTable(val table: Table<*>) : FromArgument {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.append(table.name())
}

data class FromAliasedTable(val aliasedTable: AliasedTable<*>) : FromArgument {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.append(aliasedTable.table.name()).space().append(aliasedTable.alias)
}
