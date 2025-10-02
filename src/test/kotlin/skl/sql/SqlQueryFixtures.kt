package skl.sql

private const val REGIONAL_WINDOW = "regional_window"

fun buildComplexQuery(): SqlQuery = ComplexQueryBuilder().build()

private class ComplexQueryBuilder {

  fun build(): SqlQuery =
    SqlQuery(
      withClause = buildWithClause(),
      body = buildSelectQuery(),
      orderBy = orderByClauses(),
      offset = OffsetClause(NumericLiteral("5"), OffsetUnit.ROWS),
      fetch = FetchClause(FetchOption.FIRST_ROWS, NumericLiteral("50"), withTies = true),
      limit = LimitClause(NumericLiteral("500")),
      locking = lockingClauses())

  private fun buildWithClause(): WithClause =
    WithClause(
      recursive = true,
      commonTableExpressions = listOf(importantRegionsCte(), orgTreeCte(), regionalTotalsCte()))

  private fun importantRegionsCte(): CommonTableExpression =
    CommonTableExpression(
      name = "important_regions",
      columnNames = listOf("region"),
      query =
        ValuesQuery(
          rows =
            listOf(
              listOf(StringLiteral("NORTH")),
              listOf(StringLiteral("WEST")),
              listOf(StringLiteral("EAST")))),
      materialization = MaterializedHint.NOT_MATERIALIZED)

  private fun orgTreeCte(): CommonTableExpression =
    CommonTableExpression(
      name = "org_tree",
      columnNames = listOf("id", "manager_id", "lvl", "path"),
      query =
        SetQuery(
          left = orgBaseSelect(),
          operator = SetOperator.UNION,
          right = orgRecursiveSelect(),
          quantifier = SetQuantifier.ALL),
      searchClause = SearchClause(SearchKind.DEPTH_FIRST, "search_seq"),
      cycleClause =
        CycleClause(
          columnName = "is_cycle",
          cycleMarkValue = StringLiteral("Y"),
          nonCycleMarkValue = StringLiteral("N"),
          cyclePathColumn = "cycle_path"))

  private fun regionalTotalsCte(): CommonTableExpression =
    CommonTableExpression(
      name = "regional_totals",
      columnNames = listOf("region", "sales_total", "order_count"),
      query = regionalTotalsQuery())

  private fun orgBaseSelect(): SelectQuery =
    SelectQuery(
      selectList =
        listOf(
          ExpressionSelectItem(ColumnReference(QualifiedName("o", "id")), alias = "id"),
          ExpressionSelectItem(
            ColumnReference(QualifiedName("o", "manager_id")), alias = "manager_id"),
          ExpressionSelectItem(NumericLiteral("1"), alias = "lvl"),
          ExpressionSelectItem(ColumnReference(QualifiedName("o", "name")), alias = "path")),
      from =
        FromClause(
          listOf(NamedTable(name = QualifiedName("organization"), alias = RelationAlias("o")))),
      where = IsNullExpression(ColumnReference(QualifiedName("o", "manager_id"))))

  private fun orgRecursiveSelect(): SelectQuery =
    SelectQuery(
      selectList =
        listOf(
          ExpressionSelectItem(ColumnReference(QualifiedName("c", "id")), alias = "id"),
          ExpressionSelectItem(
            ColumnReference(QualifiedName("c", "manager_id")), alias = "manager_id"),
          ExpressionSelectItem(
            BinaryExpression(
              ColumnReference(QualifiedName("p", "lvl")), BinaryOperator.PLUS, NumericLiteral("1")),
            alias = "lvl"),
          ExpressionSelectItem(
            FunctionCall(
              name = QualifiedName("CONCAT"),
              arguments =
                listOf(
                  ColumnReference(QualifiedName("p", "path")),
                  StringLiteral(" > "),
                  ColumnReference(QualifiedName("c", "name")))),
            alias = "path")),
      from =
        FromClause(
          listOf(
            JoinTable(
              left = NamedTable(QualifiedName("org_tree"), alias = RelationAlias("p")),
              right = NamedTable(QualifiedName("organization"), alias = RelationAlias("c")),
              type = JoinType.INNER,
              condition =
                OnCondition(
                  ComparisonExpression(
                    ColumnReference(QualifiedName("c", "manager_id")),
                    ComparisonOperator.EQUAL,
                    ColumnReference(QualifiedName("p", "id"))))))))

  private fun regionalTotalsQuery(): SelectQuery =
    SelectQuery(
      selectList = regionalTotalsSelectItems(),
      from = FromClause(listOf(regionalTotalsJoin())),
      where =
        BetweenExpression(
          value = ColumnReference(QualifiedName("s", "order_date")),
          lower = DateLiteral("2023-01-01"),
          upper = DateLiteral("2023-12-31")),
      groupBy =
        GroupByClause(
          elements = listOf(GroupByExpression(ColumnReference(QualifiedName("s", "region"))))),
      having =
        ComparisonExpression(
          FunctionCall(
            name = QualifiedName("SUM"),
            arguments = listOf(ColumnReference(QualifiedName("s", "amount")))),
          ComparisonOperator.GREATER_THAN,
          NumericLiteral("1000")))

