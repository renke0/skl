package com.skl.query

import com.skl.printer.QueryStringBuilder

class LimitClause(val expression: LimitExpression) : QueryClause {
  val keyword = Keyword.LIMIT

  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.print(keyword).space().print(expression.printOn(Clause.LIMIT))
}

class LimitStep internal constructor(override val context: QueryContext) : OffsetSupport

interface LimitSupport : QueryStep {
  fun limit(limit: Int): LimitStep = limit(NumberLiteral(limit))

  fun limit(limit: LimitExpression): LimitStep = context.limit(LimitClause(limit))
}

sealed interface LimitExpression : ClauseExpression
