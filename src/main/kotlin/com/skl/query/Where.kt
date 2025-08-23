package com.skl.query

import com.skl.expr.Expr
import com.skl.sql.RenderContext

class Where {
  class Clause(val expression: Expr) : BaseClause {
    override fun appendTo(sb: StringBuilder, ctx: RenderContext) {
      sb.append(" WHERE ")
      expression.toSql(sb, ctx)
    }
  }
}
