package com.skl

import com.skl.printer.QueryStyle
import com.skl.query.QueryContext
import com.skl.query.SelectExpression
import com.skl.query.SelectStep
import com.skl.query.SelectSupport
import com.skl.vendor.Vendor

object SKL : EntryPoint {
  fun configure(): SKLConfig = SKLConfig()
}

data class SKLConfig(
    val vendor: Vendor = Vendor.default,
    val style: QueryStyle = QueryStyle.default,
) {
  fun withVendor(vendor: Vendor) = copy(vendor = vendor)

  fun withStyle(style: QueryStyle) = copy(style = style)
}

interface EntryPoint {
  fun select(vararg selected: SelectExpression): SelectStep {
    val entryPoint = SelectEntryPoint()
    return entryPoint.select { selected.toList() }
  }
}

private class SelectEntryPoint : SelectSupport {
  override val context = QueryContext(Vendor.default, QueryStyle.default)
}
