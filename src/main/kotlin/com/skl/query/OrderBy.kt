package com.skl.query

import com.skl.model.Field
import com.skl.sql.RenderContext

class OrderByClause(val fields: List<Field<*>>) : QueryClause {
  override fun appendTo(sb: StringBuilder, ctx: RenderContext) {
    check(fields.isNotEmpty())
    sb.append(" ORDER BY ")
    sb.append(fields.joinToString(", ") { it.fq(ctx) })
  }
}

class OrderByStep internal constructor(override val context: QueryContext) :
    QuerySupport, LimitSupport

interface OrderBySupport : QuerySupport {
  fun orderBy(vararg fields: Field<*>): OrderByStep =
      context.orderBy(OrderByClause(fields.toList()))
}
