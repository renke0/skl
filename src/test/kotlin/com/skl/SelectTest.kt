package com.skl

import com.skl.SKL.select
import com.skl.fixtures.CustomerAddresses
import com.skl.fixtures.Customers
import com.skl.fixtures.Orders
import com.skl.fixtures.Reports
import com.skl.query.Functions.avg
import com.skl.query.Functions.count
import com.skl.query.Functions.length
import com.skl.query.Functions.lower
import com.skl.query.Functions.max
import com.skl.query.Functions.min
import com.skl.query.Functions.sum
import com.skl.query.Functions.upper
import com.skl.query.NULL
import com.skl.query.Parameter
import com.skl.query.STAR
import com.skl.query.literal
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SelectTest :
    StringSpec(
        {
          // Basic select tests
          "select" {
            val actual = select().str()
            actual shouldBe "SELECT *"
          }

          "select star" {
            val actual = select(STAR).str()
            actual shouldBe "SELECT *"
          }

          "select column" {
            val actual = select(Customers.email).from { Customers }.str()
            actual shouldBe "SELECT customers.email FROM customers"
          }

          "select table with schema" {
            val actual = select(Orders.id).from { Orders }.str()
            actual shouldBe "SELECT sales.orders.id FROM sales.orders"
          }

          "select table with database and schema" {
            val actual = select(Reports.reportName).from { Reports }.str()
            actual shouldBe
                "SELECT analytics_db.public.reports.report_name " +
                    "FROM analytics_db.public.reports"
          }

          "select multiple columns" {
            val actual = select(Customers.email, Customers.fullName).from { Customers }.str()
            actual shouldBe "SELECT customers.email, customers.full_name FROM customers"
          }

          "select column from aliased table" {
            val actual = select(Customers.email).from { Customers `as` "cus" }.str()
            actual shouldBe "SELECT cus.email FROM customers cus"
          }

          "select multiple columns from aliased table" {
            val actual =
                select(Customers.email, Customers.fullName).from { Customers `as` "cus" }.str()
            actual shouldBe "SELECT cus.email, cus.full_name FROM customers cus"
          }

          "select column with alias" {
            val actual = select(Customers.email `as` "cus_email").from { Customers }.str()
            actual shouldBe "SELECT customers.email AS cus_email FROM customers"
          }

          "select multiple columns with alias" {
            val actual =
                select(Customers.email `as` "cus_email", Customers.fullName `as` "cus_name")
                    .from { Customers }
                    .str()
            actual shouldBe
                "SELECT customers.email AS cus_email, customers.full_name AS cus_name FROM customers"
          }

          "select column with alias from aliased table" {
            val actual =
                select(Customers.email `as` "cus_email").from { Customers `as` "cus" }.str()
            actual shouldBe "SELECT cus.email AS cus_email FROM customers cus"
          }

          "select all columns in a table with from" {
            val actual = select(Customers).from { Customers }.str()
            actual shouldBe "SELECT customers.* FROM customers"
          }

          "test select all columns in multiple tables" {
            val actual =
                select(Customers, CustomerAddresses)
                    .from { Customers }
                    .join { CustomerAddresses on { Customers.id eq CustomerAddresses.customerId } }
                    .str()
            actual shouldBe
                "SELECT customers.*, customer_addresses.* " +
                    "FROM customers " +
                    "JOIN customer_addresses ON customers.id = customer_addresses.customer_id"
          }

          "select all columns in multiple aliased tables" {
            val actual =
                select(Customers, CustomerAddresses)
                    .from { Customers `as` "c" }
                    .join {
                      CustomerAddresses `as`
                          "ca" on
                          {
                            Customers.id eq CustomerAddresses.customerId
                          }
                    }
                    .str()
            actual shouldBe
                "SELECT c.*, ca.* " +
                    "FROM customers c " +
                    "JOIN customer_addresses ca ON c.id = ca.customer_id"
          }

          "select multiple columns from multiple aliased tables" {
            val actual =
                select(Customers.email, CustomerAddresses.isBilling)
                    .from { Customers `as` "c" }
                    .join {
                      CustomerAddresses `as`
                          "ca" on
                          {
                            Customers.id eq CustomerAddresses.customerId
                          }
                    }
                    .str()
            actual shouldBe
                "SELECT c.email, ca.is_billing " +
                    "FROM customers c " +
                    "JOIN customer_addresses ca ON c.id = ca.customer_id"
          }

          // Function tests
          "select with count(*)" {
            val actual = select(count()).from { Customers }.str()
            actual shouldBe "SELECT COUNT(*) FROM customers"
          }

          "select with count(column)" {
            val actual = select(count(Customers.email)).from { Customers }.str()
            actual shouldBe "SELECT COUNT(customers.email) FROM customers"
          }

          "select with sum" {
            val actual = select(sum(Orders.totalAmount)).from { Orders }.str()
            actual shouldBe "SELECT SUM(sales.orders.total_amount) FROM sales.orders"
          }

          "select with avg" {
            val actual = select(avg(Orders.totalAmount)).from { Orders }.str()
            actual shouldBe "SELECT AVG(sales.orders.total_amount) FROM sales.orders"
          }

          "select with min" {
            val actual = select(min(Orders.totalAmount)).from { Orders }.str()
            actual shouldBe "SELECT MIN(sales.orders.total_amount) FROM sales.orders"
          }

          "select with max" {
            val actual = select(max(Orders.totalAmount)).from { Orders }.str()
            actual shouldBe "SELECT MAX(sales.orders.total_amount) FROM sales.orders"
          }

          "select with lower" {
            val actual = select(lower(Customers.email)).from { Customers }.str()
            actual shouldBe "SELECT LOWER(customers.email) FROM customers"
          }

          "select with upper" {
            val actual = select(upper(Customers.email.term())).from { Customers }.str()
            actual shouldBe "SELECT UPPER(customers.email) FROM customers"
          }

          "select with length" {
            val actual = select(length(Customers.email.term())).from { Customers }.str()
            actual shouldBe "SELECT LENGTH(customers.email) FROM customers"
          }

          "select with multiple functions" {
            val actual = select(count(), avg(Orders.totalAmount)).from { Orders }.str()
            actual shouldBe "SELECT COUNT(*), AVG(sales.orders.total_amount) FROM sales.orders"
          }

          "select with function and alias" {
            val actual = select(count() `as` "total_customers").from { Customers }.str()
            actual shouldBe "SELECT COUNT(*) AS total_customers FROM customers"
          }

          // Literal tests
          "select with string literal" {
            val actual = select("Hello".literal()).from { Customers }.str()
            actual shouldBe "SELECT 'Hello' FROM customers"
          }

          "select with number literal" {
            val actual = select(42.literal()).from { Customers }.str()
            actual shouldBe "SELECT 42 FROM customers"
          }

          "select with boolean literal" {
            val actual = select(true.literal()).from { Customers }.str()
            actual shouldBe "SELECT TRUE FROM customers"
          }

          "select with null literal" {
            val actual = select(NULL).from { Customers }.str()
            actual shouldBe "SELECT NULL FROM customers"
          }

          // Parameter tests
          // TODO Enable when figure out config
          "!select with unnamed parameter" {
            val actual = select(Parameter()).from { Customers }.str()
            actual shouldBe "SELECT ? FROM customers"
          }

          "select with named parameter" {
            val actual = select(Parameter("email")).from { Customers }.str()
            actual shouldBe "SELECT @email FROM customers"
          }

          // Complex select tests
          "select with mix of columns, functions, and literals" {
            val actual =
                select(Customers.id, count() `as` "order_count", "Active".literal() `as` "status")
                    .from { Customers }
                    .str()
            actual shouldBe
                "SELECT customers.id, COUNT(*) AS order_count, 'Active' AS status FROM customers"
          }

          "select with multiple tables, joins, and functions" {
            val actual =
                select(
                        Customers.fullName,
                        count(Orders.id) `as` "order_count",
                        sum(Orders.totalAmount) `as` "total_spent")
                    .from { Customers }
                    .join { Orders on { Customers.id eq Orders.customerId } }
                    .str()
            actual shouldBe
                "SELECT customers.full_name, COUNT(sales.orders.id) AS order_count, " +
                    "SUM(sales.orders.total_amount) AS total_spent " +
                    "FROM customers " +
                    "JOIN sales.orders ON customers.id = sales.orders.customer_id"
          }
        },
    )
