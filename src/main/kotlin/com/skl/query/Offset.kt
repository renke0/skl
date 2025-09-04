package com.skl.query

import com.skl.printer.QueryStringBuilder

class OffsetClause(val expression: OffsetExpression) : QueryClause {
  val keyword = Keyword.OFFSET

  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.print(keyword).space().print(expression.printOn(Clause.OFFSET))
}

class OffsetStep internal constructor(override val context: QueryContext) : QueryStep

interface OffsetSupport : QueryStep {
  fun offset(offset: Int): OffsetStep = offset(NumberLiteral(offset))

  fun offset(offset: OffsetExpression): OffsetStep = context.offset(OffsetClause(offset))
}

sealed interface OffsetExpression : ClauseExpression
