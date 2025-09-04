package com.skl.printer

import com.skl.query.Keyword

interface Printable {
  fun printTo(qb: QueryStringBuilder): QueryStringBuilder
}

class QueryStringBuilder(val ctx: RenderContext) {
  private val sb = StringBuilder()

  fun append(value: String): QueryStringBuilder {
    sb.append(value)
    return this
  }

  fun print(p: Printable): QueryStringBuilder {
    p.printTo(this)
    return this
  }

  fun <T> printIfNotNull(
      nullable: T?,
      ifTrue: QueryStringBuilder.(notNull: T) -> QueryStringBuilder,
  ): QueryStringBuilder = if (nullable != null) ifTrue(nullable) else this

  fun printList(list: List<Printable>, separator: String = ", "): QueryStringBuilder {
    list.forEachIndexed { index, item ->
      if (index > 0) append(separator)
      print(item)
    }
    return this
  }

  fun space(): QueryStringBuilder = append(" ")

  fun dot(): QueryStringBuilder = append(".")

  fun dotStar(): QueryStringBuilder = append(".*")

  fun string(value: String): QueryStringBuilder =
      append("'").append(value.replace("'", "''")).append("'")

  fun number(value: Number): QueryStringBuilder = append(value.toString())

  fun boolean(value: Boolean): QueryStringBuilder {
    (if (value) Keyword.TRUE else Keyword.FALSE).printTo(this)
    return this
  }

  fun parameter(name: String?): QueryStringBuilder = append(ctx.parameterString(name))

  fun build(): String = sb.toString()
}
