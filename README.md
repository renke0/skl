# SKL - SQL Query Builder for Kotlin

SKL is a type-safe SQL query builder for Kotlin that allows you to build SQL queries using a fluent API. It provides a clean and intuitive way to construct complex SQL queries without writing raw SQL strings, reducing the risk of SQL injection and syntax errors.

## Table of Contents

<!-- toc -->

- [Overview](#overview)
- [Core Concepts](#core-concepts)
  - [Tables](#tables)
  - [Columns](#columns)
  - [Terms](#terms)
  - [Predicates](#predicates)
  - [Aliases](#aliases)
  - [Clauses](#clauses)
- [Usage Examples](#usage-examples)
  - [Basic SELECT Queries](#basic-select-queries)
  - [WHERE Clauses](#where-clauses)
  - [JOINs](#joins)
  - [GROUP BY and Aggregate Functions](#group-by-and-aggregate-functions)
  - [Complex Queries](#complex-queries)
  <!-- /toc -->

## Overview

SKL provides a Kotlin DSL for building SQL queries. It allows you to:

- Build SELECT, FROM, JOIN, WHERE, GROUP BY, HAVING, ORDER BY, LIMIT, and OFFSET clauses
- Use type-safe expressions for columns, tables, and conditions
- Create aliases for tables and columns
- Use functions like COUNT, SUM, AVG, etc.
- Build complex predicates with AND, OR, NOT, etc.
- Pretty-print the generated SQL for debugging

## Core Concepts

### Tables

Tables are a simple structure in SKL, designed to be straightforward and easy to use. Despite their simplicity, they provide all the functionality needed to represent database tables in your queries. Tables are represented by the `Table` class, which implements the `TableLike` interface. You define a table by extending the `Table` class and defining its columns:

```kotlin
object Customers : Table<Customers>("customers") {
  val id = column("id")
  val email = column("email")
  val fullName = column("full_name")
  val phone = column("phone")
  val createdAt = column("created_at")
}
```

Tables can have a schema and database:

```kotlin
object Orders : Table<Orders>(tableName = "orders", tableSchema = "sales", tableDatabase = "ecommerce") {
  val id = column("id")
  val orderNumber = column("order_number")
  val customerId = column("customer_id")
  val orderDate = column("order_date")
  val status = column("status")
  val totalAmount = column("total_amount")
}
```

### Columns

Columns are represented by the `Column` class, which is associated with a `TableLike` owner. Columns can be used in SELECT, GROUP BY, and ORDER BY clauses:

```kotlin
val id = column("id")  // Defined within a Table
```

### Terms

Terms are atomic values or expressions used in predicates. They can be column references, literals, function calls, or expressions. Terms serve as the building blocks of SQL conditions and can be used on either side of comparison predicates.

Types of terms include:

- Column references: `Customers.email`
- Literals: `"test@example.com".literal()`, `42.literal()`, `true.literal()`
- Function calls: `count()`, `sum(Orders.totalAmount)`, `lower(Customers.email)`
- Parameters: `Parameter("email")`

### Predicates

Predicates represent SQL conditions used in WHERE or JOIN clauses. They include:

- Comparison predicates: `=`, `<>`, `<`, `<=`, `>`, `>=`, `IN`, `LIKE`
- Logical operators: `AND`, `OR`, `NOT`
- NULL checks: `IS NULL`, `IS NOT NULL`
- BETWEEN operator
- Quantifier predicates: `ANY`, `ALL`, `SOME`, `EXISTS`

Predicates can be combined using the `and` and `or` operators, and they can be grouped using the `group()` method.

### Aliases

Aliases can be applied to various types of terms to give them different names in the query:

- Table aliases: `Customers as "c"`
- Column aliases: `Customers.email as "cus_email"`
- Function call aliases: `count() as "total_count"`, `sum(Orders.totalAmount) as "total_amount"`
- Literal aliases: `"Active".literal() as "status"`, `42.literal() as "limit_value"`

Aliases are represented by the `Aliased` interface, which is implemented by `AliasedTable` and `AliasedTerm`.

### Clauses

Clauses are the building blocks of SQL queries. SKL supports the following clauses:

- SELECT: Specifies the columns to retrieve
- FROM: Specifies the table to query
- JOIN: Combines rows from multiple tables
- WHERE: Filters rows based on conditions
- GROUP BY: Groups rows based on columns
- HAVING: Filters groups based on conditions
- ORDER BY: Sorts the result set
- LIMIT: Limits the number of rows returned
- OFFSET: Skips a number of rows

## Usage Examples

### Basic SELECT Queries

```kotlin
// Select all columns
val query1 = select().from { Customers }.str()
// SELECT * FROM customers

// Select specific columns
val query2 = select(Customers.email, Customers.fullName).from { Customers }.str()
// SELECT customers.email, customers.full_name FROM customers

// Select with column aliases
val query3 = select(Customers.email `as` "cus_email").from { Customers }.str()
// SELECT customers.email AS cus_email FROM customers

// Select with table aliases
val query4 = select(Customers.email).from { Customers `as` "cus" }.str()
// SELECT cus.email FROM customers cus

// Select with functions
val query5 = select(count()).from { Customers }.str()
// SELECT COUNT(*) FROM customers

val query6 = select(count() `as` "total_customers").from { Customers }.str()
// SELECT COUNT(*) AS total_customers FROM customers
```

### WHERE Clauses

```kotlin
// Simple equality
val query1 = select().from { Customers }.where { Customers.email eq "test@example.com" }.str()
// SELECT * FROM customers WHERE customers.email = 'test@example.com'

// Logical operators
val query2 = select()
    .from { Customers }
    .where {
      (Customers.email eq "test@example.com") and
          (Customers.fullName eq "Test User")
    }
    .str()
// SELECT * FROM customers WHERE customers.email = 'test@example.com' AND customers.full_name = 'Test User'

// Grouped conditions
val query3 = select()
    .from { Customers }
    .where {
      (Customers.email eq "test@example.com") and
          ((Customers.fullName eq "Test User") or (Customers.fullName eq "Other User")).group()
    }
    .str()
// SELECT * FROM customers WHERE customers.email = 'test@example.com' AND (customers.full_name = 'Test User' OR customers.full_name = 'Other User')

// NULL checks
val query4 = select().from { Customers }.where { Customers.phone.isNull() }.str()
// SELECT * FROM customers WHERE customers.phone IS NULL

// IN operator
val query5 = select()
    .from { Customers }
    .where {
      Customers.email `in`
          listOf("test@example.com".literal(), "other@example.com".literal())
    }
    .str()
// SELECT * FROM customers WHERE customers.email IN ('test@example.com', 'other@example.com')

// LIKE operator
val query6 = select()
    .from { Customers }
    .where { Customers.email like "%@example.com".literal() }
    .str()
// SELECT * FROM customers WHERE customers.email LIKE '%@example.com'

// BETWEEN operator
val query7 = select()
    .from { Orders }
    .where { Orders.totalAmount.between(50.0.literal() and 100.0.literal()) }
    .str()
// SELECT * FROM sales.orders WHERE sales.orders.total_amount BETWEEN 50.0 AND 100.0
```

### JOINs

```kotlin
// Inner join
val query1 = select()
    .from { Customers }
    .join { CustomerAddresses on { Customers.id eq CustomerAddresses.customerId } }
    .str()
// SELECT * FROM customers JOIN customer_addresses ON customers.id = customer_addresses.customer_id

// Left join
val query2 = select()
    .from { Customers }
    .leftJoin {
      CustomerAddresses on { Customers.id eq CustomerAddresses.customerId }
    }
    .str()
// SELECT * FROM customers LEFT JOIN customer_addresses ON customers.id = customer_addresses.customer_id

// Multiple joins
val query3 = select()
    .from { Customers }
    .join { CustomerAddresses on { Customers.id eq CustomerAddresses.customerId } }
    .join { Addresses on { Addresses.id eq CustomerAddresses.addressId } }
    .str()
// SELECT * FROM customers JOIN customer_addresses ON customers.id = customer_addresses.customer_id JOIN addresses ON addresses.id = customer_addresses.address_id

// Join with complex condition
val query4 = select()
    .from { Customers }
    .join {
      CustomerAddresses on
          {
            (Customers.id eq CustomerAddresses.customerId) and
                (CustomerAddresses.isBilling eq true)
          }
    }
    .str()
// SELECT * FROM customers JOIN customer_addresses ON customers.id = customer_addresses.customer_id AND customer_addresses.is_billing = TRUE

// Self-join
val employees = Customers `as` "employees"
val managers = Customers `as` "managers"

val query5 = select(
        employees[Customers.fullName],
        managers[Customers.fullName] `as` "manager_name",
    )
    .from { employees }
    .leftJoin { managers on { employees[Customers.id] eq managers[Customers.id] } }
    .str()
// SELECT employees.full_name, managers.full_name AS manager_name FROM customers employees LEFT JOIN customers managers ON employees.id = managers.id
```

### GROUP BY and Aggregate Functions

```kotlin
// Simple GROUP BY
val query1 = select(Customers.fullName, count() `as` "order_count")
    .from { Customers }
    .leftJoin {
      Orders on { Customers.id eq Orders.customerId }
    }
    .groupBy(Customers.fullName)
    .str()
// SELECT customers.full_name, COUNT(*) AS order_count FROM customers LEFT JOIN sales.orders ON customers.id = sales.orders.customer_id GROUP BY customers.full_name

// GROUP BY with HAVING
val query2 = select(Customers.fullName, count(Orders.id) `as` "order_count")
    .from { Customers }
    .leftJoin {
      Orders on { Customers.id eq Orders.customerId }
    }
    .groupBy(Customers.fullName)
    .having { count(Orders.id) gt 0 }
    .str()
// SELECT customers.full_name, COUNT(sales.orders.id) AS order_count FROM customers LEFT JOIN sales.orders ON customers.id = sales.orders.customer_id GROUP BY customers.full_name HAVING COUNT(sales.orders.id) > 0
```

### Complex Queries

```kotlin
val query = select(Customers.fullName, count(Orders.id) `as` "order_count")
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
// SELECT customers.full_name, COUNT(sales.orders.id) AS order_count FROM customers LEFT JOIN customer_addresses ON customers.id = customer_addresses.customer_id LEFT JOIN sales.orders ON customers.id = sales.orders.customer_id AND sales.orders.status = 'COMPLETED' GROUP BY customers.full_name HAVING COUNT(sales.orders.id) > 0 ORDER BY COUNT(sales.orders.id) DESC
```
