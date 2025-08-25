package com.skl.query

import com.github.vertical_blank.sqlformatter.SqlFormatter
import com.github.vertical_blank.sqlformatter.core.FormatConfig
import com.github.vertical_blank.sqlformatter.languages.Dialect
import com.skl.model.Table
import com.skl.sql.RenderContext

class Query(private val context: QueryContext) {

  fun print(): String {
    with(context.parts) {
      val sb = StringBuilder()
      val aliases = mutableMapOf<Table, String>()
      from?.alias?.let { aliases[from.table] = it }
      joins.forEach { join -> join.alias?.let { aliases[join.table] = it } }

      val ctx = RenderContext(aliases)
      select.appendTo(sb, ctx)
      from?.appendTo(sb, ctx)
      joins.forEach { it.appendTo(sb, ctx) }
      where?.appendTo(sb, ctx)
      groupBy?.appendTo(sb, ctx)
      having?.appendTo(sb, ctx)
      orderBy?.appendTo(sb, ctx)
      limit?.appendTo(sb, ctx)
      offset?.appendTo(sb, ctx)
      return sb.toString()
    }
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

internal data class Parts(
    val select: SelectClause,
    val from: FromClause? = null,
    val joins: List<JoinClause> = emptyList(),
    val where: WhereClause? = null,
    val groupBy: GroupByClause? = null,
    val having: HavingClause? = null,
    val orderBy: OrderByClause? = null,
    val limit: LimitClause? = null,
    val offset: OffsetClause? = null
)

class QueryContext private constructor(select: SelectClause) {
  internal var parts = Parts(select = select)

  companion object {
    fun select(select: SelectClause): SelectStep = SelectStep(QueryContext(select))
  }

  fun from(clause: FromClause): FromStep {
    check(parts.from == null) { "FROM clause is already defined" }
    parts = parts.copy(from = clause)
    return FromStep(this)
  }

  fun join(clause: JoinClause): JoinStep {
    parts = parts.copy(joins = parts.joins + clause)
    return JoinStep(this)
  }

  fun join(clauses: List<JoinClause>): JoinStep {
    parts = parts.copy(joins = parts.joins + clauses)
    return JoinStep(this)
  }

  fun where(clause: WhereClause): WhereStep {
    check(parts.where == null) { "WHERE clause is already defined" }
    parts = parts.copy(where = clause)
    return WhereStep(this)
  }

  fun groupBy(clause: GroupByClause): GroupByStep {
    check(parts.groupBy == null) { "GROUP BY clause is already defined" }
    parts = parts.copy(groupBy = clause)
    return GroupByStep(this)
  }

  fun having(clause: HavingClause): HavingStep {
    check(parts.having == null) { "HAVING clause is already defined" }
    parts = parts.copy(having = clause)
    return HavingStep(this)
  }

  fun orderBy(clause: OrderByClause): OrderByStep {
    check(parts.orderBy == null) { "ORDER BY clause is already defined" }
    parts = parts.copy(orderBy = clause)
    return OrderByStep(this)
  }

  fun limit(clause: LimitClause): LimitStep {
    check(parts.limit == null) { "LIMIT clause is already defined" }
    parts = parts.copy(limit = clause)
    return LimitStep(this)
  }

  fun offset(clause: OffsetClause): OffsetStep {
    check(parts.offset == null) { "OFFSET clause is already defined" }
    parts = parts.copy(offset = clause)
    return OffsetStep(this)
  }
}

interface QueryClause {
  fun appendTo(sb: StringBuilder, ctx: RenderContext)
}

interface QuerySupport {
  val context: QueryContext

  fun toQuery(): Query = Query(context)
}
