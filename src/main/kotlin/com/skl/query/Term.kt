package com.skl.query

import com.skl.printer.Printable
import com.skl.printer.QueryStringBuilder

/**
 * Represents a SQL term, which is an atomic value or expression used in predicates. Terms can be
 * column references, literals, function calls, or expressions, and serve as the left or right side
 * of comparison predicates. They encapsulate the building blocks of SQL conditions.
 */
sealed interface Term : Printable

data object STAR : Term, SelectExpression {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.append("*")

  override fun printOn(clause: Clause): Printable =
      when (clause) {
        Clause.SELECT -> this
        else -> error("STAR cannot be used in $clause")
      }
}

data class TermList(val terms: List<Term>) : Term {
  init {
    require(terms.isNotEmpty()) { "TermList must contain at least one term" }
  }

  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.append("(").printList(terms).append(")")
}

data class Alias<T : Term>(val alias: String, val term: T) : Term {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.append(alias)
}

data class AliasedTerm<T : TermExpression>(override val alias: String, val term: T) :
    Aliased<T>, TermExpression, SelectExpression, GroupByExpression, OrderByExpression {
  override val value = term

  override fun term(): Term = Alias(alias, term.term())

  override fun printOn(clause: Clause): Printable =
      when (clause) {
        Clause.SELECT ->
            Printable.of { qb ->
              qb.print(term.term()).space().print(Keyword.AS).space().append(alias)
            }

        Clause.GROUP_BY,
        Clause.ORDER_BY -> Printable.of { qb -> qb.append(alias) }
        else -> error("Aliased term cannot be used in $clause")
      }
}

data class AliasRef(val alias: String) : Term {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.print(qb.ctx.aliasedTerm(alias)?.term() ?: error("Unknown alias: $alias"))
}

data class AliasRefTerm(val alias: String) : TermExpression, GroupByExpression, OrderByExpression {
  override fun term(): Term = AliasRef(alias)

  override fun printOn(clause: Clause): Printable =
      Printable.of { qb ->
        qb.ctx.aliasedTerm(alias)?.printOn(clause)?.let(qb::print) ?: error("Unknown alias: $alias")
      }
}

sealed interface TermExpression {
  fun term(): Term

  infix fun `as`(alias: String): AliasedTerm<TermExpression> = AliasedTerm(alias, this)

  infix fun eq(that: TermExpression): ComparisonPredicate = Eq(this.term(), that.term())

  infix fun eq(that: String): ComparisonPredicate = Eq(this.term(), StringLiteral(that))

  infix fun eq(that: Number): ComparisonPredicate = Eq(this.term(), NumberLiteral(that))

  infix fun eq(that: Boolean): ComparisonPredicate = Eq(this.term(), BooleanLiteral(that))

  infix fun ne(that: TermExpression): ComparisonPredicate = Ne(this.term(), that.term())

  infix fun ne(that: String): ComparisonPredicate = Ne(this.term(), StringLiteral(that))

  infix fun ne(that: Number): ComparisonPredicate = Ne(this.term(), NumberLiteral(that))

  infix fun ne(that: Boolean): ComparisonPredicate = Ne(this.term(), BooleanLiteral(that))

  infix fun lt(that: TermExpression): ComparisonPredicate = Lt(this.term(), that.term())

  infix fun lt(that: String): ComparisonPredicate = Lt(this.term(), StringLiteral(that))

  infix fun lt(that: Number): ComparisonPredicate = Lt(this.term(), NumberLiteral(that))

  infix fun lt(that: Boolean): ComparisonPredicate = Lt(this.term(), BooleanLiteral(that))

  infix fun le(that: TermExpression): ComparisonPredicate = Le(this.term(), that.term())

  infix fun le(that: String): ComparisonPredicate = Le(this.term(), StringLiteral(that))

  infix fun le(that: Number): ComparisonPredicate = Le(this.term(), NumberLiteral(that))

  infix fun le(that: Boolean): ComparisonPredicate = Le(this.term(), BooleanLiteral(that))

  infix fun gt(that: TermExpression): ComparisonPredicate = Gt(this.term(), that.term())

  infix fun gt(that: String): ComparisonPredicate = Gt(this.term(), StringLiteral(that))

  infix fun gt(that: Number): ComparisonPredicate = Gt(this.term(), NumberLiteral(that))

  infix fun gt(that: Boolean): ComparisonPredicate = Gt(this.term(), BooleanLiteral(that))

  infix fun ge(that: TermExpression): ComparisonPredicate = Ge(this.term(), that.term())

  infix fun ge(that: String): ComparisonPredicate = Ge(this.term(), StringLiteral(that))

  infix fun ge(that: Number): ComparisonPredicate = Ge(this.term(), NumberLiteral(that))

  infix fun ge(that: Boolean): ComparisonPredicate = Ge(this.term(), BooleanLiteral(that))

  infix fun like(that: TermExpression): ComparisonPredicate = Like(this.term(), that.term())

  infix fun like(that: String): ComparisonPredicate = Like(this.term(), StringLiteral(that))

  infix fun like(that: Number): ComparisonPredicate = Like(this.term(), NumberLiteral(that))

  infix fun like(that: Boolean): ComparisonPredicate = Like(this.term(), BooleanLiteral(that))

  infix fun `in`(list: List<TermExpression>): ComparisonPredicate =
      In(this.term(), TermList(list.map { it.term() }))

  class BetweenRange
  internal constructor(val lowerBound: TermExpression, val upperBound: TermExpression)

  infix fun between(range: BetweenRange): Between =
      Between(this.term(), range.lowerBound.term(), range.upperBound.term())

  infix fun and(that: TermExpression): BetweenRange = BetweenRange(this, that)

  infix fun and(that: String): BetweenRange = BetweenRange(this, StringLiteral(that))

  infix fun and(that: Number): BetweenRange = BetweenRange(this, NumberLiteral(that))

  infix fun and(that: Boolean): BetweenRange = BetweenRange(this, BooleanLiteral(that))

  fun isNull(): NullCheckPredicate = IsNull(this.term())

  fun isNotNull(): NullCheckPredicate = IsNotNull(this.term())
}
