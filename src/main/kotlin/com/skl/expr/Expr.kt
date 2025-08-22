package com.skl.expr

import com.skl.sql.RenderContext
import com.skl.util.sqlLiteral
import com.skl.util.sqlNonNullLiteral

// Shared boolean expr ops available on any Expr
interface BooleanExprOps {
  infix fun and(other: Expr): Expr = Expr.And(this as Expr, other)

  infix fun or(other: Expr): Expr = Expr.Or(this as Expr, other)

  fun group(): Expr = Expr.Group(this as Expr)
}

sealed interface Expr : BooleanExprOps {
  fun toSql(sb: StringBuilder, ctx: RenderContext)

  data class And(val left: Expr, val right: Expr) : Expr {
    override fun toSql(sb: StringBuilder, ctx: RenderContext) {
      left.toSql(sb, ctx)
      sb.append(" AND ")
      right.toSql(sb, ctx)
    }
  }

  data class Or(val left: Expr, val right: Expr) : Expr {
    override fun toSql(sb: StringBuilder, ctx: RenderContext) {
      left.toSql(sb, ctx)
      sb.append(" OR ")
      right.toSql(sb, ctx)
    }
  }

  data class Group(val inner: Expr) : Expr {
    override fun toSql(sb: StringBuilder, ctx: RenderContext) {
      sb.append("(")
      inner.toSql(sb, ctx)
      sb.append(")")
    }
  }

  data class Eq(val left: Operand.FieldRef, val right: Operand) : Expr {
    override fun toSql(sb: StringBuilder, ctx: RenderContext) {
      val col = left.field.fq(ctx)
      when (right) {
        is Operand.Named -> sb.append(col).append(" = @").append(right.name)

        is Operand.Literal -> {
          if (right.value == null) {
            sb.append(col).append(" IS NULL")
          } else {
            sb.append(col).append(" = ").append(sqlLiteral(right.value))
          }
        }

        is Operand.FieldRef -> sb.append(col).append(" = ").append(right.field.fq(ctx))
        else -> error("Only named params, literals, or field references are supported")
      }
    }
  }

  data class Ne(val left: Operand.FieldRef, val right: Operand) : Expr {
    override fun toSql(sb: StringBuilder, ctx: RenderContext) {
      val col = left.field.fq(ctx)
      when (right) {
        is Operand.Named -> sb.append(col).append(" <> @").append(right.name)
        is Operand.Literal -> {
          val v = right.value
          if (v == null) sb.append(col).append(" IS NOT NULL")
          else sb.append(col).append(" <> ").append(sqlLiteral(v))
        }
        is Operand.FieldRef -> sb.append(col).append(" <> ").append(right.field.fq(ctx))
        else -> error("NE supports named, literal, or field reference values only")
      }
    }
  }

  data class Gt(val left: Operand.FieldRef, val right: Operand) : Expr {
    override fun toSql(sb: StringBuilder, ctx: RenderContext) {
      val col = left.field.fq(ctx)
      when (right) {
        is Operand.Named -> sb.append(col).append(" > @").append(right.name)
        is Operand.Literal -> sb.append(col).append(" > ").append(sqlNonNullLiteral(right.value))
        is Operand.FieldRef -> sb.append(col).append(" > ").append(right.field.fq(ctx))
        else -> error("GT supports named, literal, or field reference values only")
      }
    }
  }

  data class Ge(val left: Operand.FieldRef, val right: Operand) : Expr {
    override fun toSql(sb: StringBuilder, ctx: RenderContext) {
      val col = left.field.fq(ctx)
      when (right) {
        is Operand.Named -> sb.append(col).append(" >= @").append(right.name)
        is Operand.Literal -> sb.append(col).append(" >= ").append(sqlNonNullLiteral(right.value))
        is Operand.FieldRef -> sb.append(col).append(" >= ").append(right.field.fq(ctx))
        else -> error("GE supports named, literal, or field reference values only")
      }
    }
  }

