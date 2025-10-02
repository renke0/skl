package skl.sql

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class SqlQueryModelTest :
  DescribeSpec({
    val complexQuery = buildComplexQuery()

    describe("SqlQuery composition") {
      it("captures recursive CTE metadata and hints") {
        val withClause = complexQuery.withClause.shouldNotBeNull()
        withClause.recursive shouldBe true
        withClause.commonTableExpressions
          .map { it.name }
          .shouldContainExactly("important_regions", "org_tree", "regional_totals")

        val orgTree = withClause.commonTableExpressions[1]
        orgTree.materialization shouldBe MaterializedHint.BY_ENGINE
        orgTree.searchClause?.kind shouldBe SearchKind.DEPTH_FIRST
        orgTree.cycleClause?.cyclePathColumn shouldBe "cycle_path"
      }

      it("supports analytic windowing with QUALIFY filters") {
        val select = complexQuery.body.shouldBeInstanceOf<SelectQuery>()
        select.setQuantifier shouldBe SetQuantifier.DISTINCT
        select.distinctOn.shouldHaveSize(1)

        val windowClause = select.window.shouldNotBeNull()
        windowClause.definitions.shouldHaveSize(1)

        val windowSpec = windowClause.definitions.first().specification
        windowSpec.partitionBy.shouldHaveSize(1)
        windowSpec.orderBy.first().direction shouldBe SortDirection.ASC

        select.qualify.shouldNotBeNull().condition.shouldBeInstanceOf<ComparisonExpression>()
        select.groupBy.shouldNotBeNull().elements.shouldHaveSize(1)
      }

      it("retains advanced table constructs like MATCH_RECOGNIZE and pivots") {
        val select = complexQuery.body.shouldBeInstanceOf<SelectQuery>()
        val relations = select.from.shouldNotBeNull().relations
        relations.shouldHaveSize(2)

        val join = relations.first().shouldBeInstanceOf<JoinTable>()
        val matchRecognize = join.right.shouldBeInstanceOf<MatchRecognizeTable>().matchRecognize
        matchRecognize.pattern.shouldBeInstanceOf<PatternConcatenation>()
        matchRecognize.afterMatchSkip.shouldNotBeNull().option shouldBe
          AfterMatchSkipOption.PAST_LAST_ROW

        relations[1].shouldBeInstanceOf<PivotTable>()
      }

      it("exposes pagination and locking instructions") {
        complexQuery.offset.shouldNotBeNull().count shouldBe NumericLiteral("5")
        complexQuery.fetch.shouldNotBeNull().withTies shouldBe true
        complexQuery.limit.shouldNotBeNull().count shouldBe NumericLiteral("500")

        val locking = complexQuery.locking.single()
        locking.waitOption shouldBe LockWaitOption.NOWAIT
      }
    }
  })
