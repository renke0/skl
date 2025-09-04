package com.skl.query

import com.skl.printer.Printable
import com.skl.printer.QueryStringBuilder

class OrderByClause(
    val orders: List<OrderByArgument>,
    val direction: OrderDirection? = null,
    val nullsOrder: NullsOrder? = null,
) : QueryClause {
  init {
    require(orders.isNotEmpty()) { "At least one column must be specified" }
  }

  val keyword = Keyword.ORDER_BY

  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.print(keyword)
          .space()
          .printList(orders)
          .printIfNotNull(direction, { space().print(it.keyword) })
          .printIfNotNull(nullsOrder, { space().print(it.keyword) })

  fun asc(): QueryClause = OrderByClause(orders, OrderDirection.ASC, nullsOrder)

  fun desc(): QueryClause = OrderByClause(orders, OrderDirection.DESC, nullsOrder)

  fun nullsFirst(): QueryClause = OrderByClause(orders, direction, NullsOrder.FIRST)

  fun nullsLast(): QueryClause = OrderByClause(orders, direction, NullsOrder.LAST)
}

class OrderByStep internal constructor(override val context: QueryContext) :
    QueryStep, LimitSupport

interface OrderBySupport : QueryStep {
  fun orderBy(vararg orders: OrderByExpression): OrderByStep =
      context.orderBy(
          OrderByClause(
              orders.map {
                when (it) {
                  is Column -> OrderByColumn(it)
                  is AliasedTerm<*> -> OrderByAliasedTerm(it)
                  is Function -> OrderByFunction(it)
                }
              },
          ),
      )
}

sealed interface OrderByExpression

sealed interface OrderByArgument : Printable

data class OrderByColumn(val column: Column) : OrderByArgument {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.print(column.term())
}

data class OrderByAliasedTerm(val term: AliasedTerm<*>) : OrderByArgument {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.append(term.alias)
}

data class OrderByFunction(val function: Function) : OrderByArgument {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.print(function)
}

enum class OrderDirection(val keyword: Keyword) {
  ASC(Keyword.ASC),
  DESC(Keyword.DESC),
}

enum class NullsOrder(val keyword: Keyword) {
  FIRST(Keyword.NULLS_FIRST),
  LAST(Keyword.NULLS_LAST),
}
