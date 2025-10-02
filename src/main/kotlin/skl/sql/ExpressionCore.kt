package skl.sql

sealed interface Expression

data object ExpressionStar : Expression

data class ColumnReference(val name: QualifiedName) : Expression

sealed interface Literal : Expression

data class NumericLiteral(val value: String) : Literal

data class StringLiteral(val value: String) : Literal

data class BooleanLiteral(val value: Boolean) : Literal

data class NullLiteral(val representation: String = "NULL") : Literal

data class DateLiteral(val value: String) : Literal

data class TimeLiteral(val value: String) : Literal

data class TimestampLiteral(val value: String) : Literal

data class IntervalLiteral(val value: String, val qualifier: IntervalQualifier? = null) : Literal

data class ParameterPlaceholder(val position: Int? = null, val name: String? = null) : Expression

data class DefaultExpression(val column: String? = null) : Expression

data class ScalarSubquery(val subquery: QueryExpression) : Expression

data class RowConstructor(val elements: List<Expression>) : Expression

data class ArrayConstructor(val elements: List<Expression>) : Expression

data class StructConstructor(val fields: List<StructField>) : Expression

data class StructField(val name: String, val expression: Expression)

data class JsonExpression(
  val target: Expression,
  val path: String,
  val returningType: SqlType? = null,
  val wrapper: JsonWrapper = JsonWrapper.NONE
) : Expression

data class JsonWrapper(val kind: JsonWrapperKind, val key: String? = null) {
  companion object {
    val NONE = JsonWrapper(JsonWrapperKind.NONE)
  }
}

enum class JsonWrapperKind {
  NONE,
  WITH_PATH,
  WITHOUT_PATH
}

data class SqlType(
  val name: QualifiedName,
  val parameters: List<Expression> = emptyList(),
  val nullable: Boolean = true
)

data class QualifiedName(val parts: List<String>) {
  constructor(vararg parts: String) : this(parts.toList())

  override fun toString(): String = parts.joinToString(".")
}

data class IntervalQualifier(
  val startField: DateTimeField,
  val endField: DateTimeField? = null,
  val precision: Int? = null,
  val fractionPrecision: Int? = null
)

enum class DateTimeField {
  YEAR,
  MONTH,
  DAY,
  HOUR,
  MINUTE,
  SECOND,
  MICROSECOND
}
