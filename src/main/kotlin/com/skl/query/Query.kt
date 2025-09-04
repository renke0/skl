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
    with(context.clauses) {
      val aliases = collectAliases()

      val ctx = RenderContext(aliases, context)
      val sb = QueryStringBuilder(ctx)

      val orderedClauses =
          listOf(select, from)
              .plus(joins)
              .plus(listOf(where, groupBy, having, orderBy, limit, offset))
              .filterNotNull()

      return sb.printList(orderedClauses, separator = " ").build()
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
      context.clauses.let {
        listOf(it.from?.expression)
            .plus(it.joins.map { join -> join.expression })
            .filterIsInstance<AliasedTable<*>>()
            .map { from -> from.table to from.alias }
      }
}

internal data class Clauses(
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
  internal var clauses = Clauses()

  fun select(clause: SelectClause): SelectStep {
    check(clauses.select == null) { "SELECT clause is already defined" }
    clauses = clauses.copy(select = clause)
    return SelectStep(this)
  }

  fun from(clause: FromClause): FromStep {
    check(clauses.from == null) { "FROM clause is already defined" }
    clauses = clauses.copy(from = clause)
    return FromStep(this)
  }

  fun join(clause: JoinClause): JoinStep {
    clauses = clauses.copy(joins = clauses.joins + clause)
    return JoinStep(this)
  }

  fun join(clauses: List<JoinClause>): JoinStep {
    this.clauses = this.clauses.copy(joins = this.clauses.joins + clauses)
    return JoinStep(this)
  }

  fun where(clause: WhereClause): WhereStep {
    check(clauses.where == null) { "WHERE clause is already defined" }
    clauses = clauses.copy(where = clause)
    return WhereStep(this)
  }

  fun groupBy(clause: GroupByClause): GroupByStep {
    check(clauses.groupBy == null) { "GROUP BY clause is already defined" }
    clauses = clauses.copy(groupBy = clause)
    return GroupByStep(this)
  }

  fun having(clause: HavingClause): HavingStep {
    check(clauses.having == null) { "HAVING clause is already defined" }
    clauses = clauses.copy(having = clause)
    return HavingStep(this)
  }

  fun orderBy(clause: OrderByClause): OrderByStep {
    check(clauses.orderBy == null) { "ORDER BY clause is already defined" }
    clauses = clauses.copy(orderBy = clause)
    return OrderByStep(this)
  }

  fun limit(clause: LimitClause): LimitStep {
    check(clauses.limit == null) { "LIMIT clause is already defined" }
    clauses = clauses.copy(limit = clause)
    return LimitStep(this)
  }

  fun offset(clause: OffsetClause): OffsetStep {
    check(clauses.offset == null) { "OFFSET clause is already defined" }
    clauses = clauses.copy(offset = clause)
    return OffsetStep(this)
  }
}

interface QueryClause : Printable

interface ClauseExpression {
  fun printOn(clause: Clause): Printable
}

enum class Clause {
  SELECT,
  FROM,
  JOIN,
  WHERE,
  GROUP_BY,
  HAVING,
  ORDER_BY,
  LIMIT,
  OFFSET,
}

interface QueryStep {
  val context: QueryContext

  fun query(): Query = Query(context)

  fun str(): String = query().print()

  fun pretty(): String = query().prettyPrint()
}
