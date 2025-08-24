package com.skl.query

import com.skl.expr.param
import com.skl.query.Selectable.STAR
import com.skl.test.Tables
import kotlin.test.Test
import kotlin.test.assertEquals

class QueryBuilderTest {
  private val users = Tables.Users()
  private val orders = Tables.Orders()
  private val products = Tables.Products()
  private val orderItems = Tables.OrderItems()

  @Test
  fun `test select star with no params`() {
    val q = select().from { users }.toQuery()
    val expectedSql = "SELECT * FROM users"
    assertEquals(expectedSql, q.print())
  }

  @Test
  fun `test select star with star param`() {
    val q = select(STAR).from { users }.toQuery()
    val expectedSql = "SELECT * FROM users"
    assertEquals(expectedSql, q.print())
  }

  @Test
  fun `test select all fields from table`() {
    val q = select(users).from { users }.toQuery()
    val expectedSql = "SELECT users.* FROM users"
    assertEquals(expectedSql, q.print())
  }

  @Test
  fun `test select mixed fields and tables`() {
    val q =
        select(users, orders.id, STAR)
            .from { users }
            .join { orders on { users.id eq orders.userId } }
            .toQuery()
    val expectedSql =
        "SELECT users.*, orders.id, * FROM users JOIN orders ON users.id = orders.user_id"
    assertEquals(expectedSql, q.print())
  }

  @Test
  fun `test basic query`() {
    val q =
        select(users.firstName, users.age)
            .from { users `as` "us" }
            .where {
              (users.firstName ne param("firstname")) and
                  (users.age eq param("age")) and
                  (users.age eq 18)
            }
            .toQuery()

    val expectedSql =
        "SELECT us.first_name, us.age " +
            "FROM users us " +
            "WHERE us.first_name <> @firstname AND us.age = @age AND us.age = 18"
    assertEquals(expectedSql, q.print())
  }

  @Test
  fun `test join query`() {
    val q =
        select(users.firstName, orders.total)
            .from { users `as` "us" }
            .join { orders on { users.id eq orders.userId } }
            .where { orders.total gt 100.0 }
            .toQuery()

    val expectedSql =
        "SELECT us.first_name, orders.total " +
            "FROM users us " +
            "JOIN orders ON us.id = orders.user_id WHERE orders.total > 100.0"
    assertEquals(expectedSql, q.print())
  }

  @Test
  fun `test inner join query`() {
    val q =
        select(users.firstName, orders.total)
            .from { users `as` "us" }
            .innerJoin { orders on { users.id eq orders.userId } }
            .toQuery()

    val expectedSql =
        "SELECT us.first_name, orders.total " +
            "FROM users us " +
            "INNER JOIN orders ON us.id = orders.user_id"
    assertEquals(expectedSql, q.print())
  }

  @Test
  fun `test left join query`() {
    val q =
        select(users.firstName, orders.total)
            .from { users `as` "us" }
            .leftJoin { orders on { users.id eq orders.userId } }
            .toQuery()

    val expectedSql =
        "SELECT us.first_name, orders.total " +
            "FROM users us " +
            "LEFT JOIN orders ON us.id = orders.user_id"
    assertEquals(expectedSql, q.print())
  }

  @Test
  fun `test right join query`() {
    val q =
        select(users.firstName, orders.total)
            .from { users `as` "us" }
            .rightJoin { orders on { users.id eq orders.userId } }
            .toQuery()

    val expectedSql =
        "SELECT us.first_name, orders.total " +
            "FROM users us " +
            "RIGHT JOIN orders ON us.id = orders.user_id"
    assertEquals(expectedSql, q.print())
  }

  @Test
  fun `test full join query`() {
    val q =
        select(users.firstName, orders.total)
            .from { users `as` "us" }
            .fullJoin { orders on { users.id eq orders.userId } }
            .toQuery()

    val expectedSql =
        "SELECT us.first_name, orders.total " +
            "FROM users us " +
            "FULL JOIN orders ON us.id = orders.user_id"
    assertEquals(expectedSql, q.print())
  }

