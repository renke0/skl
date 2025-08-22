package com.skl.test

import com.skl.query.Table

object Tables {
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

  class Products : Table("products") {
    val id = field<Int>("id")
    val productName = field<String>("name")
  }

  class OrderItems : Table("order_items") {
    val orderId = field<Int>("order_id")
    val productId = field<Int>("product_id")
    val quantity = field<Int>("quantity")
  }
}
