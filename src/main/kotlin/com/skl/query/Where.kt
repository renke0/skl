package com.skl.query

import com.skl.expr.Expr
import com.skl.sql.RenderContext

class WhereClause(val expression: Expr) : QueryClause {
  override fun appendTo(sb: StringBuilder, ctx: RenderContext) {
    sb.append(" WHERE ")
    expression.toSql(sb, ctx)
  }
}

class WhereStep internal constructor(override val context: QueryContext) :
    GroupBySupport, OrderBySupport

interface WhereSupport : QuerySupport {
  fun where(block: () -> Expr): WhereStep = context.where(WhereClause(block()))
}
