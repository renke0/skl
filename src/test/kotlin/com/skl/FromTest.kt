package com.skl

import com.skl.SKL.select
import com.skl.fixtures.Addresses
import com.skl.fixtures.CustomerAddresses
import com.skl.fixtures.Customers
import com.skl.fixtures.Orders
import com.skl.fixtures.Reports
import com.skl.query.Functions.count
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class FromTest :
    StringSpec(
        {
          // Basic FROM tests
          "from without alias" {
            val actual = select().from { Customers }.str()
            actual shouldBe "SELECT * FROM customers"
          }

          "from with alias" {
            val actual = select().from { Customers `as` "cus" }.str()
            actual shouldBe "SELECT * FROM customers cus"
          }

          "from with schema qualified table" {
            val actual = select().from { Orders }.str()
            actual shouldBe "SELECT * FROM sales.orders"
          }

          "from with database and schema qualified table" {
            val actual = select().from { Reports }.str()
            actual shouldBe "SELECT * FROM analytics_db.public.reports"
          }

          // JOIN tests
          "from with inner join (join keyword)" {
            val actual =
                select()
                    .from { Customers }
                    .join { CustomerAddresses on { Customers.id eq CustomerAddresses.customerId } }
                    .str()
            actual shouldBe
                "SELECT * " +
                    "FROM customers " +
                    "JOIN customer_addresses ON customers.id = customer_addresses.customer_id"
          }

          "from with inner join (inner join keyword)" {
            val actual =
                select()
                    .from { Customers }
                    .innerJoin {
                      CustomerAddresses on { Customers.id eq CustomerAddresses.customerId }
                    }
                    .str()
            actual shouldBe
                "SELECT * " +
                    "FROM customers " +
                    "INNER JOIN customer_addresses ON customers.id = customer_addresses.customer_id"
          }

          "from with left join" {
            val actual =
                select()
                    .from { Customers }
                    .leftJoin {
                      CustomerAddresses on { Customers.id eq CustomerAddresses.customerId }
                    }
                    .str()
            actual shouldBe
                "SELECT * " +
                    "FROM customers " +
                    "LEFT JOIN customer_addresses ON customers.id = customer_addresses.customer_id"
          }

          "from with right join" {
            val actual =
                select()
                    .from { Customers }
                    .rightJoin {
                      CustomerAddresses on { Customers.id eq CustomerAddresses.customerId }
                    }
                    .str()
            actual shouldBe
                "SELECT * " +
                    "FROM customers " +
                    "RIGHT JOIN customer_addresses ON customers.id = customer_addresses.customer_id"
          }

          "from with full join" {
            val actual =
                select()
                    .from { Customers }
                    .fullJoin {
                      CustomerAddresses on { Customers.id eq CustomerAddresses.customerId }
                    }
                    .str()
            actual shouldBe
                "SELECT * " +
                    "FROM customers " +
                    "FULL JOIN customer_addresses ON customers.id = customer_addresses.customer_id"
          }

          "from with cross join" {
            val actual = select().from { Customers }.crossJoin { CustomerAddresses() }.str()
            actual shouldBe "SELECT * " + "FROM customers " + "CROSS JOIN customer_addresses"
          }

          // Multiple joins
          "from with multiple joins" {
            val actual =
                select()
                    .from { Customers }
                    .join { CustomerAddresses on { Customers.id eq CustomerAddresses.customerId } }
                    .join { Addresses on { Addresses.id eq CustomerAddresses.addressId } }
                    .str()
            actual shouldBe
                "SELECT * " +
                    "FROM customers " +
                    "JOIN customer_addresses ON customers.id = customer_addresses.customer_id " +
                    "JOIN addresses ON addresses.id = customer_addresses.address_id"
          }

          "from with different types of joins" {
            val actual =
                select()
                    .from { Customers }
                    .leftJoin {
                      CustomerAddresses on { Customers.id eq CustomerAddresses.customerId }
                    }
                    .rightJoin { Orders on { Customers.id eq Orders.customerId } }
                    .str()
            actual shouldBe
                "SELECT * " +
                    "FROM customers " +
                    "LEFT JOIN customer_addresses ON customers.id = customer_addresses.customer_id " +
                    "RIGHT JOIN sales.orders ON customers.id = sales.orders.customer_id"
          }

          // Joins with aliased tables
          "from with join using aliased tables" {
            val actual =
                select()
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
                "SELECT * " +
                    "FROM customers c " +
                    "JOIN customer_addresses ca ON c.id = ca.customer_id"
          }

          // Complex join conditions
          "from with join using complex condition (AND)" {
            val actual =
                select()
                    .from { Customers }
                    .join {
                      CustomerAddresses on
                          {
                            (Customers.id eq CustomerAddresses.customerId) and
                                (CustomerAddresses.isBilling eq true)
                          }
                    }
                    .str()
            actual shouldBe
                "SELECT * " +
                    "FROM customers " +
                    "JOIN customer_addresses ON customers.id = customer_addresses.customer_id " +
                    "AND customer_addresses.is_billing = TRUE"
          }

          "from with join using complex condition (OR)" {
            val actual =
                select()
                    .from { Customers }
                    .join {
                      CustomerAddresses on
                          {
                            (Customers.id eq CustomerAddresses.customerId) or
                                (Customers.email eq "test@example.com")
                          }
                    }
                    .str()
            actual shouldBe
                "SELECT * " +
                    "FROM customers " +
                    "JOIN customer_addresses ON customers.id = customer_addresses.customer_id " +
                    "OR customers.email = 'test@example.com'"
          }

          // Self-join
          "from with self-join" {
            val employees = Customers `as` "employees"
            val managers = Customers `as` "managers"

            val actual =
                select(
                        employees[Customers.fullName],
                        managers[Customers.fullName] `as` "manager_name",
                    )
                    .from { employees }
                    .leftJoin { managers on { employees[Customers.id] eq managers[Customers.id] } }

            actual.str() shouldBe
                "SELECT employees.full_name, managers.full_name AS manager_name " +
                    "FROM customers employees " +
                    "LEFT JOIN customers managers ON employees.id = managers.id"
          }

          // Complex query with multiple joins and conditions
          "complex query with multiple joins and conditions" {
            val actual =
                select(Customers.fullName, count(Orders.id) `as` "order_count")
                    .from { Customers }
                    .leftJoin {
                      CustomerAddresses on { Customers.id eq CustomerAddresses.customerId }
                    }
                    .leftJoin {
                      Orders on
                          {
                            (Customers.id eq Orders.customerId) and (Orders.status eq "COMPLETED")
                          }
                    }
                    .str()
            actual shouldBe
                "SELECT customers.full_name, COUNT(sales.orders.id) AS order_count " +
                    "FROM customers " +
                    "LEFT JOIN customer_addresses ON customers.id = customer_addresses.customer_id " +
                    "LEFT JOIN sales.orders ON customers.id = sales.orders.customer_id " +
                    "AND sales.orders.status = 'COMPLETED'"
          }
        },
    )
