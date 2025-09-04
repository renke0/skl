package com.skl.query

import com.skl.printer.Printable
import com.skl.printer.QueryStringBuilder

sealed interface Function : Printable, SelectExpression, OrderByExpression, TermExpression {
  val name: String
  val arguments: Array<out Term>

  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.append(name).append("(").printList(arguments.toList()).append(")")

  override fun term(): Term = FunctionTerm(this)
}

class AggregateFunction(
    override val name: String,
    override vararg val arguments: Term,
) : Function {
  override fun printOn(clause: Clause): Printable =
      when (clause) {
        Clause.SELECT,
        Clause.ORDER_BY -> this
        else -> error("Aggregate function cannot be used in $clause")
      }
}

class ScalarFunction(
    override val name: String,
    override vararg val arguments: Term,
) : Function, GroupByExpression {
  override fun printOn(clause: Clause): Printable =
      when (clause) {
        Clause.SELECT,
        Clause.ORDER_BY,
        Clause.GROUP_BY -> this
        else -> error("Scalar function cannot be used in $clause")
      }
}

class FunctionTerm(val function: Function) : Term {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.print(function)
}

object Functions {
  fun count(): AggregateFunction = AggregateFunction("COUNT", STAR)

  fun count(expression: TermExpression): AggregateFunction =
      AggregateFunction("COUNT", expression.term())

  fun sum(expression: TermExpression): AggregateFunction =
      AggregateFunction("SUM", expression.term())

  fun avg(termExpression: TermExpression): AggregateFunction =
      AggregateFunction("AVG", termExpression.term())

  fun min(expression: TermExpression): AggregateFunction =
      AggregateFunction("MIN", expression.term())

  fun max(expression: TermExpression): AggregateFunction =
      AggregateFunction("MAX", expression.term())

  fun lower(expression: TermExpression): ScalarFunction = ScalarFunction("LOWER", expression.term())

  fun upper(term: Term): ScalarFunction = ScalarFunction("UPPER", term)

  fun length(term: Term): ScalarFunction = ScalarFunction("LENGTH", term)
}