  private fun regionalTotalsSelectItems(): List<SelectItem> =
    listOf(
      ExpressionSelectItem(ColumnReference(QualifiedName("s", "region")), alias = "region"),
      ExpressionSelectItem(
        FunctionCall(
          name = QualifiedName("SUM"),
          arguments = listOf(ColumnReference(QualifiedName("s", "amount")))),
        alias = "sales_total"),
      ExpressionSelectItem(
        FunctionCall(name = QualifiedName("COUNT"), arguments = listOf(ExpressionStar)),
        alias = "order_count"))

  private fun regionalTotalsJoin(): JoinTable =
    JoinTable(
      left = salesSourceTable(),
      right = importantRegionsReference(),
      type = JoinType.INNER,
      condition =
        OnCondition(
          ComparisonExpression(
            ColumnReference(QualifiedName("s", "region")),
            ComparisonOperator.EQUAL,
            ColumnReference(QualifiedName("ir", "region")))))

  private fun salesSourceTable(): NamedTable =
    NamedTable(
      name = QualifiedName("sales"),
      alias = RelationAlias("s"),
      tableHint = TableHint(listOf("INDEX(sales_region_idx)")),
      sample = TableSampleClause(method = SampleMethod.BERNOULLI, size = NumericLiteral("50")))

  private fun importantRegionsReference(): NamedTable =
    NamedTable(name = QualifiedName("important_regions"), alias = RelationAlias("ir"))

  private fun averageOrderSubquery(): QueryExpression =
    SelectQuery(
      selectList =
        listOf(
          ExpressionSelectItem(
            FunctionCall(
              name = QualifiedName("AVG"),
              arguments = listOf(ColumnReference(QualifiedName("s2", "amount")))))),
      from = FromClause(listOf(NamedTable(QualifiedName("sales"), alias = RelationAlias("s2")))))

  private fun buildSelectQuery(): SelectQuery {
    val regionFilterSubquery = regionFilterSubquery()
    val averageOrderSubquery = averageOrderSubquery()

    return SelectQuery(
      setQuantifier = SetQuantifier.DISTINCT,
      distinctOn = listOf(ColumnReference(QualifiedName("s", "order_id"))),
      selectList = selectItems(averageOrderSubquery, regionFilterSubquery),
      from = FromClause(listOf(finalJoin(), categoryPivot())),
      where = whereClause(regionFilterSubquery),
      groupBy = groupByClause(),
      window = regionalWindowClause(),
      qualify = qualifyClause())
  }

  private fun regionFilterSubquery(): QueryExpression =
    SelectQuery(
      selectList = listOf(ExpressionSelectItem(ColumnReference(QualifiedName("ir2", "region")))),
      from =
        FromClause(
          listOf(
            NamedTable(name = QualifiedName("important_regions"), alias = RelationAlias("ir2")))))

  private fun selectItems(
    averageOrderSubquery: QueryExpression,
    regionFilterSubquery: QueryExpression
  ): List<SelectItem> = buildList {
    addAll(identitySelectItems())
    addAll(analyticSelectItems())
    addAll(payloadSelectItems(averageOrderSubquery))
    addAll(relatedRelationItems())
    add(hasRegionItem(regionFilterSubquery))
  }

  private fun identitySelectItems(): List<SelectItem> =
    listOf(
      ExpressionSelectItem(ColumnReference(QualifiedName("s", "order_id")), alias = "order_id"),
      ExpressionSelectItem(
        ColumnReference(QualifiedName("rt", "sales_total")), alias = "regional_sales"),
      ExpressionSelectItem(ColumnReference(QualifiedName("org", "path")), alias = "manager_path"))

  private fun analyticSelectItems(): List<SelectItem> =
    listOf(
      ExpressionSelectItem(
        FunctionCall(
          name = QualifiedName("ROW_NUMBER"),
          over = WindowSpecification(baseWindowName = REGIONAL_WINDOW)),
        alias = "regional_rank"),
      ExpressionSelectItem(
        FunctionCall(
          name = QualifiedName("SUM"),
          arguments = listOf(ColumnReference(QualifiedName("s", "amount"))),
          over = WindowSpecification(baseWindowName = REGIONAL_WINDOW)),
        alias = "running_sales"))

