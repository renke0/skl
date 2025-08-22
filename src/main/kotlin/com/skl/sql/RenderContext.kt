package com.skl.sql

import com.skl.query.Table

// Rendering context to resolve table names (original or alias)
class RenderContext(private val aliases: Map<Table, String> = emptyMap()) {
  fun nameFor(table: Table): String = aliases[table] ?: table.name
}
