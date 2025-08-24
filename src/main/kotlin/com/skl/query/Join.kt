package com.skl.query

import com.skl.expr.Expr
import com.skl.model.AliasedTable
import com.skl.model.Table
import com.skl.sql.RenderContext

class JoinClause(
    val type: JoinType,
    val table: Table,
    val alias: String? = null,
    val condition: Expr? = null,
) : QueryClause {
  override fun appendTo(sb: StringBuilder, ctx: RenderContext) {
    sb.append(" ").append(type.sql).append(" ").append(table.name)
    if (!alias.isNullOrBlank()) sb.append(" ").append(alias)
    if (condition != null && type != JoinType.CROSS) {
      sb.append(" ON ")
      condition.toSql(sb, ctx)
    }
  }
}

class JoinBuilder internal constructor(private val type: JoinType) {
  private lateinit var table: Table
  private var alias: String? = null
  private var condition: Expr? = null

  infix fun Table.on(conditionBlock: () -> Expr): JoinBuilder {
    check(type != JoinType.CROSS) { "CROSS JOIN does not support ON condition" }
    this@JoinBuilder.table = this
    this@JoinBuilder.condition = conditionBlock()
    return this@JoinBuilder
  }

  infix fun AliasedTable.on(conditionBlock: () -> Expr): JoinBuilder {
    check(type != JoinType.CROSS) { "CROSS JOIN does not support ON condition" }
    this@JoinBuilder.table = this.table
    this@JoinBuilder.alias = this.alias
    this@JoinBuilder.condition = conditionBlock()
    return this@JoinBuilder
  }

  operator fun Table.invoke(): JoinBuilder {
    this@JoinBuilder.table = this
    return this@JoinBuilder
  }

  operator fun AliasedTable.invoke(): JoinBuilder {
    this@JoinBuilder.table = this.table
    this@JoinBuilder.alias = this.alias
    return this@JoinBuilder
  }

  internal fun build(): JoinClause = JoinClause(type, table, alias, condition)
}

class JoinStep internal constructor(override val context: QueryContext) :
    JoinSupport, WhereSupport, GroupBySupport, OrderBySupport

interface JoinSupport : QuerySupport {
  /**
   * The `join` method provides SQL-like syntax for convenience. It functions identically to
   * `innerJoin`, performing an INNER JOIN operation.
   */
  fun join(block: JoinBuilder.() -> JoinBuilder): JoinStep = joinType(JoinType.JOIN, block)

  fun innerJoin(block: JoinBuilder.() -> JoinBuilder): JoinStep = joinType(JoinType.INNER, block)

  fun leftJoin(block: JoinBuilder.() -> JoinBuilder): JoinStep = joinType(JoinType.LEFT, block)

  fun rightJoin(block: JoinBuilder.() -> JoinBuilder): JoinStep = joinType(JoinType.RIGHT, block)

  fun fullJoin(block: JoinBuilder.() -> JoinBuilder): JoinStep = joinType(JoinType.FULL, block)

  fun crossJoin(block: JoinBuilder.() -> JoinBuilder): JoinStep = joinType(JoinType.CROSS, block)

  private fun joinType(type: JoinType, block: JoinBuilder.() -> JoinBuilder): JoinStep =
      context.join(JoinBuilder(type).block().build())
}

enum class JoinType(val sql: String) {
  JOIN("JOIN"),
  INNER("INNER JOIN"),
  LEFT("LEFT JOIN"),
  RIGHT("RIGHT JOIN"),
  FULL("FULL JOIN"),
  CROSS("CROSS JOIN"),
}
