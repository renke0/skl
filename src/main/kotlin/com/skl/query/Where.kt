package com.skl.query

import com.skl.printer.QueryStringBuilder

class WhereClause(val predicate: Predicate) : QueryClause {
  val keyword = Keyword.WHERE

  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.print(keyword).space().print(predicate)
}

class WhereStep internal constructor(override val context: QueryContext) :
    GroupBySupport, OrderBySupport, LimitSupport

interface WhereSupport : QueryStep {
  fun where(block: () -> Predicate): WhereStep = context.where(WhereClause(block()))
}
