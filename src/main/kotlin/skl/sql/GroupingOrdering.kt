package skl.sql

data class GroupByClause(val elements: List<GroupByElement>)

sealed interface GroupByElement

data class GroupByExpression(val expression: Expression) : GroupByElement

data class Rollup(val expressions: List<Expression>) : GroupByElement

data class Cube(val expressions: List<Expression>) : GroupByElement

data class GroupingSets(val sets: List<List<Expression>>) : GroupByElement

data class OrderBy(
  val expression: Expression,
  val direction: SortDirection = SortDirection.ASC,
  val nullOrdering: NullOrdering = NullOrdering.UNSPECIFIED
)

enum class SortDirection {
  ASC,
  DESC
}

enum class NullOrdering {
  NULLS_FIRST,
  NULLS_LAST,
  UNSPECIFIED
}
