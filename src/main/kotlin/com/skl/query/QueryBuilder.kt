package com.skl.query

import com.github.vertical_blank.sqlformatter.SqlFormatter
import com.github.vertical_blank.sqlformatter.core.FormatConfig
import com.github.vertical_blank.sqlformatter.languages.Dialect
import com.skl.expr.Expr
import com.skl.model.Field
import com.skl.sql.FromClause
import com.skl.sql.JoinClause
import com.skl.sql.JoinType
import com.skl.sql.RenderContext
import com.skl.sql.SelectClause
import com.skl.sql.WhereClause

class FromStep internal constructor(val table: Table, private val alias: String? = null) {
  private val from
    get() = FromClause(table, alias)

  fun `as`(alias: String): FromStep = FromStep(table, alias)

  fun select(vararg fields: Field<*>): SelectStep = SelectStep(from, SelectClause(fields.toList()))

  fun join(block: JoinBuilder.() -> JoinBuilder): JoinStep = joinStep(JoinType.INNER, block)

  fun leftJoin(block: JoinBuilder.() -> JoinBuilder): JoinStep = joinStep(JoinType.LEFT, block)

  fun rightJoin(block: JoinBuilder.() -> JoinBuilder): JoinStep = joinStep(JoinType.RIGHT, block)

  fun fullJoin(block: JoinBuilder.() -> JoinBuilder): JoinStep = joinStep(JoinType.FULL, block)

  fun crossJoin(block: JoinBuilder.() -> JoinBuilder): JoinStep = joinStep(JoinType.CROSS, block)

  private fun joinStep(type: JoinType, block: JoinBuilder.() -> JoinBuilder): JoinStep {
    val joinBuilder = JoinBuilder(type)
    val join = joinBuilder.block().build()
    return JoinStep(from, listOf(join))
  }
}

class JoinStep
internal constructor(private val from: FromClause, private val joins: List<JoinClause>) {
  fun select(vararg fields: Field<*>): SelectStep =
      SelectStep(from, SelectClause(fields.toList()), joins)

  fun join(block: JoinBuilder.() -> JoinBuilder): JoinStep = joinStep(JoinType.INNER, block)

  fun leftJoin(block: JoinBuilder.() -> JoinBuilder): JoinStep = joinStep(JoinType.LEFT, block)

  fun rightJoin(block: JoinBuilder.() -> JoinBuilder): JoinStep = joinStep(JoinType.RIGHT, block)

  fun fullJoin(block: JoinBuilder.() -> JoinBuilder): JoinStep = joinStep(JoinType.FULL, block)

  fun crossJoin(block: JoinBuilder.() -> JoinBuilder): JoinStep = joinStep(JoinType.CROSS, block)

  private fun joinStep(type: JoinType, block: JoinBuilder.() -> JoinBuilder): JoinStep {
    val joinBuilder = JoinBuilder(type)
    val join = joinBuilder.block().build()
    return JoinStep(from, joins + join)
  }
}

class SelectStep
internal constructor(
    private val from: FromClause,
    private val select: SelectClause,
    private val joins: List<JoinClause> = emptyList()
) {
  fun where(block: () -> Expr): Query = Query(select, from, joins, WhereClause(block()))

  fun toQuery(): Query = Query(select, from, joins)

  fun join(block: JoinBuilder.() -> JoinBuilder): SelectStep = joinStep(JoinType.INNER, block)

  fun leftJoin(block: JoinBuilder.() -> JoinBuilder): SelectStep = joinStep(JoinType.LEFT, block)

  fun rightJoin(block: JoinBuilder.() -> JoinBuilder): SelectStep = joinStep(JoinType.RIGHT, block)

  fun fullJoin(block: JoinBuilder.() -> JoinBuilder): SelectStep = joinStep(JoinType.FULL, block)

  fun crossJoin(block: JoinBuilder.() -> JoinBuilder): SelectStep = joinStep(JoinType.CROSS, block)

  private fun joinStep(type: JoinType, block: JoinBuilder.() -> JoinBuilder): SelectStep {
    val joinBuilder = JoinBuilder(type)
    val join = joinBuilder.block().build()
    return SelectStep(from, select, joins + join)
  }
}

data class Query(
    private val select: SelectClause,
    private val from: FromClause,
    private val joins: List<JoinClause> = emptyList(),
    private val where: WhereClause? = null
) {
  /** Returns SQL with named placeholders. */
  fun print(): String {
    val sb = StringBuilder()
    // Create a map of table aliases that includes both the main table and joined tables
    val aliases = mutableMapOf<Table, String>()
    from.alias?.let { aliases[from.table] = it }
    joins.forEach { join -> join.alias?.let { aliases[join.table] = it } }

    val ctx = RenderContext(aliases)
    select.appendTo(sb, ctx)
    from.appendTo(sb, ctx)
    joins.forEach { it.appendTo(sb, ctx) }
    where?.appendTo(sb, ctx)
    return sb.toString()
  }

  fun prettyPrint(): String {
    return SqlFormatter.of(Dialect.TSql)
        .format(
            print(),
            FormatConfig.builder()
                .maxColumnLength(120)
                .indent("  ")
                .uppercase(true)
                .linesBetweenQueries(2)
                .build(),
        )
  }
}

// Entry point: from { table } or from { table `as` "alias" }
fun from(block: () -> Any): FromStep {
  return when (val result = block()) {
    is AliasedTable -> FromStep(result.table, result.alias)
    is Table -> FromStep(result)
    else ->
        error("from { ... } must return Table or AliasedTable, got: ${result::class.simpleName}")
  }
}
