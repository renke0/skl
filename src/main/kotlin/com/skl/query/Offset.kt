package com.skl.query

import com.skl.sql.RenderContext

class OffsetClause(val offset: Int) : QueryClause {
  override fun appendTo(sb: StringBuilder, ctx: RenderContext) {
    sb.append(" OFFSET ")
    sb.append(offset)
  }
}

class OffsetStep internal constructor(override val context: QueryContext) : QuerySupport

interface OffsetSupport : QuerySupport {
  fun offset(offset: Int): OffsetStep = context.offset(OffsetClause(offset))
}
