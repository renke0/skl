package com.skl.query

import com.skl.printer.QueryStringBuilder

@Suppress("MemberVisibilityCanBePrivate")
class SelectClause(val expressions: List<SelectExpression>) : QueryClause {
  val keyword = Keyword.SELECT

  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.print(keyword).space().let {
        if (expressions.isEmpty()) it.append("*")
        else it.printList(expressions.map { expr -> expr.printOn(Clause.SELECT) })
      }
}

class SelectStep internal constructor(override val context: QueryContext) :
    FromSupport, WhereSupport

interface SelectSupport : QueryStep {
  fun select(block: () -> List<SelectExpression>): SelectStep =
      context.select(SelectClause(block()))
}

sealed interface SelectExpression : ClauseExpression
