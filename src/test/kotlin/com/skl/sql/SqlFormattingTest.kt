package com.skl.sql

import com.skl.query.`as`
import com.skl.query.from
import com.skl.test.Tables
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SqlFormattingTest {
  private val users = Tables.Users()
  private val orders = Tables.Orders()

  @Test
  fun `test pretty print formats SQL correctly`() {
    val q =
        from { users `as` "us" }
            .join { orders on { users.id eq orders.userId } }
            .select(users.firstName, orders.total)
            .where { orders.total gt 100.0 }

    val prettyPrinted = q.prettyPrint()

    // Verify that the pretty printed SQL is properly formatted
    assertTrue(prettyPrinted.contains("SELECT"), "Should contain SELECT keyword")
    assertTrue(prettyPrinted.contains("FROM"), "Should contain FROM keyword")
    assertTrue(prettyPrinted.contains("JOIN"), "Should contain JOIN keyword")
    assertTrue(prettyPrinted.contains("WHERE"), "Should contain WHERE keyword")

    // Verify that the pretty printed SQL is uppercase as configured
    assertTrue(prettyPrinted.contains("SELECT"), "Keywords should be uppercase")
    assertTrue(prettyPrinted.contains("FROM"), "Keywords should be uppercase")
    assertTrue(prettyPrinted.contains("JOIN"), "Keywords should be uppercase")
    assertTrue(prettyPrinted.contains("WHERE"), "Keywords should be uppercase")

    // Verify that the pretty printed SQL has proper indentation
    val lines = prettyPrinted.split("\n")
    assertTrue(lines.size > 1, "Pretty printed SQL should span multiple lines")
    assertTrue(lines[1].startsWith("  "), "Second line should be indented")
  }

  @Test
  fun `test print and pretty print produce equivalent SQL`() {
    val q =
        from { users }
            .leftJoin { orders on { users.id eq orders.userId } }
            .select(users.firstName, orders.total)
            .toQuery()

    val printed = q.print()
    val prettyPrinted = q.prettyPrint()

    // Remove whitespace and convert to lowercase for comparison
    val normalizedPrinted = printed.replace("\\s+".toRegex(), " ").trim().lowercase()
    val normalizedPrettyPrinted = prettyPrinted.replace("\\s+".toRegex(), " ").trim().lowercase()

    // The normalized versions should be equivalent
    assertEquals(
        normalizedPrinted,
        normalizedPrettyPrinted,
        "print() and prettyPrint() should produce equivalent SQL when normalized")
  }
}
