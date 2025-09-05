package com.skl

import com.skl.SKL.select
import com.skl.fixtures.Customers
import com.skl.fixtures.Orders
import com.skl.query.Functions.count
import com.skl.query.literal
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class OrderByTest :
    StringSpec(
        {
          // Basic ORDER BY tests
          "order by single column" {
            val actual = select().from { Customers }.orderBy(Customers.fullName).str()
            actual shouldBe "SELECT * FROM customers ORDER BY customers.full_name"
          }

          "order by multiple columns" {
            val actual =
                select().from { Customers }.orderBy(Customers.fullName, Customers.email).str()
            actual shouldBe "SELECT * FROM customers ORDER BY customers.full_name, customers.email"
          }

          // ORDER BY with direction
          "order by with ASC direction" {
            val actual = select().from { Customers }.orderBy(Customers.fullName.asc()).str()
            actual shouldBe "SELECT * FROM customers ORDER BY customers.full_name ASC"
          }

          "order by with DESC direction" {
            val actual = select().from { Customers }.orderBy(Customers.fullName.desc()).str()
            actual shouldBe "SELECT * FROM customers ORDER BY customers.full_name DESC"
          }

          "order by multiple columns with different directions" {
            val actual =
                select()
                    .from { Customers }
                    .orderBy(Customers.fullName.asc(), Customers.email.desc())
                    .str()
            actual shouldBe
                "SELECT * FROM customers ORDER BY customers.full_name ASC, customers.email DESC"
          }

          // ORDER BY with NULL ordering
          "order by with NULLS FIRST" {
            val actual = select().from { Customers }.orderBy(Customers.phone.nullsFirst()).str()
            actual shouldBe "SELECT * FROM customers ORDER BY customers.phone NULLS FIRST"
          }

          "order by with NULLS LAST" {
            val actual = select().from { Customers }.orderBy(Customers.phone.nullsLast()).str()
            actual shouldBe "SELECT * FROM customers ORDER BY customers.phone NULLS LAST"
          }

          "order by with direction and NULL ordering" {
            val actual =
                select().from { Customers }.orderBy(Customers.phone.desc().nullsFirst()).str()
            actual shouldBe "SELECT * FROM customers ORDER BY customers.phone DESC NULLS FIRST"
          }

          // ORDER BY with functions
          "order by with function" {
            val actual =
                select(Customers.fullName, count() `as` "customer_count")
                    .from { Customers }
                    .groupBy(Customers.fullName)
                    .orderBy(count())
                    .str()
            actual shouldBe
                "SELECT customers.full_name, COUNT(*) AS customer_count " +
                    "FROM customers " +
                    "GROUP BY customers.full_name " +
                    "ORDER BY COUNT(*)"
          }

          "order by with function and direction" {
            val actual =
                select(Customers.fullName, count() `as` "customer_count")
                    .from { Customers }
                    .groupBy(Customers.fullName)
                    .orderBy(count().desc())
                    .str()
            actual shouldBe
                "SELECT customers.full_name, COUNT(*) AS customer_count " +
                    "FROM customers " +
                    "GROUP BY customers.full_name " +
                    "ORDER BY COUNT(*) DESC"
          }

          // ORDER BY with aliased columns
          "order by with aliased column" {
            val actual =
                select(Customers.fullName `as` "name")
                    .from { Customers }
                    .orderBy(Customers.fullName)
                    .str()
            actual shouldBe
                "SELECT customers.full_name AS name FROM customers ORDER BY customers.full_name"
          }

          "order by with alias reference" {
            val nameAlias = Customers.fullName `as` "name"
            val actual = select(nameAlias).from { Customers }.orderBy(nameAlias).str()
            actual shouldBe "SELECT customers.full_name AS name FROM customers ORDER BY name"
          }

          "order by with multiple aliased columns" {
            val nameAlias = Customers.fullName `as` "name"
            val emailAlias = Customers.email `as` "email_address"
            val actual =
                select(nameAlias, emailAlias)
                    .from { Customers }
                    .orderBy(nameAlias.asc(), emailAlias.desc())
                    .str()
            actual shouldBe
                "SELECT customers.full_name AS name, customers.email AS email_address " +
                    "FROM customers ORDER BY name ASC, email_address DESC"
          }

          "order by with aliased function" {
            val countAlias = count() `as` "customer_count"
            val actual =
                select(Customers.fullName, countAlias)
                    .from { Customers }
                    .groupBy(Customers.fullName)
                    .orderBy(countAlias.desc())
                    .str()
            actual shouldBe
                "SELECT customers.full_name, COUNT(*) AS customer_count " +
                    "FROM customers " +
                    "GROUP BY customers.full_name " +
                    "ORDER BY customer_count DESC"
          }

          // ORDER BY with WHERE clause
          "order by with where clause" {
            val actual =
                select()
                    .from { Orders }
                    .where { Orders.totalAmount gt 100.0 }
                    .orderBy(Orders.orderDate.desc())
                    .str()
            actual shouldBe
                "SELECT * FROM sales.orders " +
                    "WHERE sales.orders.total_amount > 100.0 " +
                    "ORDER BY sales.orders.order_date DESC"
          }

          // ORDER BY with GROUP BY and HAVING clauses
          "order by with group by and having clauses" {
            val actual =
                select(Orders.status, count() `as` "order_count")
                    .from { Orders }
                    .groupBy(Orders.status)
                    .having { count() gt 5 }
                    .orderBy(count().desc())
                    .str()
            actual shouldBe
                "SELECT sales.orders.status, COUNT(*) AS order_count " +
                    "FROM sales.orders " +
                    "GROUP BY sales.orders.status " +
                    "HAVING COUNT(*) > 5 " +
                    "ORDER BY COUNT(*) DESC"
          }

          // Complex ORDER BY with multiple clauses
          "complex order by with multiple clauses" {
            val actual =
                select(Orders.status, count() `as` "order_count", Orders.totalAmount `as` "total")
                    .from { Orders }
                    .where {
                      Orders.orderDate between ("2023-01-01".literal() and "2023-12-31".literal())
                    }
                    .groupBy(Orders.status, Orders.totalAmount)
                    .having { count() gt 5 }
                    .orderBy(
                        Orders.status.asc(),
                        count().desc(),
                        Orders.totalAmount.desc().nullsLast(),
                    )
                    .str()
            actual shouldBe
                "SELECT sales.orders.status, COUNT(*) AS order_count, sales.orders.total_amount AS total " +
                    "FROM sales.orders " +
                    "WHERE sales.orders.order_date BETWEEN '2023-01-01' AND '2023-12-31' " +
                    "GROUP BY sales.orders.status, sales.orders.total_amount " +
                    "HAVING COUNT(*) > 5 " +
                    "ORDER BY sales.orders.status ASC, COUNT(*) DESC, sales.orders.total_amount DESC NULLS LAST"
          }
        },
    )
