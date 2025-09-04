package com.skl.query

import com.skl.printer.QueryStringBuilder

sealed interface LiteralTerm : Term, TermExpression, SelectExpression {
  override fun term(): Term = this
}

data class StringLiteral(val value: String) : LiteralTerm {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.string(value)
}

fun String.literal(): StringLiteral = StringLiteral(this)

data class NumberLiteral(val value: Number) : LiteralTerm, LimitExpression, OffsetExpression {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.number(value)
}

fun Number.literal(): NumberLiteral = NumberLiteral(this)

data class RawNumberLiteral(val value: String) : LiteralTerm {
  init {
    require(value.matches(Regex("-?\\d+(\\.\\d+)?"))) { "Invalid raw number literal: $value" }
  }

  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.append(value)
}

fun String.numberLiteral(): RawNumberLiteral = RawNumberLiteral(this)

data class BooleanLiteral(val value: Boolean) : LiteralTerm {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.boolean(value)
}

fun Boolean.literal(): BooleanLiteral = BooleanLiteral(this)

data object NullLiteral : LiteralTerm {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.print(Keyword.NULL)
}

val NULL = NullLiteral
