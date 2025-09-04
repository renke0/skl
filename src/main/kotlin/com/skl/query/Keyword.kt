package com.skl.query

import com.skl.printer.Printable
import com.skl.printer.QueryStringBuilder

enum class Keyword(val value: String) : Printable {
  // logic operators
  AND("AND"),
  OR("OR"),
  NOT("NOT"),

  // comparison operators
  IN("IN"),
  IS_NULL("IS NULL"),
  IS_NOT_NULL("IS NOT NULL"),

  // literals
  TRUE("TRUE"),
  FALSE("FALSE"),
  NULL("NULL"),

  // other operators
  LIKE("LIKE"),
  BETWEEN("BETWEEN"),
  EXISTS("EXISTS"),
  ANY("ANY"),
  ALL("ALL"),
  SOME("SOME"),

  // SQL clauses
  SELECT("SELECT"),
  FROM("FROM"),
  JOIN("JOIN"),
  INNER_JOIN("INNER JOIN"),
  LEFT_JOIN("LEFT JOIN"),
  RIGHT_JOIN("RIGHT JOIN"),
  FULL_JOIN("FULL JOIN"),
  CROSS_JOIN("CROSS JOIN"),
  ON("ON"),
  WHERE("WHERE"),
  GROUP_BY("GROUP BY"),
  HAVING("HAVING"),
  ORDER_BY("ORDER BY"),
  ASC("ASC"),
  DESC("DESC"),
  LIMIT("LIMIT"),
  OFFSET("OFFSET"),
  AS("AS"),

  // Order by nulls
  NULLS_FIRST("NULLS FIRST"),
  NULLS_LAST("NULLS LAST");

  override fun printTo(qb: QueryStringBuilder): QueryStringBuilder =
      qb.append(qb.ctx.keywordString(value))
}
