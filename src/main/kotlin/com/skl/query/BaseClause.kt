package com.skl.query

import com.skl.sql.RenderContext

interface BaseClause {
  fun appendTo(sb: StringBuilder, ctx: RenderContext)
}