  private val orderPayload =
    ExpressionSelectItem(
      StructConstructor(
        fields =
          listOf(
            StructField("order", ColumnReference(QualifiedName("s", "order_id"))),
            StructField(
              "tags", ArrayConstructor(listOf(ColumnReference(QualifiedName("tag", "value"))))))),
      alias = "order_payload")

  private val customerId =
    ExpressionSelectItem(
      JsonExpression(
        target = ColumnReference(QualifiedName("s", "metadata")),
        path = "$.customer.id",
        returningType = SqlType(QualifiedName("VARCHAR"))),
      alias = "customer_id")

  private val regionGroup =
    ExpressionSelectItem(
      CaseExpression(
        branches =
          listOf(
            WhenClause(
              condition =
                InListExpression(
                  ColumnReference(QualifiedName("s", "region")),
                  listOf(StringLiteral("NORTH"), StringLiteral("WEST"))),
              result = StringLiteral("core")),
            WhenClause(
              condition =
                ComparisonExpression(
                  ColumnReference(QualifiedName("s", "region")),
                  ComparisonOperator.EQUAL,
                  StringLiteral("EAST")),
              result = StringLiteral("growth"))),
        elseExpression = StringLiteral("other")),
      alias = "region_group")

  private val asOf =
    ExpressionSelectItem(
      CastExpression(
        expression = TimestampLiteral("2023-01-01T00:00:00"),
        targetType = SqlType(QualifiedName("TIMESTAMP"))),
      alias = "as_of")

  private val analysisInterval =
    ExpressionSelectItem(
      IntervalLiteral("P1M", IntervalQualifier(startField = DateTimeField.MONTH)),
      alias = "analysis_interval")
  val defaultExpr = ExpressionSelectItem(DefaultExpression(), alias = "default_expr")

  private fun payloadSelectItems(averageOrderSubquery: QueryExpression): List<SelectItem> =
    listOf(
      orderPayload,
      customerId,
      regionGroup,
      avgOrderAmount(averageOrderSubquery),
      asOf,
      analysisInterval,
      defaultExpr)

  private fun avgOrderAmount(averageOrderSubquery: QueryExpression) =
    ExpressionSelectItem(ScalarSubquery(averageOrderSubquery), alias = "avg_order_amount")

  private fun relatedRelationItems(): List<SelectItem> =
    listOf(
      StarSelectItem(QualifiedName("mr")),
      ExpressionSelectItem(ColumnReference(QualifiedName("tag", "value")), alias = "tag_value"),
      ExpressionSelectItem(
        ColumnReference(QualifiedName("category_pivot", "amount_sum")), alias = "channel_sales"))

  private fun hasRegionItem(regionFilterSubquery: QueryExpression): SelectItem =
    ExpressionSelectItem(ExistsExpression(regionFilterSubquery), alias = "has_region")

  private fun finalJoin(): JoinTable {
    val joinRegional =
      JoinTable(
        left = salesWithTags(),
        right = NamedTable(QualifiedName("regional_totals"), alias = RelationAlias("rt")),
        type = JoinType.INNER,
        condition =
          OnCondition(
            ComparisonExpression(
              ColumnReference(QualifiedName("s", "region")),
              ComparisonOperator.EQUAL,
              ColumnReference(QualifiedName("rt", "region")))))

    val joinOrg =
      JoinTable(
        left = joinRegional,
        right = NamedTable(QualifiedName("org_tree"), alias = RelationAlias("org")),
        type = JoinType.LEFT,
        condition =
          OnCondition(
            ComparisonExpression(
              ColumnReference(QualifiedName("org", "id")),
              ComparisonOperator.EQUAL,
              ColumnReference(QualifiedName("s", "employee_id")))))

    return JoinTable(
      left = joinOrg,
      right = matchRecognizeTable(),
      type = JoinType.LEFT,
      condition =
        OnCondition(
          ComparisonExpression(
            ColumnReference(QualifiedName("mr", "sensor_id")),
            ComparisonOperator.EQUAL,
            ColumnReference(QualifiedName("s", "sensor_id")))))
  }

  private fun salesWithTags(): JoinTable =
    JoinTable(
      left = NamedTable(QualifiedName("sales"), alias = RelationAlias("s")),
      right =
        UnnestTable(
          expressions = listOf(ColumnReference(QualifiedName("s", "tags"))),
          withOrdinality = true,
          alias = RelationAlias("tag", listOf("value", "position")),
          lateral = true),
      type = JoinType.LEFT,
      condition = null)

  private fun matchRecognizeTable(): MatchRecognizeTable =
    MatchRecognizeTable(
      input = NamedTable(QualifiedName("sensor_events"), alias = RelationAlias("e")),
      matchRecognize = matchRecognizeClause(),
      alias = RelationAlias("mr", listOf("sensor_id", "start_time", "end_time")))

