package com.skl.model

import com.skl.expr.Expr
import com.skl.expr.Expr.Between
import com.skl.expr.Expr.Equal
import com.skl.expr.Expr.GreaterOrEqual
import com.skl.expr.Expr.GreaterThan
import com.skl.expr.Expr.InList
import com.skl.expr.Expr.IsNotNull
import com.skl.expr.Expr.IsNull
import com.skl.expr.Expr.LesserOrEqual
import com.skl.expr.Expr.LesserThan
import com.skl.expr.Expr.Like
import com.skl.expr.Expr.NotEqual
import com.skl.expr.Expr.NotInList
import com.skl.expr.Operand.FieldRef
import com.skl.expr.Operand.Literal
import com.skl.expr.Operand.LiteralList
import com.skl.expr.Operand.Named
import com.skl.expr.Operand.NamedList
import com.skl.query.Table
import com.skl.sql.RenderContext

@Suppress("detekt:complexity:TooManyFunctions")
interface FieldOps<T> {
  // Cast helper to access Field members
  private val f
    get() = this as Field<T>

  // equality
  infix fun eq(value: T): Expr = Equal(FieldRef(f), Literal(value))

  infix fun eq(named: Named): Expr = Equal(FieldRef(f), named)

  infix fun <R> eq(field: Field<R>): Expr = Equal(FieldRef(f), FieldRef(field))

  // inequality & comparisons
  infix fun ne(value: T): Expr = NotEqual(FieldRef(f), Literal(value))

  infix fun ne(named: Named): Expr = NotEqual(FieldRef(f), named)

  infix fun <R> ne(field: Field<R>): Expr = NotEqual(FieldRef(f), FieldRef(field))

  infix fun gt(value: T): Expr = GreaterThan(FieldRef(f), Literal(value))

  infix fun gt(named: Named): Expr = GreaterThan(FieldRef(f), named)

  infix fun <R> gt(field: Field<R>): Expr = GreaterThan(FieldRef(f), FieldRef(field))

  infix fun ge(value: T): Expr = GreaterOrEqual(FieldRef(f), Literal(value))

  infix fun ge(named: Named): Expr = GreaterOrEqual(FieldRef(f), named)

  infix fun <R> ge(field: Field<R>): Expr = GreaterOrEqual(FieldRef(f), FieldRef(field))

  infix fun lt(value: T): Expr = LesserThan(FieldRef(f), Literal(value))

  infix fun lt(named: Named): Expr = LesserThan(FieldRef(f), named)

  infix fun <R> lt(field: Field<R>): Expr = LesserThan(FieldRef(f), FieldRef(field))

  infix fun le(value: T): Expr = LesserOrEqual(FieldRef(f), Literal(value))

  infix fun le(named: Named): Expr = LesserOrEqual(FieldRef(f), named)

  infix fun <R> le(field: Field<R>): Expr = LesserOrEqual(FieldRef(f), FieldRef(field))

  // between
  fun between(low: T, high: T): Expr = Between(FieldRef(f), Literal(low), Literal(high))

  fun between(low: Named, high: Named): Expr = Between(FieldRef(f), low, high)

  fun between(low: Named, high: T): Expr = Between(FieldRef(f), low, Literal(high))

  fun between(low: T, high: Named): Expr = Between(FieldRef(f), Literal(low), high)

  // like (available for convenience; caller should use it with string fields)
  infix fun like(pattern: String): Expr = Like(FieldRef(f), Literal(pattern))

  infix fun like(named: Named): Expr = Like(FieldRef(f), named)

  // NULL checks
  fun isNull(): Expr = IsNull(FieldRef(f))

  fun isNotNull(): Expr = IsNotNull(FieldRef(f))

  // IN / NOT IN
  infix fun `in`(values: Iterable<T>): Expr = InList(FieldRef(f), LiteralList(values.toList()))

  infix fun `in`(namedList: NamedList): Expr = InList(FieldRef(f), namedList)

  infix fun notIn(values: Iterable<T>): Expr = NotInList(FieldRef(f), LiteralList(values.toList()))

  infix fun notIn(namedList: NamedList): Expr = NotInList(FieldRef(f), namedList)
}

data class Field<T>(val table: Table, val name: String) : FieldOps<T> {
  fun fq(ctx: RenderContext): String = "${ctx.nameFor(table)}.${name}"
}
