package com.skl

import com.skl.SKL.select
import com.skl.fixtures.Customers
import com.skl.fixtures.Orders
import com.skl.query.Functions.count
import com.skl.query.Parameter
import com.skl.query.literal
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class OffsetTest :
    StringSpec(
        {
          // Basic OFFSET tests
          "offset with integer literal" {
            val actual = select().from { Customers }.offset(10).str()
            actual shouldBe "SELECT * FROM customers OFFSET 10"
          }

          "offset with zero" {
            val actual = select().from { Customers }.offset(0).str()
            actual shouldBe "SELECT * FROM customers OFFSET 0"
          }

          // OFFSET with parameter
          "offset with parameter" {
            val actual = select().from { Customers }.offset(Parameter("offset")).str()
            actual shouldBe "SELECT * FROM customers OFFSET @offset"
          }

          // OFFSET with other clauses
          "offset with where clause" {
            val actual =
                select()
                    .from { Customers }
                    .where { Customers.email like "%@example.com".literal() }
                    .offset(10)
                    .str()
            actual shouldBe
                "SELECT * FROM customers " +
                    "WHERE customers.email LIKE '%@example.com' " +
                    "OFFSET 10"
          }

          "offset with order by clause" {
            val actual = select().from { Customers }.orderBy(Customers.fullName).offset(10).str()
            actual shouldBe "SELECT * FROM customers ORDER BY customers.full_name OFFSET 10"
          }

          "offset with limit clause" {
            val actual = select().from { Customers }.limit(20).offset(10).str()
            actual shouldBe "SELECT * FROM customers LIMIT 20 OFFSET 10"
          }

          // Complex queries with OFFSET
          "offset with group by, having, and order by" {
            val actual =
                select(Orders.status, count() `as` "order_count")
                    .from { Orders }
                    .groupBy(Orders.status)
                    .having { count() gt 5 }
                    .orderBy(count().desc())
                    .offset(10)
                    .str()
            actual shouldBe
                "SELECT sales.orders.status, COUNT(*) AS order_count " +
                    "FROM sales.orders " +
                    "GROUP BY sales.orders.status " +
                    "HAVING COUNT(*) > 5 " +
                    "ORDER BY COUNT(*) DESC " +
                    "OFFSET 10"
          }

          "complex query with all clauses including offset" {
            val actual =
                select(Orders.status, count() `as` "order_count")
                    .from { Orders }
                    .where { Orders.totalAmount gt 100.0 }
                    .groupBy(Orders.status)
                    .having { count() gt 5 }
                    .orderBy(count().desc())
                    .limit(10)
                    .offset(20)
                    .str()
            actual shouldBe
                "SELECT sales.orders.status, COUNT(*) AS order_count " +
                    "FROM sales.orders " +
                    "WHERE sales.orders.total_amount > 100.0 " +
                    "GROUP BY sales.orders.status " +
                    "HAVING COUNT(*) > 5 " +
                    "ORDER BY COUNT(*) DESC " +
                    "LIMIT 10 " +
                    "OFFSET 20"
          }
        },
    )
