package com.skl.query

import com.skl.printer.Printable
import com.skl.printer.QueryStringBuilder

class LimitClause(val limit: LimitArgument) : QueryClause {
  val keyword = Keyword.LIMIT

  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.print(keyword).space().print(limit)
}

class LimitStep internal constructor(override val context: QueryContext) : OffsetSupport

interface LimitSupport : QueryStep {
  fun limit(limit: Int): LimitStep = limit(NumberLiteral(limit))

  fun limit(limit: LimitExpression): LimitStep =
      context.limit(
          LimitClause(
              when (limit) {
                is NumberLiteral -> LimitLiteral(limit)
                is Parameter -> LimitParam(limit)
              },
          ),
      )
}

sealed interface LimitExpression

sealed interface LimitArgument : Printable

data class LimitLiteral(val literal: NumberLiteral) : LimitArgument {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.print(literal)
}

data class LimitParam(val param: Parameter) : LimitArgument {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.print(param)
}
