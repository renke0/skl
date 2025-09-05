package com.skl.query

interface Aliased<T> {
  val value: T
  val alias: String
}
