package com.skl.query

import com.skl.expr.Expr
import com.skl.sql.JoinClause
import com.skl.sql.JoinType

class JoinBuilder(private val type: JoinType) {
  private lateinit var table: Table
  private var alias: String? = null
  private var condition: Expr? = null

  infix fun Table.on(conditionBlock: () -> Expr): JoinBuilder {
    this@JoinBuilder.table = this
    this@JoinBuilder.condition = conditionBlock()
    return this@JoinBuilder
  }

  infix fun AliasedTable.on(conditionBlock: () -> Expr): JoinBuilder {
    this@JoinBuilder.table = this.table
    this@JoinBuilder.alias = this.alias
    this@JoinBuilder.condition = conditionBlock()
    return this@JoinBuilder
  }

  internal fun build(): JoinClause {
    return JoinClause(type, table, alias, condition)
  }
}
