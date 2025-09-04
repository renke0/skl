package com.skl.query

import com.skl.printer.Printable
import com.skl.printer.QueryStringBuilder

class SelectClause(val selected: List<SelectArgument>) : QueryClause {
  val keyword = Keyword.SELECT

  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.print(keyword).space().let {
        if (selected.isEmpty()) it.append("*") else it.printList(selected)
      }
}

class SelectStep internal constructor(override val context: QueryContext) :
    FromSupport, WhereSupport

interface SelectSupport : QueryStep {
  fun select(block: () -> List<SelectExpression>): SelectStep =
      context.select(
          SelectClause(
              block().map {
                when (it) {
                  STAR -> SelectStar
                  is Column -> SelectColumn(it)
                  is Table<*> -> SelectTable(it)
                  is AliasedTable<*> -> SelectAliasedTable(it)
                  is AliasedTerm<*> -> SelectAliased(it)
                  is Function -> SelectFunction(it)
                  is LiteralTerm -> SelectLiteral(it)
                  is Parameter -> SelectParameter(it)
                }
              },
          ),
      )
}

sealed interface SelectExpression

sealed interface SelectArgument : Printable

data object SelectStar : SelectArgument {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.append("*")
}

data class SelectColumn(val column: Column) : SelectArgument {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.print(column.term())
}

data class SelectAliased(val aliased: AliasedTerm<*>) : SelectArgument {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.print(aliased.expression.term()).space().print(Keyword.AS).space().append(aliased.alias)
}

data class SelectTable(val table: Table<*>) : SelectArgument {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.append(qb.ctx.aliasFor(table) ?: table.name()).dotStar()
}

data class SelectAliasedTable(val table: AliasedTable<*>) : SelectArgument {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.append(table.alias).dotStar()
}

data class SelectFunction(val function: Function) : SelectArgument {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.print(function)
}

data class SelectLiteral(val literal: LiteralTerm) : SelectArgument {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.print(literal)
}

data class SelectParameter(val parameter: Parameter) : SelectArgument {
  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder = qb.print(parameter)
}
