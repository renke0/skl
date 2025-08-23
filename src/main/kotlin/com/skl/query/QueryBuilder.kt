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
import com.skl.sql.STAR
import com.skl.sql.SelectClause
import com.skl.sql.SelectItem
import com.skl.sql.SelectableElement
import com.skl.sql.WhereClause

class SelectStep internal constructor(items: List<SelectItem>) {
  private val select = SelectClause(items)

  fun from(block: () -> Any): FromStep =
      when (val result = block()) {
        is AliasedTable -> FromStep(result.table, result.alias, select)
        is Table -> FromStep(result, null, select)
        else ->
            error(
                "from { ... } must return Table or AliasedTable, got: ${result::class.simpleName}",
            )
      }
}

interface Joinable {
  fun join(block: JoinBuilder.() -> JoinBuilder): JoinStep = joinStep(JoinType.INNER, block)

  fun leftJoin(block: JoinBuilder.() -> JoinBuilder): JoinStep = joinStep(JoinType.LEFT, block)

  fun rightJoin(block: JoinBuilder.() -> JoinBuilder): JoinStep = joinStep(JoinType.RIGHT, block)

  fun fullJoin(block: JoinBuilder.() -> JoinBuilder): JoinStep = joinStep(JoinType.FULL, block)

  fun crossJoin(block: JoinBuilder.() -> JoinBuilder): JoinStep = joinStep(JoinType.CROSS, block)

  fun joinStep(type: JoinType, block: JoinBuilder.() -> JoinBuilder): JoinStep
}

class FromStep
internal constructor(
    private val table: Table,
    private val alias: String?,
    private val select: SelectClause
) : Joinable {
  private val from
    get() = FromClause(table, alias)

  fun where(block: () -> Expr): Query = Query(select, from, emptyList(), WhereClause(block()))

  fun toQuery(): Query = Query(select, from)

  override fun joinStep(type: JoinType, block: JoinBuilder.() -> JoinBuilder): JoinStep {
    val joinBuilder = JoinBuilder(type)
    val join = joinBuilder.block().build()
    return JoinStep(from, select, listOf(join))
  }
}

class JoinStep
internal constructor(
    private val from: FromClause,
    private val select: SelectClause,
    private val joins: List<JoinClause>
) : Joinable {
  fun where(block: () -> Expr): Query = Query(select, from, joins, WhereClause(block()))

  fun toQuery(): Query = Query(select, from, joins)

  override fun joinStep(type: JoinType, block: JoinBuilder.() -> JoinBuilder): JoinStep {
    val joinBuilder = JoinBuilder(type)
    val join = joinBuilder.block().build()
    return JoinStep(from, select, joins + join)
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

  fun prettyPrint(): String =
      SqlFormatter.of(Dialect.TSql)
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

// Entry point: select(col1, col2, table, STAR)
fun select(vararg items: SelectableElement): SelectStep {
  val selectItems =
      when {
        items.isEmpty() -> listOf(SelectItem.AllFields)
        else ->
            items.map { item ->
              when (item) {
                is Field<*> -> SelectItem.FieldItem(item)
                is Table -> SelectItem.TableAllFields(item)
                is STAR -> SelectItem.AllFields
                else ->
                    error(
                        "Select items must be Field, Table, or STAR, got: ${item::class.simpleName}")
              }
            }
      }
  return SelectStep(selectItems)
}
