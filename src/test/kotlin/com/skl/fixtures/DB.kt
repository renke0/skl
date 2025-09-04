package com.skl.fixtures

import com.skl.query.Table

object Customers : Table<Customers>("customers") {
  val id = column("id")
  val email = column("email")
  val fullName = column("full_name")
  val phone = column("phone")
  val createdAt = column("created_at")
}

object Addresses : Table<Addresses>("addresses") {
  val id = column("id")
  val line1 = column("line1")
  val line2 = column("line2")
  val city = column("city")
  val region = column("region")
  val postalCode = column("postal_code")
  val country = column("country")
}

object CustomerAddresses : Table<CustomerAddresses>("customer_addresses") {
  val customerId = column("customer_id")
  val addressId = column("address_id")
  val isBilling = column("is_billing")
  val isShipping = column("is_shipping")
}

object Orders : Table<Orders>(tableName = "orders", tableSchema = "sales") {
  val id = column("id")
  val orderNumber = column("order_number")
  val customerId = column("customer_id")
  val orderDate = column("order_date")
  val status = column("status")
  val totalAmount = column("total_amount")
}

object Reports :
    Table<Reports>(
        tableName = "reports",
        tableDatabase = "analytics_db",
        tableSchema = "public",
    ) {
  val id = column("id")
  val reportName = column("report_name")
  val createdAt = column("created_at")
  val updatedAt = column("updated_at")
}
