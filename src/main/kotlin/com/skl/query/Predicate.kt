package com.skl.query

import com.skl.printer.Printable
import com.skl.printer.QueryStringBuilder

/**
 * A `Predicate` represents any SQL condition used in `WHERE` or `JOIN` clauses. It is the base type
 * for all logical and comparison conditions, such as `AND`, `OR`, `=`, `<`, `IN`, etc. All specific
 * predicate types (boolean, comparison, etc.) inherit from this interface, allowing you to build
 * and compose complex SQL queries in a type-safe way.
 */
sealed interface Predicate : Printable {
  infix fun and(other: Predicate): Predicate = And(this, other)

  infix fun or(other: Predicate): Predicate = Or(this, other)

  fun group(): Predicate = Group(this)
}

data class Unary(val value: Predicate) : Predicate {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.print(value)
}

data class Not(val value: Predicate) : Predicate {
  val keyword = Keyword.NOT

  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.print(keyword).space().print(value)
}

data class And(val left: Predicate, val right: Predicate) : Predicate {
  val keyword = Keyword.AND

  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.print(left).space().print(keyword).space().print(right)
}

data class Or(val left: Predicate, val right: Predicate) : Predicate {
  val keyword = Keyword.OR

  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.print(left).space().print(keyword).space().print(right)
}

data class Group(val inner: Predicate) : Predicate {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.append("(").print(inner).append(")")
}

// Comparison predicates: =, !=, <, <=, >, >=, IN, LIKE
sealed interface ComparisonPredicate : Predicate {
  val left: Term
  val right: Term
}

sealed interface OperatorComparisonPredicate : ComparisonPredicate {
  val operator: ComparisonOperator

  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.print(left).space().append(operator.operator).space().print(right)
}

data class Eq(
    override val left: Term,
    override val right: Term,
) : OperatorComparisonPredicate {
  override val operator: ComparisonOperator = ComparisonOperator.EQ
}

data class Ne(
    override val left: Term,
    override val right: Term,
) : OperatorComparisonPredicate {
  override val operator: ComparisonOperator = ComparisonOperator.NE
}

data class Lt(
    override val left: Term,
    override val right: Term,
) : OperatorComparisonPredicate {
  override val operator: ComparisonOperator = ComparisonOperator.LT
}

data class Le(
    override val left: Term,
    override val right: Term,
) : OperatorComparisonPredicate {
  override val operator: ComparisonOperator = ComparisonOperator.LE
}

data class Gt(
    override val left: Term,
    override val right: Term,
) : OperatorComparisonPredicate {
  override val operator: ComparisonOperator = ComparisonOperator.GT
}

data class Ge(
    override val left: Term,
    override val right: Term,
) : OperatorComparisonPredicate {
  override val operator: ComparisonOperator = ComparisonOperator.GE
}

sealed interface KeywordComparisonPredicate : ComparisonPredicate {
  val keyword: Keyword

  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.print(left).space().print(keyword).space().print(right)
}

data class In(
    override val left: Term,
    override val right: TermList,
) : KeywordComparisonPredicate {
  override val keyword: Keyword = Keyword.IN
}

data class Like(
    override val left: Term,
    override val right: Term,
) : KeywordComparisonPredicate {
  override val keyword: Keyword = Keyword.LIKE
}

// Null check predicates: IS NULL, IS NOT NULL
sealed interface NullCheckPredicate : Predicate {
  val term: Term
  val keyword: Keyword

  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.print(term).space().print(keyword)
}

data class IsNull(override val term: Term) : NullCheckPredicate {
  override val keyword = Keyword.IS_NULL
}

data class IsNotNull(override val term: Term) : NullCheckPredicate {
  override val keyword = Keyword.IS_NOT_NULL
}

// BETWEEN
data class Between(
    val term: Term,
    val lowerBound: Term,
    val upperBound: Term,
) : Predicate {
  val keyword = Keyword.BETWEEN

  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.print(term)
          .space()
          .print(keyword)
          .space()
          .print(lowerBound)
          .space()
          .print(Keyword.AND)
          .space()
          .print(upperBound)
}

// Quantifier predicates: ANY, ALL, SOME, EXISTS
sealed interface QuantifierPredicate : Predicate {
  val subquery: SelectClause
  val quantifier: Keyword

  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.print(quantifier).space().append("(").print(subquery).append(")")
}

data class Any(override val subquery: SelectClause) : QuantifierPredicate {
  override val quantifier = Keyword.ANY
}

data class All(override val subquery: SelectClause) : QuantifierPredicate {
  override val quantifier = Keyword.ALL
}

data class Some(override val subquery: SelectClause) : QuantifierPredicate {
  override val quantifier = Keyword.SOME
}

data class Exists(override val subquery: SelectClause) : QuantifierPredicate {
  override val quantifier = Keyword.EXISTS
}
