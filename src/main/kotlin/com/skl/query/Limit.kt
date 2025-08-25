package com.skl.query

import com.skl.sql.RenderContext

class LimitClause(val limit: Int) : QueryClause {
  override fun appendTo(sb: StringBuilder, ctx: RenderContext) {
    sb.append(" LIMIT ")
    sb.append(limit)
  }
}

class LimitStep internal constructor(override val context: QueryContext) : OffsetSupport

interface LimitSupport : QuerySupport {
  fun limit(limit: Int): LimitStep = context.limit(LimitClause(limit))
}
