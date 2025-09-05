package com.skl

import com.skl.SKL.select
import com.skl.fixtures.Orders
import com.skl.query.Functions.avg
import com.skl.query.Functions.count
import com.skl.query.Functions.max
import com.skl.query.Functions.sum
import com.skl.query.literal
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class HavingTest :
    StringSpec(
        {
          // Basic HAVING tests
          "having with simple comparison" {
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

          "having with equality" {
            val actual =
                select(Orders.status, count() `as` "order_count")
                    .from { Orders }
                    .groupBy(Orders.status)
                    .having { count() eq 10 }
                    .str()
            actual shouldBe
                "SELECT sales.orders.status, COUNT(*) AS order_count " +
                    "FROM sales.orders " +
                    "GROUP BY sales.orders.status " +
                    "HAVING COUNT(*) = 10"
          }

          "having with inequality" {
            val actual =
                select(Orders.status, count() `as` "order_count")
                    .from { Orders }
                    .groupBy(Orders.status)
                    .having { count() ne 0 }
                    .str()
            actual shouldBe
                "SELECT sales.orders.status, COUNT(*) AS order_count " +
                    "FROM sales.orders " +
                    "GROUP BY sales.orders.status " +
                    "HAVING COUNT(*) <> 0"
          }

          "having with less than" {
            val actual =
                select(Orders.status, avg(Orders.totalAmount) `as` "avg_amount")
                    .from { Orders }
                    .groupBy(Orders.status)
                    .having { avg(Orders.totalAmount) lt 100.0 }
                    .str()
            actual shouldBe
                "SELECT sales.orders.status, AVG(sales.orders.total_amount) AS avg_amount " +
                    "FROM sales.orders " +
                    "GROUP BY sales.orders.status " +
                    "HAVING AVG(sales.orders.total_amount) < 100.0"
          }

          "having with less than or equal" {
            val actual =
                select(Orders.status, avg(Orders.totalAmount) `as` "avg_amount")
                    .from { Orders }
                    .groupBy(Orders.status)
                    .having { avg(Orders.totalAmount) le 100.0 }
                    .str()
            actual shouldBe
                "SELECT sales.orders.status, AVG(sales.orders.total_amount) AS avg_amount " +
                    "FROM sales.orders " +
                    "GROUP BY sales.orders.status " +
                    "HAVING AVG(sales.orders.total_amount) <= 100.0"
          }

          "having with greater than" {
            val actual =
                select(Orders.status, avg(Orders.totalAmount) `as` "avg_amount")
                    .from { Orders }
                    .groupBy(Orders.status)
                    .having { avg(Orders.totalAmount) gt 100.0 }
                    .str()
            actual shouldBe
                "SELECT sales.orders.status, AVG(sales.orders.total_amount) AS avg_amount " +
                    "FROM sales.orders " +
                    "GROUP BY sales.orders.status " +
                    "HAVING AVG(sales.orders.total_amount) > 100.0"
          }

          "having with greater than or equal" {
            val actual =
                select(Orders.status, avg(Orders.totalAmount) `as` "avg_amount")
                    .from { Orders }
                    .groupBy(Orders.status)
                    .having { avg(Orders.totalAmount) ge 100.0 }
                    .str()
            actual shouldBe
                "SELECT sales.orders.status, AVG(sales.orders.total_amount) AS avg_amount " +
                    "FROM sales.orders " +
                    "GROUP BY sales.orders.status " +
                    "HAVING AVG(sales.orders.total_amount) >= 100.0"
          }

          // Logical operators
          "having with AND" {
            val actual =
                select(
                        Orders.status,
                        count() `as` "order_count",
                        sum(Orders.totalAmount) `as` "total_amount")
                    .from { Orders }
                    .groupBy(Orders.status)
                    .having { (count() gt 5) and (sum(Orders.totalAmount) gt 1000.0) }
                    .str()
            actual shouldBe
                "SELECT sales.orders.status, COUNT(*) AS order_count, SUM(sales.orders.total_amount) AS total_amount " +
                    "FROM sales.orders " +
                    "GROUP BY sales.orders.status " +
                    "HAVING COUNT(*) > 5 AND SUM(sales.orders.total_amount) > 1000.0"
          }

          "having with OR" {
            val actual =
                select(
                        Orders.status,
                        count() `as` "order_count",
                        sum(Orders.totalAmount) `as` "total_amount")
                    .from { Orders }
                    .groupBy(Orders.status)
                    .having { (count() gt 10) or (sum(Orders.totalAmount) gt 5000.0) }
                    .str()
            actual shouldBe
                "SELECT sales.orders.status, COUNT(*) AS order_count, SUM(sales.orders.total_amount) AS total_amount " +
                    "FROM sales.orders " +
                    "GROUP BY sales.orders.status " +
                    "HAVING COUNT(*) > 10 OR SUM(sales.orders.total_amount) > 5000.0"
          }

          "having with complex condition (AND and OR)" {
            val actual =
                select(
                        Orders.status,
                        count() `as` "order_count",
                        avg(Orders.totalAmount) `as` "avg_amount",
                        max(Orders.totalAmount) `as` "max_amount")
                    .from { Orders }
                    .groupBy(Orders.status)
                    .having {
                      ((count() gt 5) and (avg(Orders.totalAmount) gt 100.0)) or
                          ((count() gt 10) and (max(Orders.totalAmount) gt 500.0))
                    }
                    .str()
            actual shouldBe
                "SELECT sales.orders.status, COUNT(*) AS order_count, " +
                    "AVG(sales.orders.total_amount) AS avg_amount, " +
                    "MAX(sales.orders.total_amount) AS max_amount " +
                    "FROM sales.orders " +
                    "GROUP BY sales.orders.status " +
                    "HAVING COUNT(*) > 5 AND AVG(sales.orders.total_amount) > 100.0 OR " +
                    "COUNT(*) > 10 AND MAX(sales.orders.total_amount) > 500.0"
          }

          "having with grouped condition" {
            val actual =
                select(
                        Orders.status,
                        count() `as` "order_count",
                        sum(Orders.totalAmount) `as` "total_amount")
                    .from { Orders }
                    .groupBy(Orders.status)
                    .having {
                      (count() gt 5) and
                          ((sum(Orders.totalAmount) gt 1000.0) or
                                  (sum(Orders.totalAmount) lt 100.0))
                              .group()
                    }
                    .str()
            actual shouldBe
                "SELECT sales.orders.status, COUNT(*) AS order_count, SUM(sales.orders.total_amount) AS total_amount " +
                    "FROM sales.orders " +
                    "GROUP BY sales.orders.status " +
                    "HAVING COUNT(*) > 5 " +
                    "AND (SUM(sales.orders.total_amount) > 1000.0 OR SUM(sales.orders.total_amount) < 100.0)"
          }

          // HAVING with aliased columns
          "having with aliased aggregate function" {
            val countAlias = count() `as` "order_count"
            val actual =
                select(Orders.status, countAlias)
                    .from { Orders }
                    .groupBy(Orders.status)
                    .having { countAlias gt 5 }
                    .str()
            actual shouldBe
                "SELECT sales.orders.status, COUNT(*) AS order_count " +
                    "FROM sales.orders " +
                    "GROUP BY sales.orders.status " +
                    "HAVING order_count > 5"
          }

          "having with multiple aliased aggregate functions" {
            val countAlias = count() `as` "order_count"
            val sumAlias = sum(Orders.totalAmount) `as` "total_amount"
            val actual =
                select(Orders.status, countAlias, sumAlias)
                    .from { Orders }
                    .groupBy(Orders.status)
                    .having { (countAlias gt 5) and (sumAlias gt 1000.0) }
                    .str()
            actual shouldBe
                "SELECT sales.orders.status, COUNT(*) AS order_count, " +
                    "SUM(sales.orders.total_amount) AS total_amount " +
                    "FROM sales.orders " +
                    "GROUP BY sales.orders.status " +
                    "HAVING order_count > 5 AND total_amount > 1000.0"
          }

          "having with aliased aggregate functions and complex condition" {
            val countAlias = count() `as` "order_count"
            val avgAlias = avg(Orders.totalAmount) `as` "avg_amount"
            val maxAlias = max(Orders.totalAmount) `as` "max_amount"
            val actual =
                select(Orders.status, countAlias, avgAlias, maxAlias)
                    .from { Orders }
                    .groupBy(Orders.status)
                    .having {
                      ((countAlias gt 5) and (avgAlias gt 100.0)) or
                          ((countAlias gt 10) and (maxAlias gt 500.0))
                    }
                    .str()
            actual shouldBe
                "SELECT sales.orders.status, COUNT(*) AS order_count, " +
                    "AVG(sales.orders.total_amount) AS avg_amount, " +
                    "MAX(sales.orders.total_amount) AS max_amount " +
                    "FROM sales.orders " +
                    "GROUP BY sales.orders.status " +
                    "HAVING order_count > 5 AND avg_amount > 100.0 OR " +
                    "order_count > 10 AND max_amount > 500.0"
          }

          // HAVING with other clauses
          "having with where clause" {
            val actual =
                select(Orders.status, count() `as` "order_count")
                    .from { Orders }
                    .where {
                      Orders.orderDate.between("2023-01-01".literal() and "2023-12-31".literal())
                    }
                    .groupBy(Orders.status)
                    .having { count() gt 5 }
                    .str()
            actual shouldBe
                "SELECT sales.orders.status, COUNT(*) AS order_count " +
                    "FROM sales.orders " +
                    "WHERE sales.orders.order_date BETWEEN '2023-01-01' AND '2023-12-31' " +
                    "GROUP BY sales.orders.status " +
                    "HAVING COUNT(*) > 5"
          }

          "having with order by clause" {
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

          // Complex HAVING with multiple clauses
          "complex having with multiple clauses" {
            val actual =
                select(
                        Orders.status,
                        count() `as` "order_count",
                        sum(Orders.totalAmount) `as` "total_amount",
                        avg(Orders.totalAmount) `as` "avg_amount")
                    .from { Orders }
                    .where {
                      Orders.orderDate.between("2023-01-01".literal() and "2023-12-31".literal())
                    }
                    .groupBy(Orders.status)
                    .having {
                      (count() gt 5) and
                          (sum(Orders.totalAmount) gt 1000.0) and
                          (avg(Orders.totalAmount) gt 200.0)
                    }
                    .orderBy(sum(Orders.totalAmount).desc())
                    .str()
            actual shouldBe
                "SELECT sales.orders.status, COUNT(*) AS order_count, " +
                    "SUM(sales.orders.total_amount) AS total_amount, " +
                    "AVG(sales.orders.total_amount) AS avg_amount " +
                    "FROM sales.orders " +
                    "WHERE sales.orders.order_date BETWEEN '2023-01-01' AND '2023-12-31' " +
                    "GROUP BY sales.orders.status " +
                    "HAVING COUNT(*) > 5 " +
                    "AND SUM(sales.orders.total_amount) > 1000.0 " +
                    "AND AVG(sales.orders.total_amount) > 200.0 " +
                    "ORDER BY SUM(sales.orders.total_amount) DESC"
          }
        },
    )
