package com.skl.query

import com.skl.printer.Printable
import com.skl.printer.QueryStringBuilder

class Parameter(val name: String? = null) :
    Term, TermExpression, LimitExpression, OffsetExpression, SelectExpression {
  init {
    require(name == null || name.matches(Regex("[a-zA-Z_][a-zA-Z0-9_]*"))) {
      "Invalid parameter name: $name"
    }
  }

  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.parameter(name)

  override fun term(): Term = this

  override fun printOn(clause: Clause): Printable =
      when (clause) {
        Clause.SELECT,
        Clause.LIMIT,
        Clause.OFFSET -> this
        else -> error("Parameter cannot be used in $clause")
      }
}
