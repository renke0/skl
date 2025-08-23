package com.skl.model

import com.skl.expr.Expr
import com.skl.expr.Operand
import com.skl.query.Selectable
import com.skl.query.Table
import com.skl.sql.RenderContext

@Suppress("detekt:complexity:TooManyFunctions")
interface FieldOps<T> {
  // Cast helper to access Field members
  private val f
    get() = this as Field<T>

  // equality
  infix fun eq(value: T): Expr = Expr.Eq(Operand.FieldRef(f), Operand.Literal(value))

  infix fun eq(named: Operand.Named): Expr = Expr.Eq(Operand.FieldRef(f), named)

  infix fun <R> eq(field: Field<R>): Expr = Expr.Eq(Operand.FieldRef(f), Operand.FieldRef(field))

  // inequality
  infix fun ne(value: T): Expr = Expr.Ne(Operand.FieldRef(f), Operand.Literal(value))

  infix fun ne(named: Operand.Named): Expr = Expr.Ne(Operand.FieldRef(f), named)

  infix fun <R> ne(field: Field<R>): Expr = Expr.Ne(Operand.FieldRef(f), Operand.FieldRef(field))

  // comparisons
  infix fun gt(value: T): Expr = Expr.Gt(Operand.FieldRef(f), Operand.Literal(value))

  infix fun gt(named: Operand.Named): Expr = Expr.Gt(Operand.FieldRef(f), named)

  infix fun <R> gt(field: Field<R>): Expr = Expr.Gt(Operand.FieldRef(f), Operand.FieldRef(field))

  infix fun ge(value: T): Expr = Expr.Ge(Operand.FieldRef(f), Operand.Literal(value))

  infix fun ge(named: Operand.Named): Expr = Expr.Ge(Operand.FieldRef(f), named)

  infix fun <R> ge(field: Field<R>): Expr = Expr.Ge(Operand.FieldRef(f), Operand.FieldRef(field))

  infix fun lt(value: T): Expr = Expr.Lt(Operand.FieldRef(f), Operand.Literal(value))

  infix fun lt(named: Operand.Named): Expr = Expr.Lt(Operand.FieldRef(f), named)

  infix fun <R> lt(field: Field<R>): Expr = Expr.Lt(Operand.FieldRef(f), Operand.FieldRef(field))

  infix fun le(value: T): Expr = Expr.Le(Operand.FieldRef(f), Operand.Literal(value))

  infix fun le(named: Operand.Named): Expr = Expr.Le(Operand.FieldRef(f), named)

  infix fun <R> le(field: Field<R>): Expr = Expr.Le(Operand.FieldRef(f), Operand.FieldRef(field))

  // between
  fun between(low: T, high: T): Expr =
      Expr.Between(Operand.FieldRef(f), Operand.Literal(low), Operand.Literal(high))

  fun between(low: Operand.Named, high: Operand.Named): Expr =
      Expr.Between(Operand.FieldRef(f), low, high)

  fun between(low: Operand.Named, high: T): Expr =
      Expr.Between(Operand.FieldRef(f), low, Operand.Literal(high))

  fun between(low: T, high: Operand.Named): Expr =
      Expr.Between(Operand.FieldRef(f), Operand.Literal(low), high)

  // like (available for convenience; caller should use it with string fields)
  infix fun like(pattern: String): Expr = Expr.Like(Operand.FieldRef(f), Operand.Literal(pattern))

  infix fun like(named: Operand.Named): Expr = Expr.Like(Operand.FieldRef(f), named)

  // NULL checks
  fun isNull(): Expr = Expr.IsNull(Operand.FieldRef(f))

  fun isNotNull(): Expr = Expr.IsNotNull(Operand.FieldRef(f))

  // IN / NOT IN
  infix fun `in`(values: Iterable<T>): Expr =
      Expr.InList(Operand.FieldRef(f), Operand.LiteralList(values.toList()))

  infix fun `in`(namedList: Operand.NamedList): Expr = Expr.InList(Operand.FieldRef(f), namedList)

  infix fun notIn(values: Iterable<T>): Expr =
      Expr.NotInList(Operand.FieldRef(f), Operand.LiteralList(values.toList()))

  infix fun notIn(namedList: Operand.NamedList): Expr =
      Expr.NotInList(Operand.FieldRef(f), namedList)
}

data class Field<T>(val table: Table, val name: String) : FieldOps<T>, Selectable {
  fun fq(ctx: RenderContext): String = "${ctx.nameFor(table)}.${name}"
}
