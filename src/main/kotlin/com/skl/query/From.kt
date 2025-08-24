package com.skl.query

import com.skl.model.Table
import com.skl.sql.RenderContext

class FromClause(val table: Table, val alias: String? = null) : QueryClause {
  override fun appendTo(sb: StringBuilder, ctx: RenderContext) {
    sb.append(" FROM ").append(table.name)
    if (!alias.isNullOrBlank()) sb.append(" ").append(alias)
  }
}

class FromStep
internal constructor(
    override val context: QueryContext,
) : JoinSupport, WhereSupport, GroupBySupport, OrderBySupport
