package skl.sql

data class FromClause(val relations: List<TableReference>)

sealed interface TableReference {
  val alias: RelationAlias?
}

data class RelationAlias(val name: String, val columnAliases: List<String> = emptyList())

data class NamedTable(
  val name: QualifiedName,
  override val alias: RelationAlias? = null,
  val tableHint: TableHint? = null,
  val sample: TableSampleClause? = null
) : TableReference

data class DerivedTable(
  val query: QueryExpression,
  override val alias: RelationAlias,
  val lateral: Boolean = false
) : TableReference

data class ValuesTable(
  val valuesQuery: ValuesQuery,
  override val alias: RelationAlias? = null,
  val lateral: Boolean = false
) : TableReference

data class FunctionTable(
  val functionCall: FunctionCall,
  override val alias: RelationAlias? = null,
  val lateral: Boolean = false,
  val withOrdinality: Boolean = false
) : TableReference

data class UnnestTable(
  val expressions: List<Expression>,
  val withOrdinality: Boolean = false,
  override val alias: RelationAlias? = null,
  val lateral: Boolean = false
) : TableReference

data class PivotTable(
  val source: TableReference,
  val pivot: PivotClause,
  override val alias: RelationAlias
) : TableReference

data class MatchRecognizeTable(
  val input: TableReference,
  val matchRecognize: MatchRecognizeClause,
  override val alias: RelationAlias
) : TableReference

data class JoinTable(
  val left: TableReference,
  val right: TableReference,
  val type: JoinType,
  val condition: JoinCondition?
) : TableReference {
  override val alias: RelationAlias? = null
}

enum class JoinType {
  INNER,
  LEFT,
  RIGHT,
  FULL,
  CROSS,
  NATURAL
}

sealed interface JoinCondition

data class OnCondition(val expression: Expression) : JoinCondition

data class UsingCondition(val columns: List<String>) : JoinCondition

data class TableSampleClause(
  val method: SampleMethod = SampleMethod.BERNOULLI,
  val size: Expression,
  val repeatableSeed: Expression? = null
)

enum class SampleMethod {
  BERNOULLI,
  SYSTEM
}

data class TableHint(val hints: List<String>)
