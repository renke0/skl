package com.skl.query

import com.skl.model.Field
import com.skl.sql.RenderContext

class GroupByClause(val fields: List<Field<*>>) : QueryClause {
  override fun appendTo(sb: StringBuilder, ctx: RenderContext) {
    check(fields.isNotEmpty())
    sb.append(" GROUP BY ")
    sb.append(fields.joinToString(", ") { it.fq(ctx) })
  }
}

class GroupByStep internal constructor(override val context: QueryContext) :
    OrderBySupport, HavingSupport

interface GroupBySupport : QuerySupport {
  fun groupBy(vararg fields: Field<*>): GroupByStep =
      context.groupBy(GroupByClause(fields.toList()))
}
