package com.skl.query

import com.skl.model.AliasedTable
import com.skl.model.Field
import com.skl.model.Table
import com.skl.query.Selectable.STAR
import com.skl.sql.RenderContext

class SelectClause(val items: List<SelectItem>) : QueryClause {
  override fun appendTo(sb: StringBuilder, ctx: RenderContext) {
    sb.append("SELECT ")
    if (items.isEmpty()) {
      sb.append("*")
    } else {
      sb.append(items.joinToString(", ") { it.toSql(ctx) })
    }
  }
}

class SelectStep internal constructor(val context: QueryContext) {
  fun from(block: () -> Any): FromStep =
      when (val result = block()) {
        is AliasedTable -> context.from(FromClause(result.table, result.alias))
        is Table -> context.from(FromClause(result))
        else ->
            error(
                "from { ... } must return Table or AliasedTable, got: ${result::class.simpleName}",
            )
      }
}

interface Selectable {
  object STAR : Selectable {
    override fun toString(): String = "*"
  }
}

sealed class SelectItem {
  abstract fun toSql(ctx: RenderContext): String

  data class FieldItem(val field: Field<*>) : SelectItem() {
    override fun toSql(ctx: RenderContext): String = field.fq(ctx)
  }

  data class TableAllFields(val table: Table) : SelectItem() {
    override fun toSql(ctx: RenderContext): String = "${ctx.nameFor(table)}.*"
  }

  data object AllFields : SelectItem() {
    override fun toSql(ctx: RenderContext): String = "*"
  }
}

fun select(vararg items: Selectable): SelectStep {
  val selectItems =
      when {
        items.isEmpty() -> listOf(SelectItem.AllFields)
        else ->
            items.map { item ->
              when (item) {
                is Field<*> -> SelectItem.FieldItem(item)
                is Table -> SelectItem.TableAllFields(item)
                is STAR -> SelectItem.AllFields
                else ->
                    error(
                        "Select items must be Field, Table, or STAR, got: ${item::class.simpleName}",
                    )
              }
            }
      }
  return QueryContext.select(SelectClause(selectItems))
}
