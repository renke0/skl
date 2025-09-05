package com.skl.printer

import com.skl.query.Aliased
import com.skl.query.AliasedTable
import com.skl.query.AliasedTerm
import com.skl.query.QueryContext
import com.skl.query.TableLike
import com.skl.query.TermExpression

class RenderContext(
    private val tableAliases: List<Aliased<TableLike>>,
    private val termAliases: List<Aliased<TermExpression>>,
    queryContext: QueryContext
) {
  private val style: QueryStyle = safeStyle(queryContext)
  private var parameterIndex = 0

  fun keywordString(keyword: String): String = style.keywords(keyword)

  fun parameterString(name: String? = null): String = style.parameters(name, ++parameterIndex)

  fun aliasFor(table: TableLike): AliasedTable<*>? =
      findAliasByOwner(table, tableAliases) as AliasedTable<*>?

  fun aliasFor(term: TermExpression): AliasedTerm<*>? =
      findAliasByOwner(term, termAliases) as AliasedTerm<*>?

  fun aliasedTable(alias: String): AliasedTable<*>? =
      findAliasedByAlias(alias, tableAliases) as AliasedTable<*>?

  fun aliasedTerm(alias: String): AliasedTerm<*>? =
      findAliasedByAlias(alias, termAliases) as AliasedTerm<*>?

  private fun <T> findAliasByOwner(aliased: T, aliases: List<Aliased<T>>): Aliased<T>? {
    val existing = aliases.filter { a -> a.value == aliased }
    check(existing.size <= 1) { "Too many aliases found for $aliased" }
    return existing.firstOrNull()
  }

  private fun <T> findAliasedByAlias(alias: String, aliases: List<Aliased<T>>): Aliased<T>? {
    val existing = aliases.filter { a -> a.alias == alias }
    check(existing.size <= 1) { "Too many terms found for alias $alias" }
    return existing.firstOrNull()
  }
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
