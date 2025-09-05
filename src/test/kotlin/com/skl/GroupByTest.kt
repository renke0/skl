package com.skl

import com.skl.SKL.alias
import com.skl.SKL.select
import com.skl.fixtures.Customers
import com.skl.fixtures.Orders
import com.skl.query.Functions.avg
import com.skl.query.Functions.count
import com.skl.query.Functions.max
import com.skl.query.Functions.min
import com.skl.query.Functions.sum
import com.skl.query.literal
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class GroupByTest :
    StringSpec(
        {
          // Basic GROUP BY tests
          "group by single column" {
            val actual =
                select(Customers.fullName, count())
                    .from { Customers }
                    .groupBy(Customers.fullName)
                    .str()
            actual shouldBe
                "SELECT customers.full_name, COUNT(*) FROM customers GROUP BY customers.full_name"
          }

          "group by multiple columns" {
            val actual =
                select(Customers.fullName, Customers.email, count())
                    .from { Customers }
                    .groupBy(Customers.fullName, Customers.email)
                    .str()
            actual shouldBe
                "SELECT customers.full_name, customers.email, COUNT(*) " +
                    "FROM customers " +
                    "GROUP BY customers.full_name, customers.email"
          }

          // GROUP BY with aggregate functions
          "group by with multiple aggregate functions" {
            val actual =
                select(
                        Orders.status,
                        count() `as` "order_count",
                        sum(Orders.totalAmount) `as` "total_amount",
                        avg(Orders.totalAmount) `as` "avg_amount",
                        min(Orders.totalAmount) `as` "min_amount",
                        max(Orders.totalAmount) `as` "max_amount",
                    )
                    .from { Orders }
                    .groupBy(Orders.status)
                    .str()
            actual shouldBe
                "SELECT sales.orders.status, COUNT(*) AS order_count, " +
                    "SUM(sales.orders.total_amount) AS total_amount, " +
                    "AVG(sales.orders.total_amount) AS avg_amount, " +
                    "MIN(sales.orders.total_amount) AS min_amount, " +
                    "MAX(sales.orders.total_amount) AS max_amount " +
                    "FROM sales.orders GROUP BY sales.orders.status"
          }

          // GROUP BY with aliased columns
          "group by with aliased columns" {
            val actual =
                select(Customers.fullName `as` "name", count() `as` "customer_count")
                    .from { Customers }
                    .groupBy(Customers.fullName)
                    .str()
            actual shouldBe
                "SELECT customers.full_name AS name, COUNT(*) AS customer_count " +
                    "FROM customers " +
                    "GROUP BY customers.full_name"
          }

          "group by with alias reference" {
            val nameAlias = Customers.fullName `as` "name"
            val actual =
                select(nameAlias, count() `as` "customer_count")
                    .from { Customers }
                    .groupBy(nameAlias)
                    .str()
            actual shouldBe
                "SELECT customers.full_name AS name, COUNT(*) AS customer_count " +
                    "FROM customers " +
                    "GROUP BY name"
          }

          "group by with multiple aliased columns" {
            val name = Customers.fullName `as` "name"
            val email = Customers.email `as` "email_address"
            val actual =
                select(name, email, count() `as` "customer_count")
                    .from { Customers }
                    .groupBy(name, email)
                    .str()
            actual shouldBe
                "SELECT customers.full_name AS name, customers.email AS email_address, COUNT(*) AS customer_count " +
                    "FROM customers " +
                    "GROUP BY name, email_address"
          }

          "group by with aliased columns and having clause" {
            val actual =
                select(Customers.fullName `as` "name", count() `as` "customer_count")
                    .from { Customers }
                    .groupBy(Customers.fullName)
                    .having { alias("customer_count") gt 5 }
                    .str()
            actual shouldBe
                "SELECT customers.full_name AS name, COUNT(*) AS customer_count " +
                    "FROM customers " +
                    "GROUP BY customers.full_name " +
                    "HAVING customer_count > 5"
          }

          // GROUP BY with WHERE clause
          "group by with where clause" {
            val actual =
                select(Orders.status, count() `as` "order_count")
                    .from { Orders }
                    .where { Orders.totalAmount gt 100.0 }
                    .groupBy(Orders.status)
                    .str()
            actual shouldBe
                "SELECT sales.orders.status, COUNT(*) AS order_count " +
                    "FROM sales.orders " +
                    "WHERE sales.orders.total_amount > 100.0 " +
                    "GROUP BY sales.orders.status"
          }

          // GROUP BY with HAVING clause
          "group by with having clause" {
            val actual =
                select(Orders.status, count() `as` "order_count")
                    .from { Orders }
                    .groupBy(Orders.status)
                    .having { count() gt 5 }
                    .str()
            actual shouldBe
                "SELECT sales.orders.status, COUNT(*) AS order_count " +
                    "FROM sales.orders " +
                    "GROUP BY sales.orders.status " +
                    "HAVING COUNT(*) > 5"
          }

          // GROUP BY with ORDER BY clause
          "group by with order by clause" {
            val actual =
                select(Orders.status, count() `as` "order_count")
                    .from { Orders }
                    .groupBy(Orders.status)
                    .orderBy(count().desc())
                    .str()
            actual shouldBe
                "SELECT sales.orders.status, COUNT(*) AS order_count " +
                    "FROM sales.orders " +
                    "GROUP BY sales.orders.status " +
                    "ORDER BY COUNT(*) DESC"
          }

          // Complex GROUP BY with multiple clauses
          "complex group by with multiple clauses" {
            val actual =
                select(
                        Orders.status,
                        count() `as` "order_count",
                        sum(Orders.totalAmount) `as` "total_amount",
                    )
                    .from { Orders }
                    .where {
                      Orders.orderDate.between("2023-01-01".literal() and "2023-12-31".literal())
                    }
                    .groupBy(Orders.status)
                    .having { count() gt 5 }
                    .orderBy(count().desc())
                    .str()
            actual shouldBe
                "SELECT sales.orders.status, COUNT(*) AS order_count, SUM(sales.orders.total_amount) AS total_amount " +
                    "FROM sales.orders " +
                    "WHERE sales.orders.order_date BETWEEN '2023-01-01' AND '2023-12-31' " +
                    "GROUP BY sales.orders.status " +
                    "HAVING COUNT(*) > 5 " +
                    "ORDER BY COUNT(*) DESC"
          }
        },
    )
