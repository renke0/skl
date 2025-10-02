package skl.sql

sealed interface SelectItem

data class ExpressionSelectItem(
  val expression: Expression,
  val alias: String? = null,
  val isHidden: Boolean = false
) : SelectItem

data class StarSelectItem(
  val qualifier: QualifiedName? = null,
  val except: List<String> = emptyList(),
  val replace: List<StarReplacement> = emptyList()
) : SelectItem

data class StarReplacement(val column: String, val expression: Expression)
