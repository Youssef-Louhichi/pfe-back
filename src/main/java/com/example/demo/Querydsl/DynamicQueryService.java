package com.example.demo.Querydsl;

import com.querydsl.sql.SQLQueryFactory;
import com.example.demo.Condition.AggregationRequest;
import com.example.demo.Condition.FilterCondition;
import com.example.demo.TableColumns.ColumnRepository;

import com.example.demo.TableColumns.TabColumn;
import com.example.demo.dto.QueryRequestDTO;
import com.example.demo.tables.DbTable;
import com.example.demo.tables.TableRepository;
import com.querydsl.core.Tuple;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;


import java.sql.Connection;
import java.sql.DriverManager;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DynamicQueryService {

    private final QueryDSLFactory queryDSLFactory;

    
    @Autowired
    private ColumnRepository columnRepository;
    
    @Autowired
    private TableRepository tableRepository;
    
    
    
    public DynamicQueryService(QueryDSLFactory queryDSLFactory) {
        this.queryDSLFactory = queryDSLFactory;
    }


    

    public List<Map<String, Object>> fetchTableDataWithCondition(QueryRequestDTO request) {
        
        DbTable t = tableRepository.findById(request.getTableId()).orElseThrow(() -> new RuntimeException("Table not found"));
        
        String dbUrl = "jdbc:mysql://localhost:3306/" + t.getDatabase().getName(); 
        String username = t.getDatabase().getConnexion().getUsername();
        String password = t.getDatabase().getConnexion().getPassword();
        String driver = "com.mysql.cj.jdbc.Driver";
        
        SQLQueryFactory queryFactory = queryDSLFactory.createSQLQueryFactory(dbUrl, username, password, driver);
        
        try (Connection conn = DriverManager.getConnection(dbUrl, username, password)) {
            
            List<TabColumn> columns = columnRepository.findAllById(request.getColumnId());
            
            List<Expression<?>> selectExpressions = new ArrayList<>();
            Map<String, Expression<?>> aliasMapping = new HashMap<>();

            // Add normal columns to SELECT
            for (TabColumn column : columns) {
                Expression<String> colExpr = Expressions.stringPath(column.getName());
                selectExpressions.add(colExpr);
                aliasMapping.put(column.getName(), colExpr);
            }

            // Add aggregate functions
            addAggregations(request, selectExpressions, aliasMapping);

            if (selectExpressions.isEmpty()) {
                throw new IllegalArgumentException("No columns found for table: " + t.getName());
            }

            // Define table reference
            RelationalPath<?> qTable = new RelationalPathBase<>(Object.class, t.getName(), null, "");

            // Build query
            SQLQuery<Tuple> query = queryFactory.select(selectExpressions.toArray(new Expression<?>[0])).from(qTable);

            // Add dynamic WHERE conditions
            addDynamicFilters(query, request.getFilters());

            // Handle GROUP BY if provided
            if (request.getGroupByColumns() != null && !request.getGroupByColumns().isEmpty()) {
                List<Expression<?>> groupByExpressions = new ArrayList<>();
                for (Long groupByColumnId : request.getGroupByColumns()) {
                    TabColumn groupByColumn = columnRepository.findById(groupByColumnId).orElse(null);
                    if (groupByColumn != null) {
                        groupByExpressions.add(Expressions.stringPath(groupByColumn.getName()));
                    }
                }
                if (!groupByExpressions.isEmpty()) {
                    query.groupBy(groupByExpressions.toArray(new Expression<?>[0]));
                }
            }

            // Execute query
            List<Tuple> result = query.fetch();
            
            System.out.println("Query executed successfully. Rows fetched: " + result.size());
            System.out.println(result.toString());
            
            List<Map<String, Object>> jsonResponse = new ArrayList<>();
            for (Tuple tuple : result) {
                
                Map<String, Object> row = new HashMap<>();
                for (Map.Entry<String, Expression<?>> entry : aliasMapping.entrySet()) {
                    row.put(entry.getKey(), tuple.get(entry.getValue()));
                }
                jsonResponse.add(row);
            }

            return jsonResponse;

        } catch (Exception e) {
            System.err.println("Error executing query: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }


    private void addDynamicFilters(SQLQuery<?> query, List<FilterCondition> filters) {
        if (filters == null || filters.isEmpty()) return;

        for (FilterCondition filter : filters) {
            String columnName = filter.getColumnName();
            String operator = filter.getOperator().toLowerCase();
            String value = filter.getValue();

            Expression<?> column = Expressions.stringPath(columnName);

            switch (operator) {
                case "=":
                    query.where(Expressions.booleanTemplate("{0} = {1}", column, value));
                    break;
                case "!=":
                    query.where(Expressions.booleanTemplate("{0} != {1}", column, value));
                    break;
                case "like":
                    query.where(Expressions.booleanTemplate("{0} LIKE {1}", column, "%" + value + "%"));
                    break;
                case ">":
                    query.where(Expressions.booleanTemplate("{0} > {1}", column, value));
                    break;
                case "<":
                    query.where(Expressions.booleanTemplate("{0} < {1}", column, value));
                    break;
                case ">=":
                    query.where(Expressions.booleanTemplate("{0} >= {1}", column, value));
                    break;
                case "<=":
                    query.where(Expressions.booleanTemplate("{0} <= {1}", column, value));
                    break;
                case "in":
                    query.where(Expressions.booleanTemplate("{0} IN ({1})", column, value));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported operator: " + operator);
            }
        }
    }
    private void addAggregations(QueryRequestDTO request, List<Expression<?>> selectExpressions, Map<String, Expression<?>> aliasMapping) {
        if (request.getAggregations() != null) {
            for (AggregationRequest agg : request.getAggregations()) {
                TabColumn column = columnRepository.findById(agg.getColumnId()).orElse(null);
                if (column != null) {
                    Expression<?> aggregateExpr = null;
                    String alias = agg.getFunction().toLowerCase() + "_" + column.getName();

                    switch (agg.getFunction().toUpperCase()) {
                        case "MAX":
                            aggregateExpr = SQLExpressions.max(Expressions.numberPath(Double.class, column.getName()));
                            break;
                        case "MIN":
                            aggregateExpr = SQLExpressions.min(Expressions.numberPath(Double.class, column.getName()));
                            break;
                        case "AVG":
                            aggregateExpr = SQLExpressions.avg(Expressions.numberPath(Double.class, column.getName()));
                            break;
                        case "COUNT":
                            aggregateExpr = SQLExpressions.count(Expressions.stringPath(column.getName()));
                            break;
                        case "SUM":
                            aggregateExpr = SQLExpressions.sum(Expressions.numberPath(Double.class, column.getName()));
                            break;
                        default:
                            throw new IllegalArgumentException("Unsupported aggregation function: " + agg.getFunction());
                    }

                    if (aggregateExpr != null) {
                        Expression<?> aliasedExpr = Expressions.as(aggregateExpr, alias); // Use Expressions.as()
                        selectExpressions.add(aliasedExpr);
                        aliasMapping.put(alias, aliasedExpr); // Store the aliased expression
                    }
                }
            }
        }
    }




}