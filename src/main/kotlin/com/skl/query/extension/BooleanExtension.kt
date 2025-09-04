package com.skl.query.extension

import com.skl.query.Between
import com.skl.query.ComparisonPredicate
import com.skl.query.NullCheckPredicate
import com.skl.query.TermExpression
import com.skl.query.literal

infix fun Boolean.eq(that: TermExpression): ComparisonPredicate = this.literal() eq that

infix fun Boolean.eq(that: String): ComparisonPredicate = this.literal() eq that.literal()

infix fun Boolean.eq(that: Number): ComparisonPredicate = this.literal() eq that.literal()

infix fun Boolean.eq(that: Boolean): ComparisonPredicate = this.literal() eq that.literal()

infix fun Boolean.ne(that: TermExpression): ComparisonPredicate = this.literal() ne that

infix fun Boolean.ne(that: String): ComparisonPredicate = this.literal() ne that.literal()

infix fun Boolean.ne(that: Number): ComparisonPredicate = this.literal() ne that.literal()

infix fun Boolean.ne(that: Boolean): ComparisonPredicate = this.literal() ne that.literal()

infix fun Boolean.lt(that: TermExpression): ComparisonPredicate = this.literal() lt that

infix fun Boolean.lt(that: String): ComparisonPredicate = this.literal() lt that.literal()

infix fun Boolean.lt(that: Number): ComparisonPredicate = this.literal() lt that.literal()

infix fun Boolean.lt(that: Boolean): ComparisonPredicate = this.literal() lt that.literal()

infix fun Boolean.le(that: TermExpression): ComparisonPredicate = this.literal() le that

infix fun Boolean.le(that: String): ComparisonPredicate = this.literal() le that.literal()

infix fun Boolean.le(that: Number): ComparisonPredicate = this.literal() le that.literal()

infix fun Boolean.le(that: Boolean): ComparisonPredicate = this.literal() le that.literal()

infix fun Boolean.gt(that: TermExpression): ComparisonPredicate = this.literal() gt that

infix fun Boolean.gt(that: String): ComparisonPredicate = this.literal() gt that.literal()

infix fun Boolean.gt(that: Number): ComparisonPredicate = this.literal() gt that.literal()

infix fun Boolean.gt(that: Boolean): ComparisonPredicate = this.literal() gt that.literal()

infix fun Boolean.ge(that: TermExpression): ComparisonPredicate = this.literal() ge that

infix fun Boolean.ge(that: String): ComparisonPredicate = this.literal() ge that.literal()

infix fun Boolean.ge(that: Number): ComparisonPredicate = this.literal() ge that.literal()

infix fun Boolean.ge(that: Boolean): ComparisonPredicate = this.literal() ge that.literal()

infix fun Boolean.like(that: TermExpression): ComparisonPredicate = this.literal() like that

infix fun Boolean.like(that: String): ComparisonPredicate = this.literal() like that.literal()

infix fun Boolean.like(that: Number): ComparisonPredicate = this.literal() like that.literal()

infix fun Boolean.like(that: Boolean): ComparisonPredicate = this.literal() like that.literal()

infix fun Boolean.`in`(list: List<TermExpression>): ComparisonPredicate = this.literal() `in` list

infix fun Boolean.between(range: TermExpression.BetweenRange): Between =
    this.literal() between range

infix fun Boolean.and(that: TermExpression): TermExpression.BetweenRange = this.literal() and that

infix fun Boolean.and(that: String): TermExpression.BetweenRange = this.literal() and that.literal()

infix fun Boolean.and(that: Number): TermExpression.BetweenRange = this.literal() and that.literal()

fun Boolean.isNull(): NullCheckPredicate = this.literal().isNull()

fun Boolean.isNotNull(): NullCheckPredicate = this.literal().isNotNull()
