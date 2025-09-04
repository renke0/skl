package com.skl.query

import com.skl.printer.QueryStringBuilder

class FromClause(val expression: FromExpression) : QueryClause {
  val keyword = Keyword.FROM

  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.print(keyword).space().print(expression.printOn(Clause.FROM))
}

class FromStep internal constructor(override val context: QueryContext) :
    JoinSupport, WhereSupport, GroupBySupport, OrderBySupport, LimitSupport

interface FromSupport : QueryStep {
  fun from(block: () -> FromExpression): FromStep = context.from(FromClause(block()))
}

sealed interface FromExpression : ClauseExpression
