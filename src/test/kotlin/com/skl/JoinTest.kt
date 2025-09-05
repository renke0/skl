package com.skl

import com.skl.SKL.select
import com.skl.fixtures.Addresses
import com.skl.fixtures.CustomerAddresses
import com.skl.fixtures.Customers
import com.skl.fixtures.Orders
import com.skl.query.Functions.count
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class JoinTest :
    StringSpec(
        {
          // Basic JOIN tests
          "inner join with JOIN keyword" {
            val actual =
                select()
                    .from { Customers }
                    .join { CustomerAddresses on { Customers.id eq CustomerAddresses.customerId } }
                    .str()
            actual shouldBe
                "SELECT * FROM customers " +
                    "JOIN customer_addresses ON customers.id = customer_addresses.customer_id"
          }

          "inner join with INNER JOIN keyword" {
            val actual =
                select()
                    .from { Customers }
                    .innerJoin {
                      CustomerAddresses on { Customers.id eq CustomerAddresses.customerId }
                    }
                    .str()
            actual shouldBe
                "SELECT * FROM customers " +
                    "INNER JOIN customer_addresses ON customers.id = customer_addresses.customer_id"
          }

          "left join" {
            val actual =
                select()
                    .from { Customers }
                    .leftJoin {
                      CustomerAddresses on { Customers.id eq CustomerAddresses.customerId }
                    }
                    .str()
            actual shouldBe
                "SELECT * FROM customers " +
                    "LEFT JOIN customer_addresses ON customers.id = customer_addresses.customer_id"
          }

          "right join" {
            val actual =
                select()
                    .from { Customers }
                    .rightJoin {
                      CustomerAddresses on { Customers.id eq CustomerAddresses.customerId }
                    }
                    .str()
            actual shouldBe
                "SELECT * FROM customers " +
                    "RIGHT JOIN customer_addresses ON customers.id = customer_addresses.customer_id"
          }

          "full join" {
            val actual =
                select()
                    .from { Customers }
                    .fullJoin {
                      CustomerAddresses on { Customers.id eq CustomerAddresses.customerId }
                    }
                    .str()
            actual shouldBe
                "SELECT * FROM customers " +
                    "FULL JOIN customer_addresses ON customers.id = customer_addresses.customer_id"
          }

          "cross join" {
            val actual = select().from { Customers }.crossJoin { CustomerAddresses() }.str()
            actual shouldBe "SELECT * FROM customers " + "CROSS JOIN customer_addresses"
          }

          // JOIN with aliased tables
          "join with aliased tables" {
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
                "SELECT * FROM customers c " + "JOIN customer_addresses ca ON c.id = ca.customer_id"
          }

          // Multiple JOINs
          "multiple joins" {
            val actual =
                select()
                    .from { Customers }
                    .join { CustomerAddresses on { Customers.id eq CustomerAddresses.customerId } }
                    .join { Addresses on { Addresses.id eq CustomerAddresses.addressId } }
                    .str()
            actual shouldBe
                "SELECT * FROM customers " +
                    "JOIN customer_addresses ON customers.id = customer_addresses.customer_id " +
                    "JOIN addresses ON addresses.id = customer_addresses.address_id"
          }

          "different types of joins" {
            val actual =
                select()
                    .from { Customers }
                    .leftJoin {
                      CustomerAddresses on { Customers.id eq CustomerAddresses.customerId }
                    }
                    .rightJoin { Orders on { Customers.id eq Orders.customerId } }
                    .str()
            actual shouldBe
                "SELECT * FROM customers " +
                    "LEFT JOIN customer_addresses ON customers.id = customer_addresses.customer_id " +
                    "RIGHT JOIN sales.orders ON customers.id = sales.orders.customer_id"
          }

          // Complex JOIN conditions
          "join with complex condition (AND)" {
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
                "SELECT * FROM customers " +
                    "JOIN customer_addresses ON customers.id = customer_addresses.customer_id " +
                    "AND customer_addresses.is_billing = TRUE"
          }

          "join with complex condition (OR)" {
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
                "SELECT * FROM customers " +
                    "JOIN customer_addresses ON customers.id = customer_addresses.customer_id " +
                    "OR customers.email = 'test@example.com'"
          }

          // JOIN with aliased columns
          "join with aliased columns" {
            val customerId = Customers.id `as` "customer_id"
            val addressId = CustomerAddresses.addressId `as` "address_id"

            val actual =
                select(customerId, addressId)
                    .from { Customers }
                    .join { CustomerAddresses on { customerId eq CustomerAddresses.customerId } }
                    .str()

            actual shouldBe
                "SELECT customers.id AS customer_id, customer_addresses.address_id AS address_id " +
                    "FROM customers " +
                    "JOIN customer_addresses ON customer_id = customer_addresses.customer_id"
          }

          "join with aliased columns from both tables" {
            val customerId = Customers.id `as` "cust_id"
            val customerAddressId = CustomerAddresses.customerId `as` "cust_addr_id"

            val actual =
                select(customerId, customerAddressId)
                    .from { Customers }
                    .join { CustomerAddresses on { customerId eq customerAddressId } }
                    .str()

            actual shouldBe
                "SELECT customers.id AS cust_id, customer_addresses.customer_id AS cust_addr_id " +
                    "FROM customers " +
                    "JOIN customer_addresses ON cust_id = cust_addr_id"
          }

          "join with aliased columns and complex condition" {
            val customerId = Customers.id `as` "cust_id"
            val customerAddressId = CustomerAddresses.customerId `as` "cust_addr_id"
            val isBilling = CustomerAddresses.isBilling `as` "is_billing"

            val actual =
                select(customerId, customerAddressId, isBilling)
                    .from { Customers }
                    .join {
                      CustomerAddresses on
                          {
                            (customerId eq customerAddressId) and (isBilling eq true)
                          }
                    }
                    .str()

            actual shouldBe
                "SELECT customers.id AS cust_id, customer_addresses.customer_id AS cust_addr_id, " +
                    "customer_addresses.is_billing AS is_billing " +
                    "FROM customers " +
                    "JOIN customer_addresses ON cust_id = cust_addr_id AND is_billing = TRUE"
          }

          // Self-join
          "self-join" {
            val employees = Customers `as` "employees"
            val managers = Customers `as` "managers"

            val actual =
                select(
                        employees[Customers.fullName],
                        managers[Customers.fullName] `as` "manager_name",
                    )
                    .from { employees }
                    .leftJoin { managers on { employees[Customers.id] eq managers[Customers.id] } }
                    .str()

            actual shouldBe
                "SELECT employees.full_name, managers.full_name AS manager_name " +
                    "FROM customers employees " +
                    "LEFT JOIN customers managers ON employees.id = managers.id"
          }

          // JOIN with WHERE clause
          "join with where clause" {
            val actual =
                select()
                    .from { Customers }
                    .join { CustomerAddresses on { Customers.id eq CustomerAddresses.customerId } }
                    .where { CustomerAddresses.isBilling eq true }
                    .str()
            actual shouldBe
                "SELECT * FROM customers " +
                    "JOIN customer_addresses ON customers.id = customer_addresses.customer_id " +
                    "WHERE customer_addresses.is_billing = TRUE"
          }

          // JOIN with GROUP BY and aggregate functions
          "join with group by and aggregate functions" {
            val actual =
                select(Customers.fullName, count() `as` "address_count")
                    .from { Customers }
                    .leftJoin {
                      CustomerAddresses on { Customers.id eq CustomerAddresses.customerId }
                    }
                    .groupBy(Customers.fullName)
                    .str()
            actual shouldBe
                "SELECT customers.full_name, COUNT(*) AS address_count " +
                    "FROM customers " +
                    "LEFT JOIN customer_addresses ON customers.id = customer_addresses.customer_id " +
                    "GROUP BY customers.full_name"
          }

          // Complex query with multiple JOINs and conditions
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
                    .groupBy(Customers.fullName)
                    .having { count(Orders.id) gt 0 }
                    .orderBy(count(Orders.id).desc())
                    .str()
            actual shouldBe
                "SELECT customers.full_name, COUNT(sales.orders.id) AS order_count " +
                    "FROM customers " +
                    "LEFT JOIN customer_addresses ON customers.id = customer_addresses.customer_id " +
                    "LEFT JOIN sales.orders ON customers.id = sales.orders.customer_id " +
                    "AND sales.orders.status = 'COMPLETED' " +
                    "GROUP BY customers.full_name " +
                    "HAVING COUNT(sales.orders.id) > 0 " +
                    "ORDER BY COUNT(sales.orders.id) DESC"
          }
        },
    )
