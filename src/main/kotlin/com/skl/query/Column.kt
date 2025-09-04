package com.skl.query

import com.skl.printer.QueryStringBuilder

data class Column(val owner: TableLike, val name: String) :
    SelectExpression, GroupByExpression, OrderByExpression, TermExpression {

  override fun term(): Term = ColumnTerm(this)
}

data class ColumnTerm(val column: Column) : Term {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.append(qb.ctx.aliasFor(column.owner) ?: column.owner.name()).dot().append(column.name)
}
