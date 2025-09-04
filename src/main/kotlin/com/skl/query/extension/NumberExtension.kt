package com.skl.query.extension

import com.skl.query.Between
import com.skl.query.ComparisonPredicate
import com.skl.query.NullCheckPredicate
import com.skl.query.TermExpression
import com.skl.query.literal

infix fun Number.eq(that: TermExpression): ComparisonPredicate = this.literal() eq that

infix fun Number.eq(that: String): ComparisonPredicate = this.literal() eq that.literal()

infix fun Number.eq(that: Number): ComparisonPredicate = this.literal() eq that.literal()

infix fun Number.eq(that: Boolean): ComparisonPredicate = this.literal() eq that.literal()

infix fun Number.ne(that: TermExpression): ComparisonPredicate = this.literal() ne that

infix fun Number.ne(that: String): ComparisonPredicate = this.literal() ne that.literal()

infix fun Number.ne(that: Number): ComparisonPredicate = this.literal() ne that.literal()

infix fun Number.ne(that: Boolean): ComparisonPredicate = this.literal() ne that.literal()

infix fun Number.lt(that: TermExpression): ComparisonPredicate = this.literal() lt that

infix fun Number.lt(that: String): ComparisonPredicate = this.literal() lt that.literal()

infix fun Number.lt(that: Number): ComparisonPredicate = this.literal() lt that.literal()

infix fun Number.lt(that: Boolean): ComparisonPredicate = this.literal() lt that.literal()

infix fun Number.le(that: TermExpression): ComparisonPredicate = this.literal() le that

infix fun Number.le(that: String): ComparisonPredicate = this.literal() le that.literal()

infix fun Number.le(that: Number): ComparisonPredicate = this.literal() le that.literal()

infix fun Number.le(that: Boolean): ComparisonPredicate = this.literal() le that.literal()

infix fun Number.gt(that: TermExpression): ComparisonPredicate = this.literal() gt that

infix fun Number.gt(that: String): ComparisonPredicate = this.literal() gt that.literal()

infix fun Number.gt(that: Number): ComparisonPredicate = this.literal() gt that.literal()

infix fun Number.gt(that: Boolean): ComparisonPredicate = this.literal() gt that.literal()

infix fun Number.ge(that: TermExpression): ComparisonPredicate = this.literal() ge that

infix fun Number.ge(that: String): ComparisonPredicate = this.literal() ge that.literal()

infix fun Number.ge(that: Number): ComparisonPredicate = this.literal() ge that.literal()

infix fun Number.ge(that: Boolean): ComparisonPredicate = this.literal() ge that.literal()

infix fun Number.like(that: TermExpression): ComparisonPredicate = this.literal() like that

infix fun Number.like(that: String): ComparisonPredicate = this.literal() like that.literal()

infix fun Number.like(that: Number): ComparisonPredicate = this.literal() like that.literal()

infix fun Number.like(that: Boolean): ComparisonPredicate = this.literal() like that.literal()

infix fun Number.`in`(list: List<TermExpression>): ComparisonPredicate = this.literal() `in` list

infix fun Number.between(range: TermExpression.BetweenRange): Between = this.literal() between range

infix fun Number.and(that: TermExpression): TermExpression.BetweenRange = this.literal() and that

infix fun Number.and(that: String): TermExpression.BetweenRange = this.literal() and that.literal()

infix fun Number.and(that: Number): TermExpression.BetweenRange = this.literal() and that.literal()

infix fun Number.and(that: Boolean): TermExpression.BetweenRange = this.literal() and that.literal()

fun Number.isNull(): NullCheckPredicate = this.literal().isNull()

fun Number.isNotNull(): NullCheckPredicate = this.literal().isNotNull()
