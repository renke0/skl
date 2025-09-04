package com.skl.query

import com.skl.printer.Printable
import com.skl.printer.QueryStringBuilder

class GroupByClause(val groups: List<GroupByArgument>) : QueryClause {
  init {
    require(groups.isNotEmpty()) { "GROUP BY must have at least one grouping" }
  }

  val keyword = Keyword.GROUP_BY

  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.print(keyword).space().printList(groups)
}

class GroupByStep internal constructor(override val context: QueryContext) :
    OrderBySupport, HavingSupport, LimitSupport

interface GroupBySupport : QueryStep {
  fun groupBy(vararg groups: GroupByExpression): GroupByStep =
      context.groupBy(
          GroupByClause(
              groups.map {
                when (it) {
                  is Column -> GroupByColumn(it)
                  is AliasedTerm<*> -> GroupByTerm(it)
                  is ScalarFunction -> GroupByFunction(it)
                }
              },
          ),
      )
}

sealed interface GroupByExpression

sealed interface GroupByArgument : Printable

data class GroupByColumn(val column: Column) : GroupByArgument {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.print(column.term())
}

data class GroupByTerm(val aliased: AliasedTerm<*>) : GroupByArgument {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.append(aliased.alias)
}

data class GroupByFunction(val function: ScalarFunction) : GroupByArgument {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.print(function)
}
