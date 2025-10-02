package skl.sql

/** Entry point for modeling a complete SQL query, including common clauses. */
data class SqlQuery(
  val withClause: WithClause? = null,
  val body: QueryExpression,
  val orderBy: List<OrderBy> = emptyList(),
  val offset: OffsetClause? = null,
  val fetch: FetchClause? = null,
  val limit: LimitClause? = null,
  val locking: List<LockingClause> = emptyList()
)

sealed interface QueryExpression

/** Core SELECT statement with optional analytics and filtering. */
data class SelectQuery(
  val setQuantifier: SetQuantifier = SetQuantifier.ALL,
  val distinctOn: List<Expression> = emptyList(),
  val selectList: List<SelectItem>,
  val from: FromClause? = null,
  val where: Expression? = null,
  val groupBy: GroupByClause? = null,
  val having: Expression? = null,
  val window: WindowClause? = null,
  val qualify: QualifyClause? = null
) : QueryExpression

/** Set operation such as UNION/INTERSECT/EXCEPT optionally chained with further operations. */
data class SetQuery(
  val left: QueryExpression,
  val operator: SetOperator,
  val right: QueryExpression,
  val quantifier: SetQuantifier = SetQuantifier.ALL,
  val corresponding: CorrespondingClause? = null,
  val orderBy: List<OrderBy> = emptyList(),
  val offset: OffsetClause? = null,
  val fetch: FetchClause? = null,
  val limit: LimitClause? = null
) : QueryExpression

/** VALUES clause used as a query expression. */
data class ValuesQuery(
  val rows: List<List<Expression>>,
  val columnAliases: List<String> = emptyList()
) : QueryExpression

/** Direct reference to a table or table-valued construct. */
data class TableQuery(val table: TableReference) : QueryExpression

enum class SetQuantifier {
  ALL,
  DISTINCT
}

enum class SetOperator {
  UNION,
  INTERSECT,
  EXCEPT,
  MINUS
}

data class CorrespondingClause(
  val columns: List<String> = emptyList(),
  val matchByName: Boolean = true
)

data class WithClause(
  val recursive: Boolean = false,
  val commonTableExpressions: List<CommonTableExpression>
)

data class CommonTableExpression(
  val name: String,
  val columnNames: List<String> = emptyList(),
  val query: QueryExpression,
  val materialization: MaterializedHint = MaterializedHint.BY_ENGINE,
  val searchClause: SearchClause? = null,
  val cycleClause: CycleClause? = null
)

enum class MaterializedHint {
  MATERIALIZED,
  NOT_MATERIALIZED,
  BY_ENGINE
}

data class SearchClause(val kind: SearchKind, val columnName: String)

enum class SearchKind {
  DEPTH_FIRST,
  BREADTH_FIRST
}

data class CycleClause(
  val columnName: String,
  val cycleMarkValue: Expression,
  val nonCycleMarkValue: Expression,
  val cyclePathColumn: String? = null
)
