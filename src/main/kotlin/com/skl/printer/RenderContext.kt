package com.skl.printer

import com.skl.query.QueryContext
import com.skl.query.TableLike

class RenderContext(
    private val tableAliases: List<Pair<TableLike, String>>,
    queryContext: QueryContext
) {
  private val style: QueryStyle = safeStyle(queryContext)
  private var parameterIndex = 0

  fun keywordString(keyword: String): String = style.keywords(keyword)

  fun aliasFor(table: TableLike): String? {
    val existing = tableAliases.filter { it.first == table }
    check(existing.size <= 1) {
      "Table $table has multiple aliases and cannot be automatically resolved"
    }
    return existing.firstOrNull()?.second
  }

  fun parameterString(name: String? = null): String = style.parameters(name, ++parameterIndex)
}

private fun safeStyle(context: QueryContext): QueryStyle {
  val keywords =
      context.vendor.allowedKeywordCasing.let {
        check(it.isNotEmpty()) { "allowedKeywordCasing must not be empty" }
        if (it.contains(context.style.keywords)) context.style.keywords else it.first()
      }
  return context.style.copy(keywords = keywords)
}

data class QueryStyle(
    val parameters: ParameterStyle,
    val keywords: KeywordStyle,
) {
  companion object {
    val default =
        QueryStyle(
            parameters = ParameterStyle.AT_NAMED,
            keywords = KeywordStyle.UPPER,
        )
  }
}

enum class ParameterStyle {
  SEQUENTIAL,
  NUMBERED,
  COLON_NAMED,
  DOLLAR_NAMED,
  AT_NAMED;

  operator fun invoke(value: String?, index: Int): String =
      when (this) {
        SEQUENTIAL -> "?"
        NUMBERED -> "?$index"
        COLON_NAMED -> ":${required(value)}"
        DOLLAR_NAMED -> "\$${required(value)}"
        AT_NAMED -> "@${required(value)}"
      }

  private fun required(value: String?) =
      requireNotNull(value) { "Named parameter must have a name" }
}

enum class KeywordStyle {
  UPPER,
  LOWER;

  operator fun invoke(value: String): String =
      when (this) {
        UPPER -> value.uppercase()
        LOWER -> value.lowercase()
      }
}
