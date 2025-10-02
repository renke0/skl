package skl.sql

data class PivotClause(
  val forColumns: List<Expression>,
  val inValues: List<PivotValue>,
  val aggregateItems: List<PivotAggregation>
)

data class PivotValue(val value: Expression, val alias: String? = null)

data class PivotAggregation(val function: FunctionCall, val alias: String? = null)
