package com.skl.query.extension

import com.skl.query.Between
import com.skl.query.ComparisonPredicate
import com.skl.query.NullCheckPredicate
import com.skl.query.TermExpression
import com.skl.query.literal

infix fun String.eq(that: TermExpression): ComparisonPredicate = this.literal() eq that

infix fun String.eq(that: String): ComparisonPredicate = this.literal() eq that.literal()

infix fun String.eq(that: Number): ComparisonPredicate = this.literal() eq that.literal()

infix fun String.eq(that: Boolean): ComparisonPredicate = this.literal() eq that.literal()

infix fun String.ne(that: TermExpression): ComparisonPredicate = this.literal() ne that

infix fun String.ne(that: String): ComparisonPredicate = this.literal() ne that.literal()

infix fun String.ne(that: Number): ComparisonPredicate = this.literal() ne that.literal()

infix fun String.ne(that: Boolean): ComparisonPredicate = this.literal() ne that.literal()

infix fun String.lt(that: TermExpression): ComparisonPredicate = this.literal() lt that

infix fun String.lt(that: String): ComparisonPredicate = this.literal() lt that.literal()

infix fun String.lt(that: Number): ComparisonPredicate = this.literal() lt that.literal()

infix fun String.lt(that: Boolean): ComparisonPredicate = this.literal() lt that.literal()

infix fun String.le(that: TermExpression): ComparisonPredicate = this.literal() le that

infix fun String.le(that: String): ComparisonPredicate = this.literal() le that.literal()

infix fun String.le(that: Number): ComparisonPredicate = this.literal() le that.literal()

infix fun String.le(that: Boolean): ComparisonPredicate = this.literal() le that.literal()

infix fun String.gt(that: TermExpression): ComparisonPredicate = this.literal() gt that

infix fun String.gt(that: String): ComparisonPredicate = this.literal() gt that.literal()

infix fun String.gt(that: Number): ComparisonPredicate = this.literal() gt that.literal()

infix fun String.gt(that: Boolean): ComparisonPredicate = this.literal() gt that.literal()

infix fun String.ge(that: TermExpression): ComparisonPredicate = this.literal() ge that

infix fun String.ge(that: String): ComparisonPredicate = this.literal() ge that.literal()

infix fun String.ge(that: Number): ComparisonPredicate = this.literal() ge that.literal()

infix fun String.ge(that: Boolean): ComparisonPredicate = this.literal() ge that.literal()

infix fun String.like(that: TermExpression): ComparisonPredicate = this.literal() like that

infix fun String.like(that: String): ComparisonPredicate = this.literal() like that.literal()

infix fun String.like(that: Number): ComparisonPredicate = this.literal() like that.literal()

infix fun String.like(that: Boolean): ComparisonPredicate = this.literal() like that.literal()

infix fun String.`in`(list: List<TermExpression>): ComparisonPredicate = this.literal() `in` list

infix fun String.between(range: TermExpression.BetweenRange): Between = this.literal() between range

infix fun String.and(that: TermExpression): TermExpression.BetweenRange = this.literal() and that

infix fun String.and(that: String): TermExpression.BetweenRange = this.literal() and that.literal()

infix fun String.and(that: Number): TermExpression.BetweenRange = this.literal() and that.literal()

infix fun String.and(that: Boolean): TermExpression.BetweenRange = this.literal() and that.literal()

fun String.isNull(): NullCheckPredicate = this.literal().isNull()

fun String.isNotNull(): NullCheckPredicate = this.literal().isNotNull()
