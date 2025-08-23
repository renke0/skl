package com.skl.sql

import com.skl.expr.Expr
import com.skl.model.Field
import com.skl.query.Table

interface SqlPart {
  fun appendTo(sb: StringBuilder, ctx: RenderContext)
}

interface SelectableElement

object STAR : SelectableElement {
  override fun toString(): String = "*"
}

sealed class SelectItem {
  abstract fun toSql(ctx: RenderContext): String

  data class FieldItem(val field: Field<*>) : SelectItem() {
    override fun toSql(ctx: RenderContext): String = field.fq(ctx)
  }

  data class TableAllFields(val table: Table) : SelectItem() {
    override fun toSql(ctx: RenderContext): String = "${ctx.nameFor(table)}.*"
  }

  data object AllFields : SelectItem() {
    override fun toSql(ctx: RenderContext): String = "*"
  }
}

class SelectClause(val items: List<SelectItem>) : SqlPart {
  override fun appendTo(sb: StringBuilder, ctx: RenderContext) {
    sb.append("SELECT ")
    if (items.isEmpty()) {
      sb.append("*")
    } else {
      sb.append(items.joinToString(", ") { it.toSql(ctx) })
    }
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

class WhereClause(val expression: Expr) : SqlPart {
  override fun appendTo(sb: StringBuilder, ctx: RenderContext) {
    sb.append(" WHERE ")
    expression.toSql(sb, ctx)
  }
}

enum class JoinType(val sql: String) {
  INNER("INNER JOIN"),
  LEFT("LEFT JOIN"),
  RIGHT("RIGHT JOIN"),
  FULL("FULL JOIN"),
  CROSS("CROSS JOIN")
}
