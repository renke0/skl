package com.skl.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SqlUtilsTest {

  @Test
  fun `test sqlLiteral with string`() {
    assertEquals("'test'", sqlLiteral("test"))
    assertEquals("'test''s'", sqlLiteral("test's"))
    assertEquals("''", sqlLiteral(""))
  }

  @Test
  fun `test sqlLiteral with numbers`() {
    assertEquals("42", sqlLiteral(42))
    assertEquals("42.5", sqlLiteral(42.5))
    assertEquals("0", sqlLiteral(0))
    assertEquals("-1", sqlLiteral(-1))
  }

  @Test
  fun `test sqlLiteral with boolean`() {
    assertEquals("TRUE", sqlLiteral(true))
    assertEquals("FALSE", sqlLiteral(false))
  }

  @Test
  fun `test sqlLiteral with other types`() {
    data class TestClass(val value: String) {
      override fun toString(): String = value
    }
    val testObject = TestClass("test")
    assertEquals("'test'", sqlLiteral(testObject))
  }

  @Test
  fun `test sqlNonNullLiteral with non-null values`() {
    assertEquals("'test'", sqlNonNullLiteral("test"))
    assertEquals("42", sqlNonNullLiteral(42))
    assertEquals("TRUE", sqlNonNullLiteral(true))
  }

  @Test
  fun `test sqlNonNullLiteral with null throws error`() {
    val exception = assertFailsWith<IllegalStateException> { sqlNonNullLiteral(null) }
    assertEquals("NULL not allowed here; use eq(null) or isNull()", exception.message)
  }
}