  private fun matchRecognizeClause(): MatchRecognizeClause =
    MatchRecognizeClause(
      partitionBy = listOf(ColumnReference(QualifiedName("e", "sensor_id"))),
      orderBy = listOf(OrderBy(ColumnReference(QualifiedName("e", "event_time")))),
      measures =
        listOf(
          MeasureDefinition(
            expression =
              FunctionCall(
                name = QualifiedName("FIRST_VALUE"),
                arguments = listOf(ColumnReference(QualifiedName("e", "event_time")))),
            alias = "start_time"),
          MeasureDefinition(
            expression =
              FunctionCall(
                name = QualifiedName("LAST_VALUE"),
                arguments = listOf(ColumnReference(QualifiedName("e", "event_time")))),
            alias = "end_time")),
      rowsPerMatch = RowsPerMatch.ALL_ROWS_PER_MATCH,
      afterMatchSkip = AfterMatchSkip(AfterMatchSkipOption.PAST_LAST_ROW),
      pattern =
        PatternConcatenation(
          parts =
            listOf(
              PatternVariable("A"),
              PatternQuantifierExpression(
                pattern = PatternVariable("B"),
                quantifier = PatternQuantifier(min = 1, max = null, greedy = false)))),
      subsets = listOf(PatternSubset("AB", listOf("A", "B"))),
      defines =
        listOf(
          PatternDefinition(
            variable = "A",
            condition =
              ComparisonExpression(
                ColumnReference(QualifiedName("e", "event_type")),
                ComparisonOperator.EQUAL,
                StringLiteral("START"))),
          PatternDefinition(
            variable = "B",
            condition =
              ComparisonExpression(
                ColumnReference(QualifiedName("e", "event_type")),
                ComparisonOperator.NOT_EQUAL,
                StringLiteral("END")))))

  private fun categoryPivot(): PivotTable =
    PivotTable(
      source = NamedTable(QualifiedName("sales_channels"), alias = RelationAlias("sc")),
      pivot =
        PivotClause(
          forColumns = listOf(ColumnReference(QualifiedName("sc", "channel"))),
          inValues =
            listOf(
              PivotValue(StringLiteral("ONLINE"), "online"),
              PivotValue(StringLiteral("STORE"), "store")),
          aggregateItems =
            listOf(
              PivotAggregation(
                function =
                  FunctionCall(
                    name = QualifiedName("SUM"),
                    arguments = listOf(ColumnReference(QualifiedName("sc", "amount")))),
                alias = "amount_sum"))),
      alias = RelationAlias("category_pivot", listOf("channel", "amount_sum")))

  private fun whereClause(regionFilterSubquery: QueryExpression): Expression =
    BinaryExpression(
      BetweenExpression(
        value = ColumnReference(QualifiedName("s", "order_date")),
        lower = DateLiteral("2023-07-01"),
        upper = DateLiteral("2023-07-31")),
      BinaryOperator.AND,
      InSubqueryExpression(
        value = ColumnReference(QualifiedName("s", "region")), subquery = regionFilterSubquery))

  private fun groupByClause(): GroupByClause =
    GroupByClause(
      elements = listOf(GroupByExpression(ColumnReference(QualifiedName("rt", "region")))))

  private fun regionalWindowClause(): WindowClause =
    WindowClause(
      definitions =
        listOf(
          WindowDefinition(
            name = REGIONAL_WINDOW,
            specification =
              WindowSpecification(
                partitionBy = listOf(ColumnReference(QualifiedName("rt", "region"))),
                orderBy =
                  listOf(
                    OrderBy(ColumnReference(QualifiedName("s", "order_date")), SortDirection.ASC)),
                frameClause =
                  WindowFrameClause(
                    type = WindowFrameType.ROWS,
                    start = FrameBound(FrameBoundType.UNBOUNDED_PRECEDING),
                    end = FrameBound(FrameBoundType.CURRENT_ROW))))))

  private fun qualifyClause(): QualifyClause =
    QualifyClause(
      ComparisonExpression(
        FunctionCall(
          name = QualifiedName("ROW_NUMBER"),
          over = WindowSpecification(baseWindowName = REGIONAL_WINDOW)),
        ComparisonOperator.LESS_THAN_OR_EQUAL,
        NumericLiteral("10")))

  private fun orderByClauses(): List<OrderBy> =
    listOf(
      OrderBy(
        expression = ColumnReference(QualifiedName("rt", "region")), direction = SortDirection.ASC),
      OrderBy(
        expression = ColumnReference(QualifiedName("s", "order_date")),
        direction = SortDirection.DESC,
        nullOrdering = NullOrdering.NULLS_LAST))

  private fun lockingClauses(): List<LockingClause> =
    listOf(
      LockingClause(
        strength = LockingStrength.FOR_UPDATE,
        tables = listOf(QualifiedName("sales")),
        waitOption = LockWaitOption.NOWAIT))
}
