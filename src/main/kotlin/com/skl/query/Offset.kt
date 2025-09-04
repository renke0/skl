package com.skl.query

import com.skl.printer.Printable
import com.skl.printer.QueryStringBuilder

class OffsetClause(val offset: OffsetArgument) : QueryClause {
  val keyword = Keyword.OFFSET

  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.print(keyword).space().print(offset)
}

class OffsetStep internal constructor(override val context: QueryContext) : QueryStep

interface OffsetSupport : QueryStep {
  fun offset(offset: Int): OffsetStep = offset(NumberLiteral(offset))

  fun offset(offset: OffsetExpression): OffsetStep =
      context.offset(
          OffsetClause(
              when (offset) {
                is NumberLiteral -> OffsetLiteral(offset)
                is Parameter -> OffsetParam(offset)
              },
          ),
      )
}

sealed interface OffsetExpression

sealed interface OffsetArgument : Printable

data class OffsetLiteral(val value: NumberLiteral) : OffsetArgument {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.print(value)
}

data class OffsetParam(val param: Parameter) : OffsetArgument {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.print(param)
}
