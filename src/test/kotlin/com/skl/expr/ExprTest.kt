package com.skl.expr

import com.skl.sql.RenderContext
import com.skl.test.Tables
import kotlin.test.Test
import kotlin.test.assertEquals

// Helper function to render expressions to SQL
private fun Expr.render(ctx: RenderContext): String =
    StringBuilder().apply { toSql(this, ctx) }.toString()

class ExprTest {
  private val users = Tables.Users()
  private val ctx = RenderContext(mapOf(users to "u"))

  @Test
  fun `test Eq expression`() {
    val expr = users.firstName eq "John"
    assertEquals("u.first_name = 'John'", expr.render(ctx))
  }

  @Test
  fun `test Eq with null literal`() {
    val expr = Expr.Eq(Operand.FieldRef(users.firstName), Operand.Literal(null))
    assertEquals("u.first_name IS NULL", expr.render(ctx))
  }

  @Test
  fun `test Eq with field reference`() {
    val expr = users.firstName eq users.firstName
    assertEquals("u.first_name = u.first_name", expr.render(ctx))
  }

  @Test
  fun `test Ne expression`() {
    val expr = users.firstName ne "John"
    assertEquals("u.first_name <> 'John'", expr.render(ctx))
  }

  @Test
  fun `test Ne with null literal`() {
    val expr = Expr.Ne(Operand.FieldRef(users.firstName), Operand.Literal(null))
    assertEquals("u.first_name IS NOT NULL", expr.render(ctx))
  }

  @Test
  fun `test Ne with field reference`() {
    val expr = users.firstName ne users.firstName
    assertEquals("u.first_name <> u.first_name", expr.render(ctx))
  }

  @Test
  fun `test Gt expression`() {
    val expr = users.age gt 18
    assertEquals("u.age > 18", expr.render(ctx))
  }

  @Test
  fun `test Gt with named parameter`() {
    val expr = users.age gt param("minAge")
    assertEquals("u.age > @minAge", expr.render(ctx))
  }

  @Test
  fun `test Gt with field reference`() {
    val expr = users.age gt users.age
    assertEquals("u.age > u.age", expr.render(ctx))
  }

  @Test
  fun `test Ge expression`() {
    val expr = users.age ge 18
    assertEquals("u.age >= 18", expr.render(ctx))
  }

  @Test
  fun `test Ge with named parameter`() {
    val expr = users.age ge param("minAge")
    assertEquals("u.age >= @minAge", expr.render(ctx))
  }

  @Test
  fun `test Ge with field reference`() {
    val expr = users.age ge users.age
    assertEquals("u.age >= u.age", expr.render(ctx))
  }

  @Test
  fun `test Lt expression`() {
    val expr = users.age lt 18
    assertEquals("u.age < 18", expr.render(ctx))
  }

  @Test
  fun `test Lt with named parameter`() {
    val expr = users.age lt param("maxAge")
    assertEquals("u.age < @maxAge", expr.render(ctx))
  }

  @Test
  fun `test Lt with field reference`() {
    val expr = users.age lt users.age
    assertEquals("u.age < u.age", expr.render(ctx))
  }

  @Test
  fun `test Le expression`() {
    val expr = users.age le 18
    assertEquals("u.age <= 18", expr.render(ctx))
  }

  @Test
  fun `test Le with named parameter`() {
    val expr = users.age le param("maxAge")
    assertEquals("u.age <= @maxAge", expr.render(ctx))
  }

  @Test
  fun `test Le with field reference`() {
    val expr = users.age le users.age
    assertEquals("u.age <= u.age", expr.render(ctx))
  }

  @Test
  fun `test Between expression`() {
    val expr = users.age.between(18, 30)
    assertEquals("u.age BETWEEN 18 AND 30", expr.render(ctx))
  }

  @Test
  fun `test Between with named parameters`() {
    val expr = Expr.Between(Operand.FieldRef(users.age), param("minAge"), param("maxAge"))
    assertEquals("u.age BETWEEN @minAge AND @maxAge", expr.render(ctx))
  }

  @Test
  fun `test Like expression`() {
    val expr = users.firstName like "J%"
    assertEquals("u.first_name LIKE 'J%'", expr.render(ctx))
  }

  @Test
  fun `test Like with named parameter`() {
    val expr = users.firstName like param("pattern")
    assertEquals("u.first_name LIKE @pattern", expr.render(ctx))
  }

  @Test
  fun `test IsNull expression`() {
    val expr = users.firstName.isNull()
    assertEquals("u.first_name IS NULL", expr.render(ctx))
  }

  @Test
  fun `test IsNotNull expression`() {
    val expr = users.firstName.isNotNull()
    assertEquals("u.first_name IS NOT NULL", expr.render(ctx))
  }

  @Test
  fun `test InList expression`() {
    val expr = users.age `in` listOf(18, 21, 25)
    assertEquals("u.age IN (18, 21, 25)", expr.render(ctx))
  }

  @Test
  fun `test InList with named parameters`() {
    val expr = Expr.InList(Operand.FieldRef(users.age), params("age1", "age2", "age3"))
    assertEquals("u.age IN (@age1, @age2, @age3)", expr.render(ctx))
  }

  @Test
  fun `test NotInList expression`() {
    val expr = users.age notIn listOf(18, 21, 25)
    assertEquals("u.age NOT IN (18, 21, 25)", expr.render(ctx))
  }

  @Test
  fun `test NotInList with named parameters`() {
    val expr = Expr.NotInList(Operand.FieldRef(users.age), params("age1", "age2", "age3"))
    assertEquals("u.age NOT IN (@age1, @age2, @age3)", expr.render(ctx))
  }

  @Test
  fun `test And expression`() {
    val expr = (users.firstName eq "John") and (users.age gt 18)
    assertEquals("u.first_name = 'John' AND u.age > 18", expr.render(ctx))
  }

  @Test
  fun `test Or expression`() {
    val expr = (users.firstName eq "John") or (users.age gt 18)
    assertEquals("u.first_name = 'John' OR u.age > 18", expr.render(ctx))
  }

  @Test
  fun `test param expression`() {
    val expr = users.firstName eq param("name")
    assertEquals("u.first_name = @name", expr.render(ctx))
  }

  @Test
  fun `test operation grouping`() {
    val expr = (users.firstName eq "John") and ((users.age gt 18) or (users.age lt 65)).group()
    assertEquals("u.first_name = 'John' AND (u.age > 18 OR u.age < 65)", expr.render(ctx))
  }
}
