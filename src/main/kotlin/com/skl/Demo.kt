package com.skl

import com.skl.expr.param
import com.skl.query.Table
import com.skl.query.`as`
import com.skl.query.from

class Users : Table("users") {
  val firstName = field<String>("first_name")
  val age = field<Int>("age")
  val id = field<Int>("id")
}

class Orders : Table("orders") {
  val id = field<Int>("id")
  val userId = field<Int>("user_id")
  val total = field<Double>("total")
}

val users = Users()
val orders = Orders()

fun basicDemo(): Pair<String, String> {
  val q =
      from { users `as` "us" }
          .select(users.firstName, users.age)
          .where {
            (users.firstName ne param("firstname")) and
                (users.age eq param("age")) and
                (users.age eq 18)
          }

  return q.print() to q.prettyPrint()
}

fun joinDemo(): Pair<String, String> {
  val q =
      from { users `as` "us" }
          .join { orders on { users.id eq orders.userId } }
          .select(users.firstName, orders.total)
          .where { orders.total gt 100.0 }

  return q.print() to q.prettyPrint()
}

fun leftJoinDemo(): Pair<String, String> {
  val q =
      from { users `as` "us" }
          .leftJoin { orders on { users.id eq orders.userId } }
          .select(users.firstName, orders.total)
          .toQuery()

  return q.print() to q.prettyPrint()
}

fun multipleJoinsDemo(): Pair<String, String> {
  class Products : Table("products") {
    val id = field<Int>("id")
    val productName = field<String>("name")
  }

  class OrderItems : Table("order_items") {
    val orderId = field<Int>("order_id")
    val productId = field<Int>("product_id")
    val quantity = field<Int>("quantity")
  }

  val products = Products()
  val orderItems = OrderItems()

  val q =
      from { users }
          .join { orders on { users.id eq orders.userId } }
          .join { orderItems on { orders.id eq orderItems.orderId } }
          .rightJoin { products on { orderItems.productId eq products.id } }
          .select(users.firstName, orders.total, products.productName, orderItems.quantity)
          .toQuery()

  return q.print() to q.prettyPrint()
}

fun main() {
  println("Basic Query:")
  basicDemo().let { (sql, pretty) ->
    println("SQL: $sql")
    println("Pretty: $pretty")
  }

  println("\nJoin Query:")
  joinDemo().let { (sql, pretty) ->
    println("SQL: $sql")
    println("Pretty: $pretty")
  }

  println("\nLeft Join Query:")
  leftJoinDemo().let { (sql, pretty) ->
    println("SQL: $sql")
    println("Pretty: $pretty")
  }

  println("\nMultiple Joins Query:")
  multipleJoinsDemo().let { (sql, pretty) ->
    println("SQL: $sql")
    println("Pretty: $pretty")
  }
}
