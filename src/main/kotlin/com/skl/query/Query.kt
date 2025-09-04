package com.skl.query

import com.github.vertical_blank.sqlformatter.SqlFormatter
import com.github.vertical_blank.sqlformatter.core.FormatConfig
import com.github.vertical_blank.sqlformatter.languages.Dialect
import com.skl.printer.Printable
import com.skl.printer.QueryStringBuilder
import com.skl.printer.QueryStyle
import com.skl.printer.RenderContext
import com.skl.vendor.Vendor

class Query(private val context: QueryContext) {
  fun print(): String {
    with(context.parts) {
      val aliases = collectAliases()

      val ctx = RenderContext(aliases, context)
      val sb = QueryStringBuilder(ctx)

      val clauses =
          listOf(select, from)
              .plus(joins)
              .plus(listOf(where, groupBy, having, orderBy, limit, offset))
              .filterNotNull()

      return sb.printList(clauses, separator = " ").build()
    }
  }

  fun prettyPrint(): String =
      SqlFormatter.of(Dialect.TSql)
          .extend { cfg -> cfg.plusNamedPlaceholderTypes(":", "$") }
          .format(
              print(),
              FormatConfig.builder()
                  .maxColumnLength(120)
                  .indent("  ")
                  .uppercase(true)
                  .linesBetweenQueries(2)
                  .build(),
          )

  private fun collectAliases(): List<Pair<TableLike, String>> =
      context.parts.let {
        listOf(it.from?.source)
            .plus(it.joins.map { join -> join.source })
            .filterIsInstance<FromAliasedTable>()
            .map { from -> from.aliasedTable.table to from.aliasedTable.alias }
      }
}

internal data class Parts(
    val select: SelectClause? = null,
    val from: FromClause? = null,
    val joins: List<JoinClause> = emptyList(),
    val where: WhereClause? = null,
    val groupBy: GroupByClause? = null,
    val having: HavingClause? = null,
    val orderBy: OrderByClause? = null,
    val limit: LimitClause? = null,
    val offset: OffsetClause? = null
)

class QueryContext(val vendor: Vendor, val style: QueryStyle) {
  internal var parts = Parts()

  fun select(clause: SelectClause): SelectStep {
    check(parts.select == null) { "SELECT clause is already defined" }
    parts = parts.copy(select = clause)
    return SelectStep(this)
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

interface QueryClause : Printable

interface QueryStep {
  val context: QueryContext

  fun query(): Query = Query(context)

  fun str(): String = query().print()

  fun pretty(): String = query().prettyPrint()
}
