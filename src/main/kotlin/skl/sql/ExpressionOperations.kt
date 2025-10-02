package skl.sql

data class BinaryExpression(
  val left: Expression,
  val operator: BinaryOperator,
  val right: Expression
) : Expression

data class UnaryExpression(
  val operator: UnaryOperator,
  val operand: Expression,
  val isPostfix: Boolean = false
) : Expression

data class ComparisonExpression(
  val left: Expression,
  val operator: ComparisonOperator,
  val right: Expression
) : Expression

data class BetweenExpression(
  val value: Expression,
  val lower: Expression,
  val upper: Expression,
  val symmetric: Boolean = false
) : Expression

data class LikeExpression(
  val value: Expression,
  val pattern: Expression,
  val escape: Expression? = null,
  val negated: Boolean = false
) : Expression

data class IsNullExpression(val expression: Expression, val negated: Boolean = false) : Expression

data class InListExpression(
  val value: Expression,
  val list: List<Expression>,
  val negated: Boolean = false
) : Expression

data class InSubqueryExpression(
  val value: Expression,
  val subquery: QueryExpression,
  val negated: Boolean = false
) : Expression

data class ExistsExpression(val subquery: QueryExpression, val negated: Boolean = false) :
  Expression

data class CaseExpression(
  val operand: Expression? = null,
  val branches: List<WhenClause>,
  val elseExpression: Expression? = null
) : Expression

data class WhenClause(val condition: Expression, val result: Expression)

data class CastExpression(
  val expression: Expression,
  val targetType: SqlType,
  val format: Expression? = null
) : Expression

data class CollateExpression(val expression: Expression, val collation: String) : Expression

data class FunctionCall(
  val name: QualifiedName,
  val arguments: List<Expression> = emptyList(),
  val distinct: Boolean = false,
  val orderBy: List<OrderBy> = emptyList(),
  val filter: Expression? = null,
  val nullTreatment: NullTreatment = NullTreatment.RESPECT,
  val over: WindowSpecification? = null
) : Expression

enum class BinaryOperator {
  CONCAT,
  PLUS,
  MINUS,
  MULTIPLY,
  DIVIDE,
  MODULUS,
  BITWISE_AND,
  BITWISE_OR,
  BITWISE_XOR,
  SHIFT_LEFT,
  SHIFT_RIGHT,
  AND,
  OR
}

enum class UnaryOperator {
  PLUS,
  MINUS,
  NOT,
  BITWISE_NOT
}

enum class ComparisonOperator {
  EQUAL,
  NOT_EQUAL,
  LESS_THAN,
  LESS_THAN_OR_EQUAL,
  GREATER_THAN,
  GREATER_THAN_OR_EQUAL,
  IS_DISTINCT_FROM,
  IS_NOT_DISTINCT_FROM
}

enum class NullTreatment {
  RESPECT,
  IGNORE
}
