package com.skl.sql

enum class JoinType(val sql: String) {
  INNER("INNER JOIN"),
  LEFT("LEFT JOIN"),
  RIGHT("RIGHT JOIN"),
  FULL("FULL JOIN"),
  CROSS("CROSS JOIN")
}
