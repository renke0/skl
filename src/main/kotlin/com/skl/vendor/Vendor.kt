package com.skl.vendor

import com.skl.printer.KeywordStyle

interface Vendor {
  val name: String
  val allowedKeywordCasing: Set<KeywordStyle>

  companion object {
    val default = BaseVendor(name = "default")
  }
}

data class BaseVendor(
    override val name: String,
    override val allowedKeywordCasing: Set<KeywordStyle> = KeywordStyle.entries.toSet(),
) : Vendor
