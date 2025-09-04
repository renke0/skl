package com.skl.query

import com.skl.printer.QueryStringBuilder

@Suppress("MemberVisibilityCanBePrivate")
class JoinClause(
    val type: JoinType,
    val source: FromArgument,
    val predicate: Predicate? = null,
) : QueryClause {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.print(type.keyword)
          .space()
          .print(source)
          .printIfNotNull(predicate, { space().print(Keyword.ON).space().print(it) })
}

class JoinBuilder internal constructor(private val type: JoinType) {
  private lateinit var source: FromArgument
  private var predicate: Predicate? = null

  infix fun JoinExpression.on(predicateBlock: () -> Predicate): JoinBuilder {
    check(type != JoinType.CROSS) { "CROSS JOIN does not support ON condition" }
    source = asSource()
    predicate = predicateBlock()
    return this@JoinBuilder
  }

  operator fun JoinExpression.invoke(): JoinBuilder {
    source = asSource()
    return this@JoinBuilder
  }

  private fun JoinExpression.asSource() =
      when (this) {
        is Table<*> -> FromTable(this)
        is AliasedTable<*> -> FromAliasedTable(this)
      }

  internal fun build(): JoinClause = JoinClause(type, source, predicate)
}

class JoinStep internal constructor(override val context: QueryContext) :
    JoinSupport, WhereSupport, GroupBySupport, OrderBySupport

interface JoinSupport : QueryStep {
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

sealed interface JoinExpression

enum class JoinType(val keyword: Keyword) {
  JOIN(Keyword.JOIN),
  INNER(Keyword.INNER_JOIN),
  LEFT(Keyword.LEFT_JOIN),
  RIGHT(Keyword.RIGHT_JOIN),
  FULL(Keyword.FULL_JOIN),
  CROSS(Keyword.CROSS_JOIN),
}