  @Test
  fun `test cross join query`() {
    val q =
        select(users.firstName, orders.total)
            .from { users `as` "us" }
            .crossJoin { orders() }
            .toQuery()

    val expectedSql = "SELECT us.first_name, orders.total " + "FROM users us " + "CROSS JOIN orders"
    assertEquals(expectedSql, q.print())
  }

  @Test
  fun `test multiple joins query`() {
    val q =
        select(users.firstName, orders.total, products.productName, orderItems.quantity)
            .from { users }
            .join { orders on { users.id eq orders.userId } }
            .innerJoin { orders on { users.id eq orders.userId } }
            .leftJoin { orderItems on { orders.id eq orderItems.orderId } }
            .rightJoin { products on { orderItems.productId eq products.id } }
            .fullJoin { users on { users.id eq orders.id } }
            .crossJoin { orders() }
            .toQuery()

    val expectedSql =
        "SELECT users.first_name, orders.total, products.name, order_items.quantity " +
            "FROM users " +
            "JOIN orders ON users.id = orders.user_id " +
            "INNER JOIN orders ON users.id = orders.user_id " +
            "LEFT JOIN order_items ON orders.id = order_items.order_id " +
            "RIGHT JOIN products ON order_items.product_id = products.id " +
            "FULL JOIN users ON users.id = orders.id " +
            "CROSS JOIN orders"
    assertEquals(expectedSql, q.print())
  }

  @Test
  fun `test group by clause`() {
    val q = select(users.age, users.id).from { users }.groupBy(users.age).toQuery()

    val expectedSql = "SELECT users.age, users.id " + "FROM users " + "GROUP BY users.age"
    assertEquals(expectedSql, q.print())
  }

  @Test
  fun `test group by with where clause`() {
    val q =
        select(users.age, users.id)
            .from { users }
            .where { users.age gt 18 }
            .groupBy(users.age)
            .toQuery()

    val expectedSql =
        "SELECT users.age, users.id " +
            "FROM users " +
            "WHERE users.age > 18 " +
            "GROUP BY users.age"
    assertEquals(expectedSql, q.print())
  }

  @Test
  fun `test having clause`() {
    val q =
        select(users.age, users.id)
            .from { users }
            .groupBy(users.age)
            .having { users.id gt 1 }
            .toQuery()

    val expectedSql =
        "SELECT users.age, users.id " +
            "FROM users " +
            "GROUP BY users.age " +
            "HAVING users.id > 1"
    assertEquals(expectedSql, q.print())
  }

  @Test
  fun `test order by clause`() {
    val q =
        select(users.firstName, users.age)
            .from { users }
            .orderBy(users.age, users.firstName)
            .toQuery()

    val expectedSql =
        "SELECT users.first_name, users.age " +
            "FROM users " +
            "ORDER BY users.age, users.first_name"
    assertEquals(expectedSql, q.print())
  }

  @Test
  fun `test group by having and order by clauses`() {
    val q =
        select(users.age, users.id)
            .from { users }
            .where { users.firstName like "J%" }
            .groupBy(users.age)
            .having { users.id gt 1 }
            .orderBy(users.age)
            .toQuery()

    val expectedSql =
        "SELECT users.age, users.id " +
            "FROM users " +
            "WHERE users.first_name LIKE 'J%' " +
            "GROUP BY users.age " +
            "HAVING users.id > 1 " +
            "ORDER BY users.age"
    assertEquals(expectedSql, q.print())
  }

  @Test
  fun `test join with group by having and order by clauses`() {
    val q =
        select(users.age, users.id, orders.total)
            .from { users }
            .join { orders on { users.id eq orders.userId } }
            .where { orders.total gt 100.0 }
            .groupBy(users.age, orders.total)
            .having { users.id gt 1 }
            .orderBy(users.age, orders.total)
            .toQuery()

    val expectedSql =
        "SELECT users.age, users.id, orders.total " +
            "FROM users " +
            "JOIN orders ON users.id = orders.user_id " +
            "WHERE orders.total > 100.0 " +
            "GROUP BY users.age, orders.total " +
            "HAVING users.id > 1 " +
            "ORDER BY users.age, orders.total"
    assertEquals(expectedSql, q.print())
  }
}
