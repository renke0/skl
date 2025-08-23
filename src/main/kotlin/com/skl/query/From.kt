package com.skl.query

import com.skl.expr.Expr
import com.skl.sql.RenderContext

class From {
  class Clause(val table: Table, val alias: String? = null) : BaseClause {
    override fun appendTo(sb: StringBuilder, ctx: RenderContext) {
      sb.append(" FROM ").append(table.name)
      if (!alias.isNullOrBlank()) sb.append(" ").append(alias)
    }
  }

  class Step
  internal constructor(
      private val table: Table,
      private val alias: String?,
      private val select: Select.Clause,
  ) : Joinable {
    private val from
      get() = Clause(table, alias)

    fun where(block: () -> Expr): Query = Query(select, from, emptyList(), Where.Clause(block()))

    fun toQuery(): Query = Query(select, from)

    override fun joinStep(type: JoinType, block: Join.Builder.() -> Join.Builder): Join.Step {
      val joinBuilder = Join.Builder(type)
      val join = joinBuilder.block().build()
      return Join.Step(from, select, listOf(join))
    }
  }
}
