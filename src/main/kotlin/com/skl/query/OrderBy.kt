package com.skl.query

import com.skl.printer.Printable
import com.skl.printer.QueryStringBuilder

@Suppress("MemberVisibilityCanBePrivate")
class OrderByClause(val orders: List<OrderItem>) : QueryClause {
  init {
    require(orders.isNotEmpty()) { "At least one column must be specified" }
  }

  val keyword = Keyword.ORDER_BY

  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.print(keyword).space().printList(orders)
}

class OrderByStep internal constructor(override val context: QueryContext) :
    QueryStep, LimitSupport, OffsetSupport

interface OrderBySupport : QueryStep {
  fun orderBy(vararg orders: OrderByExpression): OrderByStep =
      context.orderBy(OrderByClause(orders.map(::toOrderItem)))

  private fun toOrderItem(expr: OrderByExpression): OrderItem =
      if (expr is OrderItem) expr else OrderItem(expr)
}

data class OrderItem(
    val expression: OrderByExpression,
    val direction: OrderDirection? = null,
    val nullsOrder: NullsOrder? = null
) : OrderByExpression, Printable {
  override fun printOn(clause: Clause): Printable = expression.printOn(clause)

  override fun asc(): OrderItem = copy(direction = OrderDirection.ASC)

  override fun desc(): OrderItem = copy(direction = OrderDirection.DESC)

  override fun nullsFirst(): OrderItem = copy(nullsOrder = NullsOrder.FIRST)

  override fun nullsLast(): OrderItem = copy(nullsOrder = NullsOrder.LAST)

  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.print(expression.printOn(Clause.ORDER_BY))
          .printIfNotNull(direction) { space().print(it.keyword) }
          .printIfNotNull(nullsOrder) { space().print(it.keyword) }
}

sealed interface OrderByExpression : ClauseExpression {
  fun asc(): OrderItem = OrderItem(this, direction = OrderDirection.ASC)

  fun desc(): OrderItem = OrderItem(this, direction = OrderDirection.DESC)

  fun nullsFirst(): OrderItem = OrderItem(this, nullsOrder = NullsOrder.FIRST)

  fun nullsLast(): OrderItem = OrderItem(this, nullsOrder = NullsOrder.LAST)
}

enum class OrderDirection(val keyword: Keyword) {
  ASC(Keyword.ASC),
  DESC(Keyword.DESC),
}

enum class NullsOrder(val keyword: Keyword) {
  FIRST(Keyword.NULLS_FIRST),
  LAST(Keyword.NULLS_LAST),
}
