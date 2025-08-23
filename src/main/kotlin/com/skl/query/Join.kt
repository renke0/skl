package com.skl.query

import com.skl.expr.Expr
import com.skl.sql.RenderContext

class Join {
  class Clause(
      val type: JoinType,
      val table: Table,
      val alias: String? = null,
      val condition: Expr? = null,
  ) : BaseClause {
    override fun appendTo(sb: StringBuilder, ctx: RenderContext) {
      sb.append(" ").append(type.sql).append(" ").append(table.name)
      if (!alias.isNullOrBlank()) sb.append(" ").append(alias)
      if (condition != null && type != JoinType.CROSS) {
        sb.append(" ON ")
        condition.toSql(sb, ctx)
      }
    }
  }

  class Builder internal constructor(private val type: JoinType) {
    private lateinit var table: Table
    private var alias: String? = null
    private var condition: Expr? = null

    infix fun Table.on(conditionBlock: () -> Expr): Builder {
      check(type != JoinType.CROSS) { "CROSS JOIN does not support ON condition" }
      this@Builder.table = this
      this@Builder.condition = conditionBlock()
      return this@Builder
    }

    infix fun AliasedTable.on(conditionBlock: () -> Expr): Builder {
      check(type != JoinType.CROSS) { "CROSS JOIN does not support ON condition" }
      this@Builder.table = this.table
      this@Builder.alias = this.alias
      this@Builder.condition = conditionBlock()
      return this@Builder
    }

    operator fun Table.invoke(): Builder {
      this@Builder.table = this
      return this@Builder
    }

    operator fun AliasedTable.invoke(): Builder {
      this@Builder.table = this.table
      this@Builder.alias = this.alias
      return this@Builder
    }

    internal fun build(): Clause = Clause(type, table, alias, condition)
  }

  class Step
  internal constructor(
      private val from: From.Clause,
      private val select: Select.Clause,
      private val joins: List<Clause>,
  ) : Joinable {
    fun where(block: () -> Expr): Query = Query(select, from, joins, Where.Clause(block()))

    fun toQuery(): Query = Query(select, from, joins)

    override fun joinStep(type: JoinType, block: Builder.() -> Builder): Step {
      val joinBuilder = Builder(type)
      val join = joinBuilder.block().build()
      return Step(from, select, joins + join)
    }
  }
}

interface Joinable {
  /**
   * The `join` method provides SQL-like syntax for convenience. It functions identically to
   * `innerJoin`, performing an INNER JOIN operation.
   */
  fun join(block: Join.Builder.() -> Join.Builder): Join.Step = joinStep(JoinType.JOIN, block)

  fun innerJoin(block: Join.Builder.() -> Join.Builder): Join.Step = joinStep(JoinType.INNER, block)

  fun leftJoin(block: Join.Builder.() -> Join.Builder): Join.Step = joinStep(JoinType.LEFT, block)

  fun rightJoin(block: Join.Builder.() -> Join.Builder): Join.Step = joinStep(JoinType.RIGHT, block)

  fun fullJoin(block: Join.Builder.() -> Join.Builder): Join.Step = joinStep(JoinType.FULL, block)

  fun crossJoin(block: Join.Builder.() -> Join.Builder): Join.Step = joinStep(JoinType.CROSS, block)

  fun joinStep(type: JoinType, block: Join.Builder.() -> Join.Builder): Join.Step
}

enum class JoinType(val sql: String) {
  JOIN("JOIN"),
  INNER("INNER JOIN"),
  LEFT("LEFT JOIN"),
  RIGHT("RIGHT JOIN"),
  FULL("FULL JOIN"),
  CROSS("CROSS JOIN"),
}
