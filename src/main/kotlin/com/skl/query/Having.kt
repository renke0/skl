package com.skl.query

import com.skl.expr.Expr
import com.skl.sql.RenderContext

class HavingClause(val expression: Expr) : QueryClause {
  override fun appendTo(sb: StringBuilder, ctx: RenderContext) {
    sb.append(" HAVING ")
    expression.toSql(sb, ctx)
  }
}

class HavingStep internal constructor(override val context: QueryContext) : OrderBySupport

interface HavingSupport : QuerySupport {
  fun having(block: () -> Expr): HavingStep = context.having(HavingClause(block()))
}