  data class Lt(val left: Operand.FieldRef, val right: Operand) : Expr {
    override fun toSql(sb: StringBuilder, ctx: RenderContext) {
      val col = left.field.fq(ctx)
      when (right) {
        is Operand.Named -> sb.append(col).append(" < @").append(right.name)
        is Operand.Literal -> sb.append(col).append(" < ").append(sqlNonNullLiteral(right.value))
        is Operand.FieldRef -> sb.append(col).append(" < ").append(right.field.fq(ctx))
        else -> error("LT supports named, literal, or field reference values only")
      }
    }
  }

  data class Le(val left: Operand.FieldRef, val right: Operand) : Expr {
    override fun toSql(sb: StringBuilder, ctx: RenderContext) {
      val col = left.field.fq(ctx)
      when (right) {
        is Operand.Named -> sb.append(col).append(" <= @").append(right.name)
        is Operand.Literal -> sb.append(col).append(" <= ").append(sqlNonNullLiteral(right.value))
        is Operand.FieldRef -> sb.append(col).append(" <= ").append(right.field.fq(ctx))
        else -> error("LE supports named, literal, or field reference values only")
      }
    }
  }

  data class Between(val left: Operand.FieldRef, val low: Operand, val high: Operand) : Expr {
    override fun toSql(sb: StringBuilder, ctx: RenderContext) {
      val col = left.field.fq(ctx)
      sb.append(col).append(" BETWEEN ")
      appendOperand(sb, low)
      sb.append(" AND ")
      appendOperand(sb, high)
    }

    private fun appendOperand(sb: StringBuilder, op: Operand) {
      when (op) {
        is Operand.Named -> sb.append("@").append(op.name)
        is Operand.Literal -> sb.append(sqlNonNullLiteral(op.value))
        else -> error("BETWEEN supports named or literal bounds only")
      }
    }
  }

  data class Like(val left: Operand.FieldRef, val pattern: Operand) : Expr {
    override fun toSql(sb: StringBuilder, ctx: RenderContext) {
      val col = left.field.fq(ctx)
      when (pattern) {
        is Operand.Named -> sb.append(col).append(" LIKE @").append(pattern.name)
        is Operand.Literal ->
            sb.append(col).append(" LIKE ").append(sqlNonNullLiteral(pattern.value))
        else -> error("LIKE supports named or literal patterns only")
      }
    }
  }

  data class IsNull(val left: Operand.FieldRef) : Expr {
    override fun toSql(sb: StringBuilder, ctx: RenderContext) {
      sb.append(left.field.fq(ctx)).append(" IS NULL")
    }
  }

  data class IsNotNull(val left: Operand.FieldRef) : Expr {
    override fun toSql(sb: StringBuilder, ctx: RenderContext) {
      sb.append(left.field.fq(ctx)).append(" IS NOT NULL")
    }
  }

  data class InList(val left: Operand.FieldRef, val list: Operand) : Expr {
    override fun toSql(sb: StringBuilder, ctx: RenderContext) {
      val col = left.field.fq(ctx)
      sb.append(col).append(" IN (")
      when (list) {
        is Operand.NamedList -> sb.append(list.names.joinToString(", ") { "@" + it })
        is Operand.LiteralList -> {
          sb.append(list.values.joinToString(", ") { v -> v?.let { sqlLiteral(it) } ?: "NULL" })
        }
        else -> error("IN expects NamedList or LiteralList")
      }
      sb.append(")")
    }
  }

  data class NotInList(val left: Operand.FieldRef, val list: Operand) : Expr {
    override fun toSql(sb: StringBuilder, ctx: RenderContext) {
      val col = left.field.fq(ctx)
      sb.append(col).append(" NOT IN (")
      when (list) {
        is Operand.NamedList -> sb.append(list.names.joinToString(", ") { "@" + it })
        is Operand.LiteralList -> {
          sb.append(list.values.joinToString(", ") { v -> v?.let { sqlLiteral(it) } ?: "NULL" })
        }
        else -> error("NOT IN expects NamedList or LiteralList")
      }
      sb.append(")")
    }
  }
}
