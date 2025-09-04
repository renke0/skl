package com.skl.query

import com.skl.printer.Printable
import com.skl.printer.QueryStringBuilder

sealed interface LiteralTerm : Term, TermExpression, SelectExpression {
  override fun term(): Term = this
}

data class StringLiteral(val value: String) : LiteralTerm {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.string(value)

  override fun printOn(clause: Clause): Printable =
      when (clause) {
        Clause.SELECT -> this
        else -> error("String literal cannot be used in $clause")
      }
}

fun String.literal(): StringLiteral = StringLiteral(this)

data class NumberLiteral(val value: Number) : LiteralTerm, LimitExpression, OffsetExpression {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.number(value)

  override fun printOn(clause: Clause): Printable =
      when (clause) {
        Clause.SELECT,
        Clause.LIMIT,
        Clause.OFFSET -> this
        else -> error("Number literal cannot be used in $clause")
      }
}

fun Number.literal(): NumberLiteral = NumberLiteral(this)

data class RawNumberLiteral(val value: String) : LiteralTerm {
  init {
    require(value.matches(Regex("-?\\d+(\\.\\d+)?"))) { "Invalid raw number literal: $value" }
  }

  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.append(value)

  override fun printOn(clause: Clause): Printable =
      when (clause) {
        Clause.SELECT,
        Clause.LIMIT,
        Clause.OFFSET -> this
        else -> error("Number literal cannot be used in $clause")
      }
}

fun String.numberLiteral(): RawNumberLiteral = RawNumberLiteral(this)

data class BooleanLiteral(val value: Boolean) : LiteralTerm {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.boolean(value)

  override fun printOn(clause: Clause): Printable =
      when (clause) {
        Clause.SELECT -> this
        else -> error("Boolean literal cannot be used in $clause")
      }
}

fun Boolean.literal(): BooleanLiteral = BooleanLiteral(this)

data object NullLiteral : LiteralTerm {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.print(Keyword.NULL)

  override fun printOn(clause: Clause): Printable =
      when (clause) {
        Clause.SELECT -> this
        else -> error("NULL literal cannot be used in $clause")
      }
}

val NULL = NullLiteral
