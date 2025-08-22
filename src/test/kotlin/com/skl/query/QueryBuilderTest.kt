package com.skl.query

import com.skl.expr.param
import com.skl.test.Tables
import kotlin.test.Test
import kotlin.test.assertEquals

class QueryBuilderTest {
  private val users = Tables.Users()
  private val orders = Tables.Orders()
  private val products = Tables.Products()
  private val orderItems = Tables.OrderItems()

  @Test
  fun `test basic query`() {
    val q =
        from { users `as` "us" }
            .select(users.firstName, users.age)
            .where {
              (users.firstName ne param("firstname")) and
                  (users.age eq param("age")) and
                  (users.age eq 18)
            }

    val expectedSql =
        "SELECT us.first_name, us.age " +
            "FROM users us " +
            "WHERE us.first_name <> @firstname AND us.age = @age AND us.age = 18"
    assertEquals(expectedSql, q.print())
  }

  @Test
  fun `test join query`() {
    val q =
        from { users `as` "us" }
            .join { orders on { users.id eq orders.userId } }
            .select(users.firstName, orders.total)
            .where { orders.total gt 100.0 }

    val expectedSql =
        "SELECT us.first_name, orders.total " +
            "FROM users us " +
            "INNER JOIN orders ON us.id = orders.user_id WHERE orders.total > 100.0"
    assertEquals(expectedSql, q.print())
  }

  @Test
  fun `test left join query`() {
    val q =
        from { users `as` "us" }
            .leftJoin { orders on { users.id eq orders.userId } }
            .select(users.firstName, orders.total)
            .toQuery()

    val expectedSql =
        "SELECT us.first_name, orders.total " +
            "FROM users us " +
            "LEFT JOIN orders ON us.id = orders.user_id"
    assertEquals(expectedSql, q.print())
  }

  @Test
  fun `test multiple joins query`() {
    val q =
        from { users }
            .join { orders on { users.id eq orders.userId } }
            .join { orderItems on { orders.id eq orderItems.orderId } }
            .rightJoin { products on { orderItems.productId eq products.id } }
            .select(users.firstName, orders.total, products.productName, orderItems.quantity)
            .toQuery()

    val expectedSql =
        "SELECT users.first_name, orders.total, products.name, order_items.quantity " +
            "FROM users INNER JOIN orders ON users.id = orders.user_id " +
            "INNER JOIN order_items ON orders.id = order_items.order_id " +
            "RIGHT JOIN products ON order_items.product_id = products.id"
    assertEquals(expectedSql, q.print())
  }
}
