package skl.sql

data class OffsetClause(val count: Expression, val unit: OffsetUnit = OffsetUnit.ROWS)

/** Standard LIMIT/OFFSET and SQL:2008 FETCH FIRST syntax co-exist across engines. */
data class FetchClause(
  val option: FetchOption,
  val count: Expression,
  val withTies: Boolean = false
)

data class LimitClause(val count: Expression)

enum class FetchOption {
  FIRST_ROWS,
  NEXT_ROWS,
  PERCENT
}

enum class OffsetUnit {
  ROWS
}
