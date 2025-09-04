package com.skl.query

import com.skl.printer.Printable
import com.skl.printer.QueryStringBuilder

data class Column(val owner: TableLike, val name: String) :
    SelectExpression, GroupByExpression, OrderByExpression, TermExpression {

  override fun term(): Term = ColumnTerm(this)

  override fun printOn(clause: Clause): Printable =
      when (clause) {
        Clause.SELECT,
        Clause.ORDER_BY,
        Clause.GROUP_BY -> term()
        else -> error("Column cannot be used in $clause")
      }
}

data class ColumnTerm(val column: Column) : Term {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.append(qb.ctx.aliasFor(column.owner) ?: column.owner.name()).dot().append(column.name)
}
