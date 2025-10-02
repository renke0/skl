package skl.sql

data class MatchRecognizeClause(
  val partitionBy: List<Expression> = emptyList(),
  val orderBy: List<OrderBy> = emptyList(),
  val measures: List<MeasureDefinition> = emptyList(),
  val rowsPerMatch: RowsPerMatch = RowsPerMatch.ONE_ROW_PER_MATCH,
  val afterMatchSkip: AfterMatchSkip? = null,
  val pattern: PatternExpression,
  val subsets: List<PatternSubset> = emptyList(),
  val defines: List<PatternDefinition> = emptyList()
)

data class MeasureDefinition(val expression: Expression, val alias: String)

enum class RowsPerMatch {
  ONE_ROW_PER_MATCH,
  ALL_ROWS_PER_MATCH
}

data class AfterMatchSkip(val option: AfterMatchSkipOption, val variable: String? = null)

enum class AfterMatchSkipOption {
  PAST_LAST_ROW,
  TO_NEXT_ROW,
  TO_FIRST,
  TO_LAST
}

sealed interface PatternExpression

data class PatternVariable(val name: String) : PatternExpression

data class PatternConcatenation(val parts: List<PatternExpression>) : PatternExpression

data class PatternAlternation(val options: List<PatternExpression>) : PatternExpression

data class PatternQuantifierExpression(
  val pattern: PatternExpression,
  val quantifier: PatternQuantifier
) : PatternExpression

data class PatternQuantifier(
  val min: Int? = null,
  val max: Int? = null,
  val greedy: Boolean = true
)

data class PatternSubset(val name: String, val members: List<String>)

data class PatternDefinition(val variable: String, val condition: Expression)
