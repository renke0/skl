package com.skl.expr

import com.skl.model.Field

sealed interface Operand {
  data class FieldRef(val field: Field<*>) : Operand

  data class Named(val name: String) : Operand

  data class Literal(val value: Any?) : Operand

  data class NamedList(val names: List<String>) : Operand

  data class LiteralList(val values: List<Any?>) : Operand
}

// Utility functions for creating operands
fun param(name: String): Operand.Named = Operand.Named(name)

fun params(vararg names: String): Operand.NamedList = Operand.NamedList(names.toList())
