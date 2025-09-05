package com.skl

import com.skl.SKL.select
import com.skl.fixtures.Customers
import com.skl.fixtures.Orders
import com.skl.query.Functions.count
import com.skl.query.Parameter
import com.skl.query.literal
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class LimitTest :
    StringSpec(
        {
          // Basic LIMIT tests
          "limit with integer literal" {
            val actual = select().from { Customers }.limit(10).str()
            actual shouldBe "SELECT * FROM customers LIMIT 10"
          }

          "limit with zero" {
            val actual = select().from { Customers }.limit(0).str()
            actual shouldBe "SELECT * FROM customers LIMIT 0"
          }

          "limit with one" {
            val actual = select().from { Customers }.limit(1).str()
            actual shouldBe "SELECT * FROM customers LIMIT 1"
          }

          // LIMIT with parameter
          "limit with parameter" {
            val actual = select().from { Customers }.limit(Parameter("limit")).str()
            actual shouldBe "SELECT * FROM customers LIMIT @limit"
          }

          // LIMIT with other clauses
          "limit with where clause" {
            val actual =
                select()
                    .from { Customers }
                    .where { Customers.email like "%@example.com".literal() }
                    .limit(10)
                    .str()
            actual shouldBe
                "SELECT * FROM customers " +
                    "WHERE customers.email LIKE '%@example.com' " +
                    "LIMIT 10"
          }

          "limit with order by clause" {
            val actual = select().from { Customers }.orderBy(Customers.fullName).limit(10).str()
            actual shouldBe "SELECT * FROM customers ORDER BY customers.full_name LIMIT 10"
          }

          // LIMIT with OFFSET
          "limit with offset" {
            val actual = select().from { Customers }.limit(10).offset(20).str()
            actual shouldBe "SELECT * FROM customers LIMIT 10 OFFSET 20"
          }

          // Complex queries with LIMIT
          "limit with group by, having, and order by" {
            val actual =
                select(Orders.status, count() `as` "order_count")
                    .from { Orders }
                    .groupBy(Orders.status)
                    .having { count() gt 5 }
                    .orderBy(count().desc())
                    .limit(10)
                    .str()
            actual shouldBe
                "SELECT sales.orders.status, COUNT(*) AS order_count " +
                    "FROM sales.orders " +
                    "GROUP BY sales.orders.status " +
                    "HAVING COUNT(*) > 5 " +
                    "ORDER BY COUNT(*) DESC " +
                    "LIMIT 10"
          }

          "complex query with all clauses including limit and offset" {
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

          // Pagination pattern
          "pagination with limit and offset" {
            val pageSize = 10
            val pageNumber = 3
            val offset = pageSize * (pageNumber - 1)

            val actual =
                select()
                    .from { Customers }
                    .orderBy(Customers.id)
                    .limit(pageSize)
                    .offset(offset)
                    .str()
            actual shouldBe "SELECT * FROM customers ORDER BY customers.id LIMIT 10 OFFSET 20"
          }
        },
    )
