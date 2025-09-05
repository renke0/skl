package com.skl.query

import com.skl.printer.QueryStringBuilder

@Suppress("MemberVisibilityCanBePrivate")
class HavingClause(val predicate: Predicate) : QueryClause {
  val keyword = Keyword.HAVING

  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.print(keyword).space().print(predicate)
}

class HavingStep internal constructor(override val context: QueryContext) :
    OrderBySupport, LimitSupport

interface HavingSupport : QueryStep {
  fun having(block: () -> Predicate): HavingStep = context.having(HavingClause(block()))
}
