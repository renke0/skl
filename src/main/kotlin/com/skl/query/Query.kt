package com.skl.query

import com.github.vertical_blank.sqlformatter.SqlFormatter
import com.github.vertical_blank.sqlformatter.core.FormatConfig
import com.github.vertical_blank.sqlformatter.languages.Dialect
import com.skl.sql.RenderContext

class Query
internal constructor(
    private val select: Select.Clause,
    private val from: From.Clause,
    private val joins: List<Join.Clause> = emptyList(),
    private val where: Where.Clause? = null
) {
  fun print(): String {
    val sb = StringBuilder()
    val aliases = mutableMapOf<Table, String>()
    from.alias?.let { aliases[from.table] = it }
    joins.forEach { join -> join.alias?.let { aliases[join.table] = it } }

    val ctx = RenderContext(aliases)
    select.appendTo(sb, ctx)
    from.appendTo(sb, ctx)
    joins.forEach { it.appendTo(sb, ctx) }
    where?.appendTo(sb, ctx)
    return sb.toString()
  }

  fun prettyPrint(): String =
      SqlFormatter.of(Dialect.TSql)
          .format(
              print(),
              FormatConfig.builder()
                  .maxColumnLength(120)
                  .indent("  ")
                  .uppercase(true)
                  .linesBetweenQueries(2)
                  .build(),
          )
}
