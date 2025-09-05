package com.skl.query

import com.skl.printer.QueryStringBuilder

@Suppress("MemberVisibilityCanBePrivate")
class GroupByClause(val expressions: List<GroupByExpression>) : QueryClause {
  init {
    require(expressions.isNotEmpty()) { "GROUP BY must have at least one grouping" }
  }

  val keyword = Keyword.GROUP_BY

  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.print(keyword).space().printList(expressions.map { it.printOn(Clause.GROUP_BY) })
}

class GroupByStep internal constructor(override val context: QueryContext) :
    OrderBySupport, HavingSupport, LimitSupport

interface GroupBySupport : QueryStep {
  fun groupBy(vararg groups: GroupByExpression): GroupByStep =
      context.groupBy(GroupByClause(groups.toList()))
}

sealed interface GroupByExpression : ClauseExpression
