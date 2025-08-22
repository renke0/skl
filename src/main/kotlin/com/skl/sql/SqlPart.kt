package com.skl.sql

import com.skl.expr.Expr
import com.skl.model.Field
import com.skl.query.Table

interface SqlPart {
  fun appendTo(sb: StringBuilder, ctx: RenderContext)
}

class SelectClause(private val fields: List<Field<*>>) : SqlPart {
  override fun appendTo(sb: StringBuilder, ctx: RenderContext) {
    sb.append("SELECT ")
    if (fields.isEmpty()) sb.append("*") else sb.append(fields.joinToString(", ") { it.fq(ctx) })
  }
}

class FromClause(val table: Table, val alias: String? = null) : SqlPart {
  override fun appendTo(sb: StringBuilder, ctx: RenderContext) {
    sb.append(" FROM ").append(table.name)
    if (!alias.isNullOrBlank()) sb.append(" ").append(alias)
  }
}

class JoinClause(
    val type: JoinType,
    val table: Table,
    val alias: String? = null,
    val condition: Expr? = null
) : SqlPart {
  override fun appendTo(sb: StringBuilder, ctx: RenderContext) {
    sb.append(" ").append(type.sql).append(" ").append(table.name)
    if (!alias.isNullOrBlank()) sb.append(" ").append(alias)
    if (condition != null && type != JoinType.CROSS) {
      sb.append(" ON ")
      condition.toSql(sb, ctx)
    }
  }
}

class WhereClause(private val expr: Expr) : SqlPart {
  override fun appendTo(sb: StringBuilder, ctx: RenderContext) {
    sb.append(" WHERE ")
    expr.toSql(sb, ctx)
  }
}
