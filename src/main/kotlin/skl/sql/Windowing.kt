package skl.sql

/** Window clause attaching named windows to the query. */
data class WindowClause(val definitions: List<WindowDefinition>)

data class WindowDefinition(val name: String, val specification: WindowSpecification)

/** Specification for analytic/window functions. */
data class WindowSpecification(
  val baseWindowName: String? = null,
  val partitionBy: List<Expression> = emptyList(),
  val orderBy: List<OrderBy> = emptyList(),
  val frameClause: WindowFrameClause? = null
)

data class WindowFrameClause(
  val type: WindowFrameType,
  val start: FrameBound,
  val end: FrameBound? = null,
  val exclusion: FrameExclusion = FrameExclusion.NO_OTHERS
)

enum class WindowFrameType {
  ROWS,
  RANGE,
  GROUPS
}

data class FrameBound(val boundType: FrameBoundType, val offset: Expression? = null)

enum class FrameBoundType {
  UNBOUNDED_PRECEDING,
  OFFSET_PRECEDING,
  CURRENT_ROW,
  OFFSET_FOLLOWING,
  UNBOUNDED_FOLLOWING
}

enum class FrameExclusion {
  NO_OTHERS,
  CURRENT_ROW,
  GROUP,
  TIES
}

/** Enables post-window filtering used by analytic warehouses via QUALIFY. */
data class QualifyClause(val condition: Expression)
