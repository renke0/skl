package com.skl

import com.skl.SKL.select
import com.skl.fixtures.Customers
import com.skl.fixtures.Orders
import com.skl.query.Parameter
import com.skl.query.literal
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class WhereTest :
    StringSpec(
        {
          // Basic WHERE tests
          "where with simple equality" {
            val actual =
                select().from { Customers }.where { Customers.email eq "test@example.com" }.str()
            actual shouldBe "SELECT * FROM customers WHERE customers.email = 'test@example.com'"
          }

          "where with simple inequality" {
            val actual =
                select().from { Customers }.where { Customers.email ne "test@example.com" }.str()
            actual shouldBe "SELECT * FROM customers WHERE customers.email <> 'test@example.com'"
          }

          "where with less than" {
            val actual = select().from { Orders }.where { Orders.totalAmount lt 100.0 }.str()
            actual shouldBe "SELECT * FROM sales.orders WHERE sales.orders.total_amount < 100.0"
          }

          "where with less than or equal" {
            val actual = select().from { Orders }.where { Orders.totalAmount le 100.0 }.str()
            actual shouldBe "SELECT * FROM sales.orders WHERE sales.orders.total_amount <= 100.0"
          }

          "where with greater than" {
            val actual = select().from { Orders }.where { Orders.totalAmount gt 100.0 }.str()
            actual shouldBe "SELECT * FROM sales.orders WHERE sales.orders.total_amount > 100.0"
          }

          "where with greater than or equal" {
            val actual = select().from { Orders }.where { Orders.totalAmount ge 100.0 }.str()
            actual shouldBe "SELECT * FROM sales.orders WHERE sales.orders.total_amount >= 100.0"
          }

          // Logical operators
          "where with AND" {
            val actual =
                select()
                    .from { Customers }
                    .where {
                      (Customers.email eq "test@example.com") and
                          (Customers.fullName eq "Test User")
                    }
                    .str()
            actual shouldBe
                "SELECT * " +
                    "FROM customers " +
                    "WHERE customers.email = 'test@example.com' AND customers.full_name = 'Test User'"
          }

          "where with OR" {
            val actual =
                select()
                    .from { Customers }
                    .where {
                      (Customers.email eq "test@example.com") or
                          (Customers.email eq "other@example.com")
                    }
                    .str()
            actual shouldBe
                "SELECT * " +
                    "FROM customers " +
                    "WHERE customers.email = 'test@example.com' OR customers.email = 'other@example.com'"
          }

          "where with complex condition (AND and OR)" {
            val actual =
                select()
                    .from { Customers }
                    .where {
                      ((Customers.email eq "test@example.com") and
                          (Customers.fullName eq "Test User")) or
                          ((Customers.email eq "other@example.com") and
                              (Customers.fullName eq "Other User"))
                    }
                    .str()
            actual shouldBe
                "SELECT * " +
                    "FROM customers " +
                    "WHERE customers.email = 'test@example.com' " +
                    "AND customers.full_name = 'Test User' " +
                    "OR customers.email = 'other@example.com' " +
                    "AND customers.full_name = 'Other User'"
          }

          "where with grouped condition" {
            val actual =
                select()
                    .from { Customers }
                    .where {
                      (Customers.email eq "test@example.com") and
                          ((Customers.fullName eq "Test User") or
                                  (Customers.fullName eq "Other User"))
                              .group()
                    }
                    .str()
            actual shouldBe
                "SELECT * " +
                    "FROM customers " +
                    "WHERE customers.email = 'test@example.com' " +
                    "AND (customers.full_name = 'Test User' OR customers.full_name = 'Other User')"
          }

          // NULL checks
          "where with IS NULL" {
            val actual = select().from { Customers }.where { Customers.phone.isNull() }.str()
            actual shouldBe "SELECT * FROM customers WHERE customers.phone IS NULL"
          }

          "where with IS NOT NULL" {
            val actual = select().from { Customers }.where { Customers.phone.isNotNull() }.str()
            actual shouldBe "SELECT * FROM customers WHERE customers.phone IS NOT NULL"
          }

          // IN operator
          "where with IN" {
            val actual =
                select()
                    .from { Customers }
                    .where {
                      Customers.email `in`
                          listOf("test@example.com".literal(), "other@example.com".literal())
                    }
                    .str()
            actual shouldBe
                "SELECT * FROM customers WHERE customers.email IN ('test@example.com', 'other@example.com')"
          }

          // LIKE operator
          "where with LIKE" {
            val actual =
                select()
                    .from { Customers }
                    .where { Customers.email like "%@example.com".literal() }
                    .str()
            actual shouldBe "SELECT * FROM customers WHERE customers.email LIKE '%@example.com'"
          }

          // BETWEEN operator
          "where with BETWEEN" {
            val actual =
                select()
                    .from { Orders }
                    .where { Orders.totalAmount.between(50.0.literal() and 100.0.literal()) }
                    .str()
            actual shouldBe
                "SELECT * FROM sales.orders WHERE sales.orders.total_amount BETWEEN 50.0 AND 100.0"
          }

          // Parameter tests
          "where with parameter" {
            val actual =
                select().from { Customers }.where { Customers.email eq Parameter("email") }.str()
            actual shouldBe "SELECT * FROM customers WHERE customers.email = @email"
          }

          // WHERE with aliased columns
          "where with aliased column" {
            val emailAlias = Customers.email `as` "email_address"
            val actual =
                select(emailAlias)
                    .from { Customers }
                    .where { emailAlias eq "test@example.com" }
                    .str()
            actual shouldBe
                "SELECT customers.email AS email_address " +
                    "FROM customers " +
                    "WHERE email_address = 'test@example.com'"
          }

          "where with multiple aliased columns" {
            val emailAlias = Customers.email `as` "email_address"
            val nameAlias = Customers.fullName `as` "name"
            val actual =
                select(emailAlias, nameAlias)
                    .from { Customers }
                    .where { (emailAlias eq "test@example.com") and (nameAlias eq "Test User") }
                    .str()
            actual shouldBe
                "SELECT customers.email AS email_address, customers.full_name AS name " +
                    "FROM customers " +
                    "WHERE email_address = 'test@example.com' AND name = 'Test User'"
          }

          "where with aliased column and IN operator" {
            val emailAlias = Customers.email `as` "email_address"
            val actual =
                select(emailAlias)
                    .from { Customers }
                    .where {
                      emailAlias `in`
                          listOf("test@example.com".literal(), "other@example.com".literal())
                    }
                    .str()
            actual shouldBe
                "SELECT customers.email AS email_address " +
                    "FROM customers " +
                    "WHERE email_address IN ('test@example.com', 'other@example.com')"
          }

          "where with aliased column and LIKE operator" {
            val emailAlias = Customers.email `as` "email_address"
            val actual =
                select(emailAlias)
                    .from { Customers }
                    .where { emailAlias like "%@example.com".literal() }
                    .str()
            actual shouldBe
                "SELECT customers.email AS email_address " +
                    "FROM customers " +
                    "WHERE email_address LIKE '%@example.com'"
          }

          // Complex WHERE with multiple conditions
          "complex where with multiple conditions" {
            val actual =
                select()
                    .from { Orders }
                    .where {
                      (Orders.totalAmount gt 100.0) and
                          (Orders.status eq "COMPLETED") and
                          (Orders.orderDate.between(
                              "2023-01-01".literal() and "2023-12-31".literal()))
                    }
                    .str()
            actual shouldBe
                "SELECT * " +
                    "FROM sales.orders " +
                    "WHERE sales.orders.total_amount > 100.0 " +
                    "AND sales.orders.status = 'COMPLETED' " +
                    "AND sales.orders.order_date BETWEEN '2023-01-01' AND '2023-12-31'"
          }
        },
    )
