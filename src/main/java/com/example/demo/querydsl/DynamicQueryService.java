package com.example.demo.querydsl;

import com.querydsl.sql.SQLQueryFactory;


import com.example.demo.condition.AggregationRequest;
import com.example.demo.condition.FilterCondition;
import com.example.demo.condition.JoinCondition;
import com.example.demo.condition.OrderBy;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;
import com.example.demo.condition.AggregationRequest;
import com.example.demo.condition.FilterCondition;
import com.example.demo.condition.JoinCondition;
import com.example.demo.tablecolumns.ColumnRepository;

import com.example.demo.tablecolumns.TabColumn;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.example.demo.connexions.DatabaseType;
import com.example.demo.database.Database;
import com.example.demo.database.DatabaseRepository;
import com.example.demo.dto.DeleteRequestDTO;
import com.example.demo.dto.InsertRequestDTO;
import com.example.demo.dto.JoinRequestDTO;
import com.example.demo.dto.QueryRequestDTO;

import com.example.demo.tablecolumns.ColumnRepository;
import com.example.demo.tablecolumns.TabColumn;

import com.example.demo.dto.UpdateRequestDTO;
import com.example.demo.having.HavingCondition;
import com.example.demo.requete.Requete;
import com.example.demo.requete.RequeteRepository;
import com.example.demo.tablecolumns.ColumnRepository;
import com.example.demo.tablecolumns.TabColumn;

import com.example.demo.tables.DbTable;
import com.example.demo.tables.TableRepository;
import com.example.demo.users.User;
import com.example.demo.users.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.Tuple;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Ops;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class DynamicQueryService {

    private final QueryDSLFactory queryDSLFactory;
    
    @Autowired
    private ColumnRepository columnRepository;
    
    @Autowired
    private TableRepository tableRepository;
    
    
    @Autowired
    private RequeteRepository requeteRepository ;
    
    @Autowired
    private DatabaseRepository databaseRepository ;
    
    @Autowired
    private UserRepository userRepository ;
    
    
    public DynamicQueryService(QueryDSLFactory queryDSLFactory) {
        this.queryDSLFactory = queryDSLFactory;
    }
    
  

    public List<Map<String, Object>> fetchTableDataWithCondition(QueryRequestDTO request) {
    	System.out.println("zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz");
        // Validate request
        if (request.getTableId() == null || request.getTableId().isEmpty()) {
            throw new IllegalArgumentException("At least one table ID must be provided");
        }
        
        // Get primary table (first in the list)
        Long primaryTableId = request.getTableId().get(0);
        DbTable primaryTable = tableRepository.findById(primaryTableId)
                .orElseThrow(() -> new RuntimeException("Primary table not found with ID: " + primaryTableId));
        
        // Get database connection details
        DatabaseType dbType = primaryTable.getDatabase().getDbtype();
        String dbUrl, driver;
        
        if (dbType == DatabaseType.MySQL) {
            dbUrl = "jdbc:mysql://" + primaryTable.getDatabase().getConnexion().getHost() + ":" 
                  + primaryTable.getDatabase().getConnexion().getPort() + "/" 
                  + primaryTable.getDatabase().getName();
            driver = "com.mysql.cj.jdbc.Driver";
        } else if (dbType == DatabaseType.Oracle) {
            dbUrl = "jdbc:oracle:thin:@" + primaryTable.getDatabase().getConnexion().getHost() + ":" 
                  + primaryTable.getDatabase().getConnexion().getPort() + ":" 
                  + primaryTable.getDatabase().getName();
            driver = "oracle.jdbc.OracleDriver";
        } else {
            throw new RuntimeException("Unsupported database type: " + dbType);
        }

        String username = primaryTable.getDatabase().getConnexion().getUsername();
        String password = primaryTable.getDatabase().getConnexion().getPassword();
        
        SQLQueryFactory queryFactory = queryDSLFactory.createSQLQueryFactory(dbUrl, username, password, driver);

        try (Connection conn = DriverManager.getConnection(dbUrl, username, password)) {
            // Get list of requested tables and build a mapping
            List<DbTable> tables = new ArrayList<>();
            Map<Long, DbTable> tableIdMap = new HashMap<>();
            
            for (Long tableId : request.getTableId()) {
                DbTable table = tableRepository.findById(tableId)
                        .orElseThrow(() -> new RuntimeException("Table not found with ID: " + tableId));
                tables.add(table);
                tableIdMap.put(tableId, table);
            }

            // Create path objects for all tables - use actual table names
            Map<Long, RelationalPath<?>> tablePaths = new HashMap<>();
            for (DbTable table : tables) {
                // Use the actual table name for the path
            	RelationalPathBase<?> path = new RelationalPathBase<>(
            		    Object.class, 
            		    table.getName(),  
            		    table.getName(), 
            		    table.getName()             
            		);
                tablePaths.put(table.getId(), path);
                System.out.println("Added table path: " + table.getName() + " for ID: " + table.getId());
            }
            
            // Get columns with their tables
            List<TabColumn> columns = columnRepository.findAllById(request.getColumnId());
            
            // Prepare select expressions and alias mapping
            List<Expression<?>> selectExpressions = new ArrayList<>();
            Map<String, Expression<?>> aliasMapping = new HashMap<>();

            for (TabColumn column : columns) {
                String tableName = column.getTable().getName();
                String columnName = column.getName();
                
                // Use template expressions to create qualified column references
                Expression<?> colExpr = Expressions.template(Object.class, "{0}.{1}", 
                    Expressions.template(Object.class, tableName),
                    Expressions.template(Object.class, columnName)
                );
                
                // Create alias for column
                String columnAlias = tableName + "_" + columnName;
                Expression<?> aliasedExpr = Expressions.as(colExpr, columnAlias);
                
                selectExpressions.add(aliasedExpr);
                aliasMapping.put(columnAlias, aliasedExpr);
            }

            // Add aggregations
            addAggregations(request, selectExpressions, aliasMapping, tables);

            if (selectExpressions.isEmpty()) {
                throw new IllegalArgumentException("No columns selected for query");
            }

            // Start building query with primary table
            RelationalPath<?> primaryPath = tablePaths.get(primaryTableId);
            SQLQuery<Tuple> query = queryFactory.select(selectExpressions.toArray(new Expression<?>[0])).from(primaryPath);
            
            
            boolean hasJoins = (request.getJoinRequest() != null && 
                    request.getJoinRequest().getJoinConditions() != null && 
                    !request.getJoinRequest().getJoinConditions().isEmpty()) || 
                   (request.getReq() != null && 
                    request.getReq().getJoinConditions() != null && 
                    !request.getReq().getJoinConditions().isEmpty());
            
            
            if (hasJoins) {
                // First try to use joinConditions from req
                if (request.getReq() != null && 
                    request.getReq().getJoinConditions() != null && 
                    !request.getReq().getJoinConditions().isEmpty()) {
                    
                    addJoinsFromList(query, tablePaths, request.getReq().getJoinConditions());
                } 
                // If no joins in req, try joinRequest
                else if (request.getJoinRequest() != null && 
                         request.getJoinRequest().getJoinConditions() != null) {
                    
                    addJoinsFromList(query, tablePaths, request.getJoinRequest().getJoinConditions());
                }
            }
            
            // Add filters
            addDynamicFilters(query, request.getFilters(), tables);
            
            // Add group by
            if (request.getGroupByColumns() != null && !request.getGroupByColumns().isEmpty()) {
                List<Expression<?>> groupByExpressions = new ArrayList<>();
                for (Long groupByColumnId : request.getGroupByColumns()) {
                    TabColumn groupByColumn = columnRepository.findById(groupByColumnId).orElse(null);
                    if (groupByColumn != null) {
                        RelationalPath<?> tablePath = tablePaths.get(groupByColumn.getTable().getId());
                        if (tablePath != null) {
                            groupByExpressions.add(Expressions.path(Object.class, tablePath, groupByColumn.getName()));
                        }
                    }
                }
                if (!groupByExpressions.isEmpty()) {
                    query.groupBy(groupByExpressions.toArray(new Expression<?>[0]));
                }
            }
            
         // Add having conditions
            if (request.getHavingConditions() != null && !request.getHavingConditions().isEmpty()) {
                for (HavingCondition having : request.getHavingConditions()) {
                    TabColumn column = columnRepository.findById(having.getColumnId()).orElse(null);
                    if (column == null) continue;

                    RelationalPath<?> tablePath = tablePaths.get(column.getTable().getId());
                    if (tablePath == null) continue;

                    Expression<?> colExpr = Expressions.path(Object.class, tablePath, column.getName());
                    Expression<? extends Number> aggExpr = null;
                    
                    if (having.getFunction() == null || having.getFunction().trim().isEmpty()) {
                        // No aggregation function - use the column directly
                        aggExpr = (Expression<? extends Number>) colExpr;
                    } else {

                    switch (having.getFunction().toLowerCase()) {
                        case "count":
                            aggExpr = Expressions.numberTemplate(Long.class, "count({0})", colExpr);
                            break;
                        case "sum":
                            aggExpr = Expressions.numberTemplate(Double.class, "sum({0})", colExpr);
                            break;
                        case "avg":
                            aggExpr = Expressions.numberTemplate(Double.class, "avg({0})", colExpr);
                            break;
                        case "min":
                            aggExpr = Expressions.numberTemplate(Double.class, "min({0})", colExpr);
                            break;
                        case "max":
                            aggExpr = Expressions.numberTemplate(Double.class, "max({0})", colExpr);
                            break;
                        default:
                            continue;
                    }
                    }
                    
                    
                    if (having.isTest()) {
                        Object value = having.getValue();
                        Requete subqueryRequete;
                        if (value instanceof LinkedHashMap) {
                            ObjectMapper mapper = new ObjectMapper();
                            mapper.registerModule(new JavaTimeModule()); // Add this
                            subqueryRequete = mapper.convertValue(value, Requete.class);
                        } else if (value instanceof Requete) {
                            subqueryRequete = (Requete) value;
                        } else {
                            throw new IllegalArgumentException("Unsupported value type: " + value.getClass().getName());
                        }
                        // Create a subquery using the Requete object
                        SQLQuery<?> subquery = createSubquery(queryFactory, subqueryRequete, tablePaths, tableIdMap);
                        
                        // Apply the having condition with the subquery
                        applyHavingWithSubquery(query, having, aggExpr, subquery);
                        System.out.println(aggExpr);
                    } else {
                    		System.out.println("eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
                    // Apply having condition using the correct type
                    		Object rawValue = having.getValue();
                    		Number value;

                    		if (rawValue instanceof Number) {
                    		    value = (Number) rawValue;
                    		} else if (rawValue instanceof String) {
                    		    try {
                    		        value = Double.parseDouble((String) rawValue);
                    		    } catch (NumberFormatException e) {
                    		        throw new IllegalArgumentException("Value is not a valid number: " + rawValue);
                    		    }
                    		} else {
                    		    throw new IllegalArgumentException("Unsupported value type: " + rawValue.getClass().getName());
                    		}
                    
                    if (having.getFunction() == null || having.getFunction().trim().isEmpty()) {
                    	
                    	 applySimpleHavingCondition(query, colExpr, having.getOperator(), value);
                    	
                    }
                    else 
                    {
                    // Determine the specific number type based on the aggregation function
                    switch (having.getFunction().toLowerCase()) {
                        case "count":
                            // For count, we work with Long values
                            Long longValue = value.longValue();
                            NumberExpression<Long> longExpr = (NumberExpression<Long>) aggExpr;
                            
                            switch (having.getOperator()) {
                                case ">": query.having(longExpr.gt(longValue)); break;
                                case "<": query.having(longExpr.lt(longValue)); break;
                                case "=": query.having(longExpr.eq(longValue)); break;
                                case ">=": query.having(longExpr.goe(longValue)); break;
                                case "<=": query.having(longExpr.loe(longValue)); break;
                            }
                            break;
                            
                        case "sum":
                        case "avg":
                            // For sum and avg, we work with Double values
                            Double doubleValue = value.doubleValue();
                            NumberExpression<Double> doubleExpr = (NumberExpression<Double>) aggExpr;
                            
                            switch (having.getOperator()) {
                                case ">": query.having(doubleExpr.gt(doubleValue)); break;
                                case "<": query.having(doubleExpr.lt(doubleValue)); break;
                                case "=": query.having(doubleExpr.eq(doubleValue)); break;
                                case ">=": query.having(doubleExpr.goe(doubleValue)); break;
                                case "<=": query.having(doubleExpr.loe(doubleValue)); break;
                            }
                            break;
                    } }
                } }
            }
           
         // Add order by
         // Add order by
            if (request.getOrderBy() != null && !request.getOrderBy().isEmpty()) {
                List<OrderBy> orderByList = request.getOrderBy();
                for (OrderBy orderBy : orderByList) {
                    Long colId = orderBy.getColId(); // Updated to use columnId
                    String orderType = orderBy.getOrderType();

                    if (colId != null) {
                        // Find the column by its ID
                        TabColumn orderColumn = columns.stream()
                                .filter(column -> column.getId().equals(colId))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Column not found for Order By: " + colId));

                        // Get the table of this column
                        DbTable orderTable = orderColumn.getTable();
                        RelationalPath<?> orderTablePath = tablePaths.get(orderTable.getId());

                        if (orderTablePath != null) {
                            // Create a column reference for the order by column
                            ComparableExpression<?> orderByExpr = Expressions.comparablePath(Comparable.class, orderTablePath, orderColumn.getName());

                            // Apply the order by clause
                            if ("ASC".equalsIgnoreCase(orderType)) {
                                query.orderBy(orderByExpr.asc());
                            } else if ("DESC".equalsIgnoreCase(orderType)) {
                                query.orderBy(orderByExpr.desc());
                            } else {
                                throw new IllegalArgumentException("Invalid order type: " + orderType);
                            }
                        } else {
                            throw new RuntimeException("Invalid table for column: " + colId);
                        }
                    }
                }
            }
            
            if (request.getLimit() != null && request.getLimit() > 0) {
                query.limit(request.getLimit());
/*
                if (request.getOffset() != null && request.getOffset() >= 0) {
                    query.offset(request.getOffset());
                }*/
            }


            System.out.println("Final query: " + query.toString());
            
            // Execute query and process results
            List<Tuple> result = query.fetch();
            System.out.println("Query executed successfully. Rows fetched: " + result.size());
            Requete req = new Requete();
            req.setSentAt(LocalDateTime.now());
           // req.setContent(query.toString());
           /* req.setSender(request.getReq().getSender());
            req.setTable(primaryTable);
            req.setJoinConditions(request.getJoinRequest().getJoinConditions());
            req.setAggregation(request.getAggregations());
            req.setFilters(request.getFilters());
            req.setTableId(request.getTableId());
            req.setColumnId(request.getColumnId());
            req.setGroupByColumns(request.getGroupByColumns());
            requeteRepository.save( req);*/
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
    
    
   
    
    private void applySimpleHavingCondition(SQLQuery<Tuple> query, Expression<?> colExpr, String operator, Number value) {
        // Determine the data type of the column expression
        // Check if the value is an integer type first
        if (value instanceof Integer || value instanceof Long) {
            // For integer columns, work with Long values
            Long longValue = value.longValue();
            NumberExpression<Long> numExpr = Expressions.numberTemplate(Long.class, "{0}", colExpr);
            
            switch (operator) {
                case ">": query.having(numExpr.gt(longValue)); break;
                case "<": query.having(numExpr.lt(longValue)); break;
                case "=": query.having(numExpr.eq(longValue)); break;
                case ">=": query.having(numExpr.goe(longValue)); break;
                case "<=": query.having(numExpr.loe(longValue)); break;
                case "!=": query.having(numExpr.ne(longValue)); break;
                default:
                    throw new IllegalArgumentException("Unsupported operator: " + operator);
            }
        } else {
            // For decimal columns, work with Double values
            Double doubleValue = value.doubleValue();
            NumberExpression<Double> numExpr = Expressions.numberTemplate(Double.class, "{0}", colExpr);
            
            switch (operator) {
                case ">": query.having(numExpr.gt(doubleValue)); break;
                case "<": query.having(numExpr.lt(doubleValue)); break;
                case "=": query.having(numExpr.eq(doubleValue)); break;
                case ">=": query.having(numExpr.goe(doubleValue)); break;
                case "<=": query.having(numExpr.loe(doubleValue)); break;
                case "!=": query.having(numExpr.ne(doubleValue)); break;
                default:
                    throw new IllegalArgumentException("Unsupported operator: " + operator);
            }
        }
    }
    private <T> SQLQuery<T> createSubquery(SQLQueryFactory queryFactory, Requete subqueryRequete, 
            Map<Long, RelationalPath<?>> tablePaths, 
            Map<Long, DbTable> tableIdMap) {
        // Get the primary table for the subquery
    	
    	 for (Long tableId : subqueryRequete.getTableId()) {
    	        if (!tablePaths.containsKey(tableId)) {
    	            // Fetch the table from tableIdMap or database
    	            DbTable table = tableIdMap.getOrDefault(tableId, tableRepository.findById(tableId)
    	                    .orElseThrow(() -> new IllegalArgumentException("Table not found for ID: " + tableId)));

    	            // Add the table to tableIdMap if not already present
    	            tableIdMap.putIfAbsent(tableId, table);
    	            System.out.println("Added table to tableIdMap: " + table.getName() + " for ID: " + tableId);

    	            // Create and add the table path to tablePaths
    	            RelationalPathBase<?> path = new RelationalPathBase<>(
    	                Object.class, 
    	                table.getName(),  
    	                table.getName(), 
    	                table.getName()
    	            );
    	            tablePaths.put(tableId, path);
    	            System.out.println("Added table path: " + table.getName() + " for ID: " + tableId);
    	        }
    	    }
        Long primaryTableId = subqueryRequete.getTableId().get(0);
      
        RelationalPath<?> primaryPath = tablePaths.get(primaryTableId);
        
        if (primaryPath == null) {
            throw new IllegalArgumentException("Primary table not found for ID: " + primaryTableId);
        }

        // Create subquery select expressions
        List<Expression<?>> selectExpressions = new ArrayList<>();

        // Handle columns
        if (subqueryRequete.getColumnId() != null && !subqueryRequete.getColumnId().isEmpty()) {
            for (Long columnId : subqueryRequete.getColumnId()) {
                TabColumn column = columnRepository.findById(columnId).orElse(null);
                if (column != null) {
                    RelationalPath<?> tablePath = tablePaths.get(column.getTable().getId());
                    if (tablePath != null) {
                        Expression<?> pathExpr = Expressions.path(Object.class, tablePath, column.getName());
                        selectExpressions.add(pathExpr);
                    }
                }
            }
        }

        // Handle aggregations with possible nesting
        if (subqueryRequete.getAggregation() != null && !subqueryRequete.getAggregation().isEmpty()) {
        	System.out.println("hedha 1");
            for (AggregationRequest agg : subqueryRequete.getAggregation()) {
                TabColumn column = columnRepository.findById(agg.getColumnId()).orElse(null);
                if (column != null) {
                	System.out.println("column mawjouda");
                    RelationalPath<?> tablePath = tablePaths.get(column.getTable().getId());
                    System.out.println(column.getTable().getId());
                    System.out.println(tablePaths.get(column.getTable().getId()));
                    if (tablePath != null) {
                        // Create base column path
                        NumberPath<Double> numberPath = Expressions.numberPath(Double.class, tablePath, column.getName());
                        System.out.println("hedha 1.5");
                        // Special handling for multiple aggregation functions
                        if (agg.getfunctionagg().size() > 1) {
                        	System.out.println("hedha 3");
                            // Create a SQL template for nested aggregations
                            StringBuilder templateStr = new StringBuilder();
                            
                            // Build the nested function string: MAX(AVG(column))
                            for (String func : agg.getfunctionagg()) {
                                templateStr.append(func.toUpperCase()).append("(");
                            }
                            
                            // Add the column reference
                            templateStr.append("{0}");
                            
                            // Close all function parentheses
                            for (int i = 0; i < agg.getfunctionagg().size(); i++) {
                                templateStr.append(")");
                            }
                            
                            // Create a template expression with the nested functions
                            Expression<?> aggregateExpr = Expressions.template(
                                Double.class, 
                                templateStr.toString(),
                                numberPath
                            );
                            
                            selectExpressions.add(aggregateExpr);
                        } 
                        // Simple case - single aggregation function
                        else if (agg.getfunctionagg().size() == 1) {
                        	System.out.println("hedha 2");
                            String func = agg.getfunctionagg().get(0).toUpperCase();
                            Expression<?> aggregateExpr = null;
                            
                            switch (func) {
                                case "MAX":
                                    aggregateExpr = SQLExpressions.max(numberPath);
                                    break;
                                case "MIN":
                                    aggregateExpr = SQLExpressions.min(numberPath);
                                    break;
                                case "AVG":
                                    aggregateExpr = SQLExpressions.avg(numberPath);
                                    break;
                                case "SUM":
                                    aggregateExpr = SQLExpressions.sum(numberPath);
                                    break;
                                case "COUNT":
                                    aggregateExpr = SQLExpressions.count(numberPath);
                                    break;
                                default:
                                    throw new IllegalArgumentException("Unsupported aggregation function: " + func);
                            }
                            
                            if (aggregateExpr != null) {
                                selectExpressions.add(aggregateExpr);
                            }
                        }
                    }
                    else {System.out.println("famech");}
                }
            }
        }
        else {System.out.println("hedha howa");}

        // If no expressions, add a default one
        if (selectExpressions.isEmpty()) {
            selectExpressions.add(Expressions.constant(1)); // Fallback to SELECT 1
        }

        // Start building subquery
        SQLQuery<T> subquery = (SQLQuery<T>) queryFactory.select(selectExpressions.toArray(new Expression<?>[0]))
                              .from(primaryPath);

        // Add joins if present
        if (subqueryRequete.getJoinConditions() != null && !subqueryRequete.getJoinConditions().isEmpty()) {
            addJoinsFromList(subquery, tablePaths, subqueryRequete.getJoinConditions());
            System.out.println("yes joinnnnnnnn");
        }
        else {System.out.println("no joinnnnnnnn");}

        // Add filters if present
        if (subqueryRequete.getFilters() != null && !subqueryRequete.getFilters().isEmpty()) {
            List<DbTable> tables = new ArrayList<>();
            for (Long tableId : subqueryRequete.getTableId()) {
                DbTable table = tableIdMap.get(tableId);
                if (table == null) {
                    System.out.println("Table ID " + tableId + " not found in tableIdMap");
                } else {
                    tables.add(table);
                }
            }
            System.out.println("Tables after filtering: " + tables.size());
            addDynamicFilters(subquery, subqueryRequete.getFilters(), tables);
        }

        // Add group by if present
        if (subqueryRequete.getGroupByColumns() != null && !subqueryRequete.getGroupByColumns().isEmpty()) {
            List<Expression<?>> groupByExpressions = new ArrayList<>();
            for (Long groupByColumnId : subqueryRequete.getGroupByColumns()) {
                TabColumn groupByColumn = columnRepository.findById(groupByColumnId).orElse(null);
                if (groupByColumn != null) {
                    RelationalPath<?> tablePath = tablePaths.get(groupByColumn.getTable().getId());
                    if (tablePath != null) {
                        groupByExpressions.add(Expressions.path(Object.class, tablePath, groupByColumn.getName()));
                    }
                }
            }
            if (!groupByExpressions.isEmpty()) {
                subquery.groupBy(groupByExpressions.toArray(new Expression<?>[0]));
            }
        }
        
        
     // Add HAVING conditions to the subquery
        if (subqueryRequete.getHavingConditions() != null && !subqueryRequete.getHavingConditions().isEmpty()) {
            for (HavingCondition having : subqueryRequete.getHavingConditions()) {
                TabColumn column = columnRepository.findById(having.getColumnId()).orElse(null);
                if (column == null) continue;

                DbTable table = tableIdMap.get(column.getTable().getId());
                if (table == null) continue;

                // Build column expression
                Expression<?> colExpr = Expressions.template(Object.class, "{0}.{1}", 
                    Expressions.template(Object.class, table.getName()), 
                    Expressions.template(Object.class, column.getName())
                );

                Expression<? extends Number> aggExpr = null;

                if (having.getFunction() == null || having.getFunction().trim().isEmpty()) {
                	
                    // No aggregation function - use the column directly
                    aggExpr = (Expression<? extends Number>) colExpr;
                } else {
                	System.out.println("function in having 1");
                    switch (having.getFunction().toLowerCase()) {
                        case "count":
                            aggExpr = Expressions.numberTemplate(Long.class, "count({0})", colExpr);
                            System.out.println("function in having 2");
                            break;
                        case "sum":
                            aggExpr = Expressions.numberTemplate(Double.class, "sum({0})", colExpr);
                            break;
                        case "avg":
                            aggExpr = Expressions.numberTemplate(Double.class, "avg({0})", colExpr);
                            break;
                        case "min":
                            aggExpr = Expressions.numberTemplate(Double.class, "min({0})", colExpr);
                            break;
                        case "max":
                            aggExpr = Expressions.numberTemplate(Double.class, "max({0})", colExpr);
                            break;
                        default:
                            continue;
                    }
                }

                if (having.isTest()) {
                    Object value = having.getValue();
                    Requete subqueryRequete1;
                    if (value instanceof LinkedHashMap) {
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.registerModule(new JavaTimeModule());
                        subqueryRequete1 = mapper.convertValue(value, Requete.class);
                    } else if (value instanceof Requete) {
                        subqueryRequete1 = (Requete) value;
                        System.out.println("yes instance");
                    } else {
                        throw new IllegalArgumentException("Unsupported value type: " + value.getClass().getName());
                    }

                    // Create a nested subquery using the Requete object
                    SQLQuery<?> subquery1 = createSubquery(queryFactory, subqueryRequete1, tablePaths, tableIdMap);
                    System.out.println(subquery1.toString());
                    // Apply the HAVING condition with the subquery
                    applyHavingWithSubquery(subquery, having, aggExpr, subquery1);
                } else {
                	System.out.println("exexexe");
                	System.out.println(aggExpr);
                    // Apply a direct HAVING condition
                    Object rawValue = having.getValue();
                    Number value;

                    if (rawValue instanceof Number) {
                        value = (Number) rawValue;
                    } else if (rawValue instanceof String) {
                        try {
                            value = Double.parseDouble((String) rawValue);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Value is not a valid number: " + rawValue);
                        }
                    } else {
                        throw new IllegalArgumentException("Unsupported value type: " + rawValue.getClass().getName());
                    }

                    // Apply the HAVING condition directly
                    applySimpleHavingCondition2(subquery, aggExpr, having.getOperator(), value);
                }
            }
        }


        return subquery;
    }
    
 // Overloaded method for generic SQLQuery<?>
    private <T> void applySimpleHavingCondition2(SQLQuery<T> query, Expression<?> colExpr, String operator, Number value) {
    	System.out.println("using") ;
        if (value instanceof Integer || value instanceof Long) {
            // For integer columns, work with Long values
            Long longValue = value.longValue();
            NumberExpression<Long> numExpr = Expressions.numberTemplate(Long.class, "{0}", colExpr);

            switch (operator) {
                case ">": query.having(numExpr.gt(longValue)); break;
                case "<": query.having(numExpr.lt(longValue)); break;
                case "=": query.having(numExpr.eq(longValue)); break;
                case ">=": query.having(numExpr.goe(longValue)); break;
                case "<=": query.having(numExpr.loe(longValue)); break;
                case "!=": query.having(numExpr.ne(longValue)); break;
                default:
                    throw new IllegalArgumentException("Unsupported operator: " + operator);
            }
        } else {
            // For decimal columns, work with Double values
            Double doubleValue = value.doubleValue();
            NumberExpression<Double> numExpr = Expressions.numberTemplate(Double.class, "{0}", colExpr);

            switch (operator) {
                case ">": query.having(numExpr.gt(doubleValue)); break;
                case "<": query.having(numExpr.lt(doubleValue)); break;
                case "=": query.having(numExpr.eq(doubleValue)); break;
                case ">=": query.having(numExpr.goe(doubleValue)); break;
                case "<=": query.having(numExpr.loe(doubleValue)); break;
                case "!=": query.having(numExpr.ne(doubleValue)); break;
                default:
                    throw new IllegalArgumentException("Unsupported operator: " + operator);
            }
        }
    }
    
private <T extends Number> void applyHavingWithSubquery(SQLQuery<?> query, 
                             HavingCondition having, 
                             Expression<T> aggExpr, 
                             SQLQuery<?> subquery) {
// Default to handling a single value result if no specific comparator provided
String comparator = having.getSubqueryComparator();
if (comparator == null) {
comparator = "="; // Default comparator
}

switch (comparator.toUpperCase()) {
case "IN":
query.having(Expressions.booleanTemplate("{0} IN ({1})", aggExpr, subquery));
break;
case "NOT IN":
query.having(Expressions.booleanTemplate("{0} NOT IN ({1})", aggExpr, subquery));
break;
case "=":
case "==":
query.having(Expressions.booleanTemplate("{0} = ({1})", aggExpr, subquery));
break;
case ">":
query.having(Expressions.booleanTemplate("{0} > ({1})", aggExpr, subquery));
break;
case "<":
query.having(Expressions.booleanTemplate("{0} < ({1})", aggExpr, subquery));
break;
case ">=":
query.having(Expressions.booleanTemplate("{0} >= ({1})", aggExpr, subquery));
break;
case "<=":
query.having(Expressions.booleanTemplate("{0} <= ({1})", aggExpr, subquery));
break;
case "ANY":
case "SOME":
// For operators that need a comparison operator specified
String op = having.getOperator();
if (op == null || op.isEmpty()) {
op = "="; // Default operator
}
query.having(Expressions.booleanTemplate("{0} " + op + " ANY ({1})", aggExpr, subquery));
break;
case "ALL":
// For ALL, we also need a comparison operator
String opAll = having.getOperator();
if (opAll == null || opAll.isEmpty()) {
opAll = "="; // Default operator
}
query.having(Expressions.booleanTemplate("{0} " + opAll + " ALL ({1})", aggExpr, subquery));
break;
default:
// Default to = for unknown comparators
query.having(Expressions.booleanTemplate("{0} = ({1})", aggExpr, subquery));
}
}

    
    private void addJoinsFromList(SQLQuery<?> query, Map<Long, RelationalPath<?>> tablePaths, 
            List<JoinCondition> joinConditions) {
for (JoinCondition joinCondition : joinConditions) {
RelationalPath<?> firstTablePath = tablePaths.get(joinCondition.getFirstTableId());
RelationalPath<?> secondTablePath = tablePaths.get(joinCondition.getSecondTableId());
System.out.println("1");
if (firstTablePath == null || secondTablePath == null) {
throw new RuntimeException("One of the tables in the join condition is missing. First table ID: " + 
 joinCondition.getFirstTableId() + ", Second table ID: " + joinCondition.getSecondTableId());
}

String firstTable = firstTablePath.getMetadata().getName();
String secondTable = secondTablePath.getMetadata().getName();

System.out.println("Joining tables: " + firstTable + " and " + secondTable);
System.out.println("2");
// Use properly qualified column references with appropriate operators
BooleanExpression joinOnCondition = Expressions.booleanTemplate(
"{0}.{1} = {2}.{3}", 
Expressions.template(Object.class, firstTable),
Expressions.template(Object.class, joinCondition.getFirstColumnName()),
Expressions.template(Object.class, secondTable),
Expressions.template(Object.class, joinCondition.getSecondColumnName())
);
System.out.println(joinCondition.getFirstColumnName());
System.out.println(firstTable);
System.out.println(joinCondition.getSecondColumnName());
System.out.println(secondTable);

// Apply join type
switch (joinCondition.getJoinType().toUpperCase()) {
case "INNER":
 query.innerJoin(secondTablePath).on(joinOnCondition);
 System.out.print("okkkkk");
 break;
case "LEFT":
 query.leftJoin(secondTablePath).on(joinOnCondition);
 break;
case "RIGHT":
 query.rightJoin(secondTablePath).on(joinOnCondition);
 break;
default:
 throw new IllegalArgumentException("Unsupported join type: " + joinCondition.getJoinType());
}
}
}

    
/*
    private void addDynamicFilters(SQLQuery<?> query, List<FilterCondition> filters, List<DbTable> tables) {
        if (filters == null || filters.isEmpty()) {
            System.out.println("No filters applied.");
            return;
        }

        Map<String, DbTable> tableNameMap = tables.stream()
            .collect(Collectors.toMap(DbTable::getName, t -> t));

        for (FilterCondition filter : filters) {
            System.out.println("Filter received -> Column: " + filter.getColumnName() + 
                               ", Operator: " + filter.getOperator() + 
                               ", Value: " + filter.getValue());
            
            // Handle qualified column names (table.column format)
            String columnName = filter.getColumnName();
            String tableName = null;
            
            if (columnName.contains(".")) {
                String[] parts = columnName.split("\\.", 2);
                tableName = parts[0];
                columnName = parts[1];
            } else if (filter.getTableName() != null) {
                // Use tableName from filter if provided
                tableName = filter.getTableName();
            } else if (tables.size() == 1) {
                // If only one table, use that
                tableName = tables.get(0).getName();
            } else {
                // Cannot determine which table the column belongs to
                throw new IllegalArgumentException("Column name must be qualified with table name when multiple tables are used: " + columnName);
            }
            
            // Create fully qualified column name
            
            
            String operator = filter.getOperator().toLowerCase();
            Object value = filter.getValue();
            System.out.println("eeeeee");
            System.out.println(value);
            Expression<?> column = Expressions.template(Object.class, "{0}.{1}", 
            	    Expressions.template(Object.class, tableName),
            	    Expressions.template(Object.class, columnName)
            	);

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
*/
    
    
    
    /**
     * Modified addDynamicFilters method to handle both simple values and subqueries
     */
    private void addDynamicFilters(SQLQuery<?> query, List<FilterCondition> filters, List<DbTable> tables) {
        if (filters == null || filters.isEmpty()) {
            System.out.println("No filters applied.");
            return;
        }

        Map<String, DbTable> tableNameMap = tables.stream()
            .collect(Collectors.toMap(DbTable::getName, t -> t));
System.out.println(tables.size()+"sssssssssssssssssssssssieze");
        for (FilterCondition filter : filters) {
            System.out.println("Filter received -> Column: " + filter.getColumnName() + 
                               ", Operator: " + filter.getOperator() + 
                               ", Value: " + filter.getValue() +
                               ", Is Subquery: " + filter.isTest());
            
            // Check if this is a subquery comparison
            if (filter.isTest()) {
            	if (tables == null || tables.isEmpty()) {
            	    throw new IllegalArgumentException("No tables provided for the query. loula");
            	}
                // Handle subquery comparison
                addDynamicFilterWithSubquery(query, filter, tables, tableNameMap);
            } else {
                // Handle simple value comparison
                addSimpleFilter(query, filter, tables, tableNameMap);
            }
        }
    }

    /**
     * Handle simple value comparison filters
     */
    private void addSimpleFilter(SQLQuery<?> query, FilterCondition filter, 
            List<DbTable> tables, Map<String, DbTable> tableNameMap) {
// Handle qualified column names (table.column format)
String columnName = filter.getColumnName();
String tableName = null;

if (columnName.contains(".")) {
String[] parts = columnName.split("\\.", 2);
tableName = parts[0];
columnName = parts[1];
} else if (filter.getTableName() != null) {
// Use tableName from filter if provided
tableName = filter.getTableName();
} else if (tables.size() == 1) {
// If only one table, use that
tableName = tables.get(0).getName();
} else {
// Cannot determine which table the column belongs to
throw new IllegalArgumentException("Column name must be qualified with table name when multiple tables are used: " + columnName);
}

// Create fully qualified column name
String operator = filter.getOperator().toLowerCase();
Object valueObj = filter.getValue();

if (!(valueObj instanceof String)) {
throw new IllegalArgumentException("Value must be a String for simple filters");
}
String value = (String) valueObj;

Expression<?> column = Expressions.template(Object.class, "{0}.{1}", 
Expressions.template(Object.class, tableName),
Expressions.template(Object.class, columnName)
);

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


    /**
     * Handle filters with subquery comparison
     */
    private void addDynamicFilterWithSubquery(SQLQuery<?> query, FilterCondition filter, 
            List<DbTable> tables, Map<String, DbTable> tableNameMap) {
    	
    	
    	if (tables == null || tables.isEmpty()) {
    	    throw new IllegalArgumentException("No tables provided for the query.");
    	}
// Extract table and column information
String columnName = filter.getColumnName();
String tableName = null;

if (columnName.contains(".")) {
String[] parts = columnName.split("\\.", 2);
tableName = parts[0];
columnName = parts[1];
} else if (filter.getTableName() != null) {
tableName = filter.getTableName();
} else if (tables.size() == 1) {
tableName = tables.get(0).getName();
} else {
throw new IllegalArgumentException("Column name must be qualified with table name when multiple tables are used: " + columnName);
}

// Create the column expression
Expression<?> column = Expressions.template(Object.class, "{0}.{1}", 
Expressions.template(Object.class, tableName),
Expressions.template(Object.class, columnName)
);

// Get the value as a Requete object
Object valueObj = filter.getValue();
Requete subqueryRequete;

if (valueObj instanceof Requete) {
subqueryRequete = (Requete) valueObj;
} else {
throw new IllegalArgumentException("Value must be a Requete object for subquery filters");
}

// Create a mapping of table IDs to path objects
Map<Long, RelationalPath<?>> tablePaths = new HashMap<>();
Map<Long, DbTable> tableIdMap = new HashMap<>();

for (DbTable table : tables) {
RelationalPathBase<?> path = new RelationalPathBase<>(
Object.class, 
table.getName(),  
table.getName(), 
table.getName()
);
tablePaths.put(table.getId(), path);
tableIdMap.put(table.getId(), table);
}

// Create database connection for subquery
SQLQueryFactory queryFactory = queryDSLFactory.createSQLQueryFactory(
getDbUrl(tables.get(0)), 
tables.get(0).getDatabase().getConnexion().getUsername(),
tables.get(0).getDatabase().getConnexion().getPassword(),
getDriverClass(tables.get(0).getDatabase().getDbtype())
);

// Create and apply the subquery
SQLQuery<?> subquery = createSubquery(queryFactory, subqueryRequete, tablePaths, tableIdMap);

// Apply the filter with the subquery
applyWhereWithSubquery(query, filter, column, subquery);
}


    /**
     * Apply WHERE condition with subquery
     */
    private void applyWhereWithSubquery(SQLQuery<?> query, 
                                        FilterCondition filter, 
                                        Expression<?> column, 
                                        SQLQuery<?> subquery) {
        String operator = filter.getOperator();
        if (operator == null || operator.isEmpty()) {
            operator = "="; // Default operator
        }
        
        switch (operator.toUpperCase()) {
            case "=":
            case "==":
                query.where(Expressions.booleanTemplate("{0} = ({1})", column, subquery));
                break;
            case ">":
                query.where(Expressions.booleanTemplate("{0} > ({1})", column, subquery));
                break;
            case "<":
                query.where(Expressions.booleanTemplate("{0} < ({1})", column, subquery));
                break;
            case ">=":
                query.where(Expressions.booleanTemplate("{0} >= ({1})", column, subquery));
                break;
            case "<=":
                query.where(Expressions.booleanTemplate("{0} <= ({1})", column, subquery));
                break;
            case "!=":
            case "<>":
                query.where(Expressions.booleanTemplate("{0} != ({1})", column, subquery));
                break;
            case "IN":
                query.where(Expressions.booleanTemplate("{0} IN ({1})", column, subquery));
                break;
            case "NOT IN":
                query.where(Expressions.booleanTemplate("{0} NOT IN ({1})", column, subquery));
                break;
            case "EXISTS":
                query.where(Expressions.booleanTemplate("EXISTS ({1})", column, subquery));
                break;
            case "NOT EXISTS":
                query.where(Expressions.booleanTemplate("NOT EXISTS ({1})", column, subquery));
                break;
            case "ANY":
            case "SOME":
                query.where(Expressions.booleanTemplate("{0} " + filter.getOperator() + " ANY ({1})", column, subquery));
                break;
            case "ALL":
                query.where(Expressions.booleanTemplate("{0} " + filter.getOperator() + " ALL ({1})", column, subquery));
                break;
            default:
                // Default to = for unknown operators
                query.where(Expressions.booleanTemplate("{0} = ({1})", column, subquery));
        }
    }

    /**
     * Helper method to get the database URL
     */
    private String getDbUrl(DbTable table) {
        DatabaseType dbType = table.getDatabase().getDbtype();
        
        if (dbType == DatabaseType.MySQL) {
            return "jdbc:mysql://" + table.getDatabase().getConnexion().getHost() + ":" 
                  + table.getDatabase().getConnexion().getPort() + "/" 
                  + table.getDatabase().getName();
        } else if (dbType == DatabaseType.Oracle) {
            return "jdbc:oracle:thin:@" + table.getDatabase().getConnexion().getHost() + ":" 
                  + table.getDatabase().getConnexion().getPort() + ":" 
                  + table.getDatabase().getName();
        } else {
            throw new RuntimeException("Unsupported database type: " + dbType);
        }
    }

    /**
     * Helper method to get the driver class
     */
    private String getDriverClass(DatabaseType dbType) {
        if (dbType == DatabaseType.MySQL) {
            return "com.mysql.cj.jdbc.Driver";
        } else if (dbType == DatabaseType.Oracle) {
            return "oracle.jdbc.OracleDriver";
        } else {
            throw new RuntimeException("Unsupported database type: " + dbType);
        }
    }
    
    
    
    
    private void addAggregations(QueryRequestDTO request, List<Expression<?>> selectExpressions, 
            Map<String, Expression<?>> aliasMapping, List<DbTable> tables) {
        if (request.getAggregations() != null) {
            for (AggregationRequest agg : request.getAggregations()) {
                TabColumn column = columnRepository.findById(agg.getColumnId()).orElse(null);
                if (column != null) {
                    String tableName = column.getTable().getName();
                    String columnName = column.getName();
                    String columnType = column.getType().toLowerCase();

                    Expression<?> aggregateExpr;

                    if (columnType.contains("date")) {
                        aggregateExpr = Expressions.dateTemplate(java.util.Date.class, tableName + "." + columnName);
                        for (String func : agg.getfunctionagg()) {
                        	switch (func.toUpperCase()) {
                        	case "MAX":
                                aggregateExpr = SQLExpressions.max(Expressions.dateTemplate(java.util.Date.class, aggregateExpr.toString()));
                                break;
                        	case "MIN":
                                aggregateExpr = SQLExpressions.min(Expressions.dateTemplate(java.util.Date.class, aggregateExpr.toString()));
                                break;
                          
                            case "COUNT":
                                aggregateExpr = SQLExpressions.count(aggregateExpr);
                                break;
                                
                                
                            }
                        }
                    } else { // For numbers
                        aggregateExpr = Expressions.numberTemplate(Double.class, tableName + "." + columnName);
                        for (String func : agg.getfunctionagg()) {
                            switch (func.toUpperCase()) {
                                case "MAX":
                                    aggregateExpr = SQLExpressions.max(Expressions.numberTemplate(Double.class, aggregateExpr.toString()));
                                    break;
                                case "MIN":
                                    aggregateExpr = SQLExpressions.min(Expressions.numberTemplate(Double.class, aggregateExpr.toString()));
                                    break;
                                case "AVG":
                                    aggregateExpr = SQLExpressions.avg(Expressions.numberTemplate(Double.class, aggregateExpr.toString()));
                                    break;
                                case "SUM":
                                    aggregateExpr = SQLExpressions.sum(Expressions.numberTemplate(Double.class, aggregateExpr.toString()));
                                    break;
                                case "COUNT":
                                    aggregateExpr = SQLExpressions.count(aggregateExpr);
                                    break;
                            }
                        }
                    }

                    String alias = String.join("_", agg.getfunctionagg()).toLowerCase() + "_" + tableName + "_" + columnName;
                    Expression<?> aliasedExpr = Expressions.as(aggregateExpr, alias);
                    selectExpressions.add(aliasedExpr);
                    aliasMapping.put(alias, aliasedExpr);
                }
            }
        }
    }

    
  
    
    public Long insertTableData(InsertRequestDTO request) {
        // Validate request
        if (request.getTableId() == null) {
            throw new IllegalArgumentException("Table ID must be provided");
        }
        
        if (request.getColumnValues() == null || request.getColumnValues().isEmpty()) {
            throw new IllegalArgumentException("Column values must be provided");
        }
        
        // Get table
        DbTable table = tableRepository.findById(request.getTableId())
                .orElseThrow(() -> new RuntimeException("Table not found with ID: " + request.getTableId()));
        
        // Get database connection details
        DatabaseType dbType = table.getDatabase().getDbtype();
        String dbUrl, driver;
        
        if (dbType == DatabaseType.MySQL) {
            dbUrl = "jdbc:mysql://" + table.getDatabase().getConnexion().getHost() + ":" 
                  + table.getDatabase().getConnexion().getPort() + "/" 
                  + table.getDatabase().getName();
            driver = "com.mysql.cj.jdbc.Driver";
        } else if (dbType == DatabaseType.Oracle) {
            dbUrl = "jdbc:oracle:thin:@" + table.getDatabase().getConnexion().getHost() + ":" 
                  + table.getDatabase().getConnexion().getPort() + ":" 
                  + table.getDatabase().getName();
            driver = "oracle.jdbc.OracleDriver";
        } else {
            throw new RuntimeException("Unsupported database type: " + dbType);
        }

        String username = table.getDatabase().getConnexion().getUsername();
        String password = table.getDatabase().getConnexion().getPassword();
        
        SQLQueryFactory queryFactory = queryDSLFactory.createSQLQueryFactory(dbUrl, username, password, driver);

        try (Connection conn = DriverManager.getConnection(dbUrl, username, password)) {
            // Create path object for the table
            RelationalPathBase<?> path = new RelationalPathBase<>(
                Object.class, 
                table.getName(),  
                table.getName(), 
                table.getName()             
            );
            
            // Build insert query
            SQLInsertClause insert = queryFactory.insert(path);
            
            // Add column values
            for (Map.Entry<String, Object> entry : request.getColumnValues().entrySet()) {
                String columnName = entry.getKey();
                Object value = entry.getValue();
                
                // Use Expressions to set values
                insert.set(Expressions.path(Object.class, path, columnName), value);
            }
            
            // Execute insert
            System.out.println("Executing insert: " + insert.toString());
            long result = insert.execute();
            System.out.println("Insert completed. Rows affected: " + result);
            
            return result;
        } catch (Exception e) {
            System.err.println("Error executing insert: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to insert data: " + e.getMessage(), e);
        }
    }
    
    
    
    public Long updateTableData(UpdateRequestDTO request) {
        // Validate request
        if (request.getTableId() == null) {
            throw new IllegalArgumentException("Table ID must be provided");
        }

        if (request.getColumnValues() == null || request.getColumnValues().isEmpty()) {
            throw new IllegalArgumentException("Column values must be provided");
        }

        if (request.getFilters() == null || request.getFilters().isEmpty()) {
            throw new IllegalArgumentException("At least one filter condition must be provided for update");
        }

        // Get table
        DbTable table = tableRepository.findById(request.getTableId())
            .orElseThrow(() -> new RuntimeException("Table not found with ID: " + request.getTableId()));

        // Get database connection details
        DatabaseType dbType = table.getDatabase().getDbtype();
        String dbUrl, driver;

        if (dbType == DatabaseType.MySQL) {
            dbUrl = "jdbc:mysql://" + table.getDatabase().getConnexion().getHost() + ":"
                + table.getDatabase().getConnexion().getPort() + "/"
                + table.getDatabase().getName();
            driver = "com.mysql.cj.jdbc.Driver";
        } else if (dbType == DatabaseType.Oracle) {
            dbUrl = "jdbc:oracle:thin:@" + table.getDatabase().getConnexion().getHost() + ":"
                + table.getDatabase().getConnexion().getPort() + ":"
                + table.getDatabase().getName();
            driver = "oracle.jdbc.OracleDriver";
        } else {
            throw new RuntimeException("Unsupported database type: " + dbType);
        }

        String username = table.getDatabase().getConnexion().getUsername();
        String password = table.getDatabase().getConnexion().getPassword();

        SQLQueryFactory queryFactory = queryDSLFactory.createSQLQueryFactory(dbUrl, username, password, driver);

        try (Connection conn = DriverManager.getConnection(dbUrl, username, password)) {
            // Create path object for the table
            RelationalPathBase<?> path = new RelationalPathBase<>(
                Object.class,
                table.getName(),
                table.getName(),
                table.getName()
            );

            // Build update query
            SQLUpdateClause update = queryFactory.update(path);

            // Add column values to update
            for (Map.Entry<String, Object> entry : request.getColumnValues().entrySet()) {
                String columnName = entry.getKey();
                Object value = entry.getValue();

                // Use Expressions to set values
                update.set(Expressions.path(Object.class, path, columnName), value);
            }

            // Add where conditions from filters
            applyFiltersToUpdate(update, request.getFilters(), Collections.singletonList(table));

            // Execute update
            System.out.println("Executing update: " + update.toString());
            long result = update.execute();
            System.out.println("Update completed. Rows affected: " + result);

            return result;
        } catch (Exception e) {
            System.err.println("Error executing update: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update data: " + e.getMessage(), e);
        }
    }

    /**
     * Applies filter conditions to an update clause
     */
    private void applyFiltersToUpdate(SQLUpdateClause update, List<FilterCondition> filters, List<DbTable> tables) {
        if (filters == null || filters.isEmpty()) {
            System.out.println("No filters applied to update.");
            return;
        }

        Map<String, DbTable> tableNameMap = tables.stream()
            .collect(Collectors.toMap(DbTable::getName, t -> t));

        for (FilterCondition filter : filters) {
            System.out.println("Filter applied to update -> Column: " + filter.getColumnName() +
                              ", Operator: " + filter.getOperator() +
                              ", Value: " + filter.getValue());

            // Handle qualified column names (table.column format)
            String columnName = filter.getColumnName();
            String tableName = null;

            if (columnName.contains(".")) {
                String[] parts = columnName.split("\\.", 2);
                tableName = parts[0];
                columnName = parts[1];
            } else if (filter.getTableName() != null) {
                // Use tableName from filter if provided
                tableName = filter.getTableName();
            } else if (tables.size() == 1) {
                // If only one table, use that
                tableName = tables.get(0).getName();
            } else {
                // Cannot determine which table the column belongs to
                throw new IllegalArgumentException("Column name must be qualified with table name when multiple tables are used: " + columnName);
            }

            String operator = filter.getOperator().toLowerCase();
            String value = filter.getValue().toString();
            
            Expression<?> column = Expressions.template(Object.class, "{0}.{1}",
                Expressions.template(Object.class, tableName),
                Expressions.template(Object.class, columnName)
            );

            switch (operator) {
                case "=":
                    update.where(Expressions.booleanTemplate("{0} = {1}", column, value));
                    break;
                case "!=":
                    update.where(Expressions.booleanTemplate("{0} != {1}", column, value));
                    break;
                case "like":
                    update.where(Expressions.booleanTemplate("{0} LIKE {1}", column, "%" + value + "%"));
                    break;
                case ">":
                    update.where(Expressions.booleanTemplate("{0} > {1}", column, value));
                    break;
                case "<":
                    update.where(Expressions.booleanTemplate("{0} < {1}", column, value));
                    break;
                case ">=":
                    update.where(Expressions.booleanTemplate("{0} >= {1}", column, value));
                    break;
                case "<=":
                    update.where(Expressions.booleanTemplate("{0} <= {1}", column, value));
                    break;
                case "in":
                    update.where(Expressions.booleanTemplate("{0} IN ({1})", column, value));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported operator: " + operator);
            }
        }
    }
    
    
    
    public Long updateTableDataWithJoins(UpdateRequestDTO request) {
        // Validate request
        if (request.getTableId() == null) {
            throw new IllegalArgumentException("Table ID must be provided");
        }

        if (request.getColumnValues() == null || request.getColumnValues().isEmpty()) {
            throw new IllegalArgumentException("Column values must be provided");
        }

        if ((request.getFilters() == null || request.getFilters().isEmpty()) && 
            (request.getJoins() == null || request.getJoins().isEmpty())) {
            throw new IllegalArgumentException("At least one filter condition or join must be provided for update");
        }

        // Get table
        DbTable table = tableRepository.findById(request.getTableId())
            .orElseThrow(() -> new RuntimeException("Table not found with ID: " + request.getTableId()));

        // STEP 1: Create a QueryRequestDTO to fetch the records using joins
        QueryRequestDTO fetchRequest = new QueryRequestDTO();
        
        // Add main table and any joined tables
        List<Long> allTableIds = new ArrayList<>();
        allTableIds.add(request.getTableId());
        
        // Add all tables involved in joins
        if (request.getJoins() != null) {
            for (JoinCondition join : request.getJoins()) {
                if (!allTableIds.contains(join.getFirstTableId())) {
                    allTableIds.add(join.getFirstTableId());
                }
                if (!allTableIds.contains(join.getSecondTableId())) {
                    allTableIds.add(join.getSecondTableId());
                }
            }
        }
        fetchRequest.setTableId(allTableIds);

        List<TabColumn> mainTableColumns = columnRepository.findByTableId(request.getTableId());
        List<Long> mainColumnIds = mainTableColumns.stream()
            .map(TabColumn::getId)
            .collect(Collectors.toList());
        fetchRequest.setColumnId(mainColumnIds);
        
        // Set up join conditions
        if (request.getJoins() != null && !request.getJoins().isEmpty()) {
            JoinRequestDTO joinRequest = new JoinRequestDTO();
            joinRequest.setJoinConditions(request.getJoins());
            fetchRequest.setJoinRequest(joinRequest);
            
        }
        
        
        // Add the same filters as the update request
        fetchRequest.setFilters(request.getFilters());
        System.out.println("eeeeeeeeeeeeeeeeeeeeeeeee"+fetchRequest);
        // STEP 2: Fetch the records that will be updated
        List<Map<String, Object>> recordsToUpdate = fetchTableDataWithCondition(fetchRequest);
        
        if (recordsToUpdate.isEmpty()) {
            System.out.println("No records found matching the join conditions");
            return 0L;
        }
        
        System.out.println("Found " + recordsToUpdate.size() + " records to update");
        
        // STEP 3: Update each record individually
        long totalRowsUpdated = 0;
        String tableName = table.getName();
        
        for (Map<String, Object> record : recordsToUpdate) {
            // Create a new filter condition for this specific record
            List<FilterCondition> recordFilters = new ArrayList<>();
            
            // Use the ID columns from the main table to identify this record uniquely
            for (TabColumn column : mainTableColumns) {
                String columnAlias = tableName + "_" + column.getName();
                if (record.containsKey(columnAlias) && record.get(columnAlias) != null) {
                    FilterCondition idFilter = new FilterCondition();
                    idFilter.setColumnName(column.getName());
                    idFilter.setTableName(tableName);
                    idFilter.setOperator("=");
                    idFilter.setValue(String.valueOf(record.get(columnAlias)));
                    recordFilters.add(idFilter);
                    
                    // We only need one unique identifier column (typically the ID)
                    // but you could add more if needed for compound keys
                    break;
                }
            }
            
            if (recordFilters.isEmpty()) {
                System.out.println("Warning: Could not create unique filter for a record. Skipping.");
                continue;
            }
            
            // Create a new update request for just this record
            UpdateRequestDTO singleUpdateRequest = new UpdateRequestDTO();
            singleUpdateRequest.setTableId(request.getTableId());
            singleUpdateRequest.setColumnValues(request.getColumnValues());
            singleUpdateRequest.setFilters(recordFilters);
            
            try {
                Long rowsUpdated = updateTableData(singleUpdateRequest);
                totalRowsUpdated += rowsUpdated;
            } catch (Exception e) {
                System.err.println("Error updating record: " + e.getMessage());
                // You can choose to continue with other records or throw an exception
            }
        }
        
        return totalRowsUpdated;
    }

  
    
    
    public Long deleteTableDataWithJoins(DeleteRequestDTO request) {
        // Validate request
        if (request.getTableId() == null) {
            throw new IllegalArgumentException("Table ID must be provided");
        }

        if ((request.getFilters() == null || request.getFilters().isEmpty()) && 
            (request.getJoins() == null || request.getJoins().isEmpty())) {
            throw new IllegalArgumentException("At least one filter condition or join must be provided for delete");
        }

        // Get table
        DbTable table = tableRepository.findById(request.getTableId())
            .orElseThrow(() -> new RuntimeException("Table not found with ID: " + request.getTableId()));

        // STEP 1: Create a QueryRequestDTO to fetch the records using joins
        QueryRequestDTO fetchRequest = new QueryRequestDTO();
        
        // Add main table and any joined tables
        List<Long> allTableIds = new ArrayList<>();
        allTableIds.add(request.getTableId());
        
        // Add all tables involved in joins
        if (request.getJoins() != null) {
            for (JoinCondition join : request.getJoins()) {
                if (!allTableIds.contains(join.getFirstTableId())) {
                    allTableIds.add(join.getFirstTableId());
                }
                if (!allTableIds.contains(join.getSecondTableId())) {
                    allTableIds.add(join.getSecondTableId());
                }
            }
        }
        fetchRequest.setTableId(allTableIds);

        List<TabColumn> mainTableColumns = columnRepository.findByTableId(request.getTableId());
        List<Long> mainColumnIds = mainTableColumns.stream()
            .map(TabColumn::getId)
            .collect(Collectors.toList());
        fetchRequest.setColumnId(mainColumnIds);
        
        // Set up join conditions
        if (request.getJoins() != null && !request.getJoins().isEmpty()) {
            JoinRequestDTO joinRequest = new JoinRequestDTO();
            joinRequest.setJoinConditions(request.getJoins());
            fetchRequest.setJoinRequest(joinRequest);
        }
        
        // Add the same filters as the delete request
        fetchRequest.setFilters(request.getFilters());
        System.out.println("Preparing delete with joins query: " + fetchRequest);
        
        // STEP 2: Fetch the records that will be deleted
        List<Map<String, Object>> recordsToDelete = fetchTableDataWithCondition(fetchRequest);
        
        if (recordsToDelete.isEmpty()) {
            System.out.println("No records found matching the join conditions");
            return 0L;
        }
        
        System.out.println("Found " + recordsToDelete.size() + " records to delete");
        
        // STEP 3: Delete each record individually
        long totalRowsDeleted = 0;
        String tableName = table.getName();
        
        for (Map<String, Object> record : recordsToDelete) {
            // Create a new filter condition for this specific record
            List<FilterCondition> recordFilters = new ArrayList<>();
            
            // Use the ID columns from the main table to identify this record uniquely
            for (TabColumn column : mainTableColumns) {
                String columnAlias = tableName + "_" + column.getName();
                if (record.containsKey(columnAlias) && record.get(columnAlias) != null) {
                    FilterCondition idFilter = new FilterCondition();
                    idFilter.setColumnName(column.getName());
                    idFilter.setTableName(tableName);
                    idFilter.setOperator("=");
                    idFilter.setValue(String.valueOf(record.get(columnAlias)));
                    recordFilters.add(idFilter);
                    
                    // We only need one unique identifier column (typically the ID)
                    break;
                }
            }
            
            if (recordFilters.isEmpty()) {
                System.out.println("Warning: Could not create unique filter for a record. Skipping.");
                continue;
            }
            
            try {
                Long rowsDeleted = deleteTableData(new DeleteRequestDTO(request.getTableId(), recordFilters, null));
                totalRowsDeleted += rowsDeleted;
            } catch (Exception e) {
                System.err.println("Error deleting record: " + e.getMessage());
                // You can choose to continue with other records or throw an exception
            }
        }
        
        return totalRowsDeleted;
    }
    
    
    public Long deleteTableData(DeleteRequestDTO request) {
        // Validate request
        if (request.getTableId() == null) {
            throw new IllegalArgumentException("Table ID must be provided");
        }

        if (request.getFilters() == null || request.getFilters().isEmpty()) {
            throw new IllegalArgumentException("At least one filter condition must be provided for delete");
        }

        // Get table
        DbTable table = tableRepository.findById(request.getTableId())
            .orElseThrow(() -> new RuntimeException("Table not found with ID: " + request.getTableId()));

        // Get database connection details
        DatabaseType dbType = table.getDatabase().getDbtype();
        String dbUrl, driver;

        if (dbType == DatabaseType.MySQL) {
            dbUrl = "jdbc:mysql://" + table.getDatabase().getConnexion().getHost() + ":"
                + table.getDatabase().getConnexion().getPort() + "/"
                + table.getDatabase().getName();
            driver = "com.mysql.cj.jdbc.Driver";
        } else if (dbType == DatabaseType.Oracle) {
            dbUrl = "jdbc:oracle:thin:@" + table.getDatabase().getConnexion().getHost() + ":"
                + table.getDatabase().getConnexion().getPort() + ":"
                + table.getDatabase().getName();
            driver = "oracle.jdbc.OracleDriver";
        } else {
            throw new RuntimeException("Unsupported database type: " + dbType);
        }

        String username = table.getDatabase().getConnexion().getUsername();
        String password = table.getDatabase().getConnexion().getPassword();

        SQLQueryFactory queryFactory = queryDSLFactory.createSQLQueryFactory(dbUrl, username, password, driver);

        try (Connection conn = DriverManager.getConnection(dbUrl, username, password)) {
            // Create path object for the table
            RelationalPathBase<?> path = new RelationalPathBase<>(
                Object.class,
                table.getName(),
                table.getName(),
                table.getName()
            );

            // Build delete query
            SQLDeleteClause delete = queryFactory.delete(path);

            // Add where conditions from filters
            applyFiltersToDelete(delete, request.getFilters(), Collections.singletonList(table));

            // Execute delete
            System.out.println("Executing delete: " + delete.toString());
            long result = delete.execute();
            System.out.println("Delete completed. Rows affected: " + result);

            return result;
        } catch (Exception e) {
            System.err.println("Error executing delete: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete data: " + e.getMessage(), e);
        }
    }
    
    /**
     * Applies filter conditions to a delete clause
     */
    private void applyFiltersToDelete(SQLDeleteClause delete, List<FilterCondition> filters, List<DbTable> tables) {
        if (filters == null || filters.isEmpty()) {
            System.out.println("No filters applied to delete.");
            return;
        }

        Map<String, DbTable> tableNameMap = tables.stream()
            .collect(Collectors.toMap(DbTable::getName, t -> t));

        for (FilterCondition filter : filters) {
            System.out.println("Filter applied to delete -> Column: " + filter.getColumnName() +
                              ", Operator: " + filter.getOperator() +
                              ", Value: " + filter.getValue());

            // Handle qualified column names (table.column format)
            String columnName = filter.getColumnName();
            String tableName = null;

            if (columnName.contains(".")) {
                String[] parts = columnName.split("\\.", 2);
                tableName = parts[0];
                columnName = parts[1];
            } else if (filter.getTableName() != null) {
                // Use tableName from filter if provided
                tableName = filter.getTableName();
            } else if (tables.size() == 1) {
                // If only one table, use that
                tableName = tables.get(0).getName();
            } else {
                // Cannot determine which table the column belongs to
                throw new IllegalArgumentException("Column name must be qualified with table name when multiple tables are used: " + columnName);
            }

            String operator = filter.getOperator().toLowerCase();
            String value = filter.getValue().toString();
            
            Expression<?> column = Expressions.template(Object.class, "{0}.{1}",
                Expressions.template(Object.class, tableName),
                Expressions.template(Object.class, columnName)
            );

            switch (operator) {
                case "=":
                    delete.where(Expressions.booleanTemplate("{0} = {1}", column, value));
                    break;
                case "!=":
                    delete.where(Expressions.booleanTemplate("{0} != {1}", column, value));
                    break;
                case "like":
                    delete.where(Expressions.booleanTemplate("{0} LIKE {1}", column, "%" + value + "%"));
                    break;
                case ">":
                    delete.where(Expressions.booleanTemplate("{0} > {1}", column, value));
                    break;
                case "<":
                    delete.where(Expressions.booleanTemplate("{0} < {1}", column, value));
                    break;
                case ">=":
                    delete.where(Expressions.booleanTemplate("{0} >= {1}", column, value));
                    break;
                case "<=":
                    delete.where(Expressions.booleanTemplate("{0} <= {1}", column, value));
                    break;
                case "in":
                    delete.where(Expressions.booleanTemplate("{0} IN ({1})", column, value));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported operator: " + operator);
            }
        }
    }
    
    
    public List<Map<String, Object>> executeStringQuery(String query, Long dbid,Long senderId) {
    	
    	  Database db = databaseRepository.findById(dbid)
                  .orElseThrow(() -> new RuntimeException("Db not found with ID: " + dbid));
    	  
    	  User u = userRepository.findById(senderId).orElseThrow(() -> new RuntimeException("User not found with ID: " + senderId));
    	
        try (Connection conn = DriverManager.getConnection(db.getJdbcUrl(), db.getConnexion().getUsername(), db.getConnexion().getPassword());
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(query)) {

            List<Map<String, Object>> resultList = new ArrayList<>();
            java.sql.ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(rsmd.getColumnLabel(i), rs.getObject(i));
                }
                resultList.add(row);
            }
            
            Requete r = new Requete();
            r.setContent(query);
            r.setSender(u);
            r.setSentAt(LocalDateTime.now());
            
            //this.requeteRepository.save(r);

            return resultList;

        } catch (Exception e) {
            throw new RuntimeException("Error executing query: " + query, e);
        }
    }


    /*
      private void addAggregations2(Requete request, List<Expression<?>> selectExpressions, 
              Map<String, Expression<?>> aliasMapping, List<DbTable> tables) {
  if (request.getAggregation() != null) {
  for (AggregationRequest agg : request.getAggregation()) {
  TabColumn column = columnRepository.findById(agg.getColumnId()).orElse(null);
  if (column != null) {
  String tableName = column.getTable().getName();
  String columnName = column.getName();
  String alias = agg.getfunctionagg().toLowerCase() + "_" + tableName + "_" + columnName;
  Expression<?> aggregateExpr = null;

  switch (agg.getfunctionagg().toUpperCase()) {
      case "MAX":
          aggregateExpr = SQLExpressions.max(
          		Expressions.numberTemplate(Double.class, tableName + "." + columnName)

          );
          break;
      case "MIN":
          aggregateExpr = SQLExpressions.min(
          		Expressions.numberTemplate(Double.class, tableName + "." + columnName)

          );
          break;
      case "AVG":
          aggregateExpr = SQLExpressions.avg(
          		Expressions.numberTemplate(Double.class, tableName + "." + columnName)

          );
          break;
      case "SUM":
          aggregateExpr = SQLExpressions.sum(
          		Expressions.numberTemplate(Double.class, tableName + "." + columnName)
          );
          break;
      case "COUNT":
          
          aggregateExpr = SQLExpressions.count(
          		Expressions.numberTemplate(Double.class, tableName + "." + columnName)

          );
          break;
      default:
          throw new IllegalArgumentException("Unsupported aggregation function: " + agg.getfunctionagg());
  }

  if (aggregateExpr != null) {
      Expression<?> aliasedExpr = Expressions.as(aggregateExpr, alias);
      selectExpressions.add(aliasedExpr);
      aliasMapping.put(alias, aliasedExpr);
  }
  }
  }
  }
  }
   */
      public List<Map<String, Object>> fetchTableDataWithCondition2(Requete request) {
          // Validate request
          if (request.getTableId() == null || request.getTableId().isEmpty()) {
              throw new IllegalArgumentException("At least one table ID must be provided");
          }
          
          // Get primary table (first in the list)
          Long primaryTableId = request.getTableId().get(0);
          DbTable primaryTable = tableRepository.findById(primaryTableId)
                  .orElseThrow(() -> new RuntimeException("Primary table not found with ID: " + primaryTableId));
          
          // Get database connection details
          DatabaseType dbType = primaryTable.getDatabase().getDbtype();
          String dbUrl, driver;
          
          if (dbType == DatabaseType.MySQL) {
              dbUrl = "jdbc:mysql://" + primaryTable.getDatabase().getConnexion().getHost() + ":" 
                    + primaryTable.getDatabase().getConnexion().getPort() + "/" 
                    + primaryTable.getDatabase().getName();
              driver = "com.mysql.cj.jdbc.Driver";
          } else if (dbType == DatabaseType.Oracle) {
              dbUrl = "jdbc:oracle:thin:@" + primaryTable.getDatabase().getConnexion().getHost() + ":" 
                    + primaryTable.getDatabase().getConnexion().getPort() + ":" 
                    + primaryTable.getDatabase().getName();
              driver = "oracle.jdbc.OracleDriver";
          } else {
              throw new RuntimeException("Unsupported database type: " + dbType);
          }

          String username = primaryTable.getDatabase().getConnexion().getUsername();
          String password = primaryTable.getDatabase().getConnexion().getPassword();
          
          SQLQueryFactory queryFactory = queryDSLFactory.createSQLQueryFactory(dbUrl, username, password, driver);

          try (Connection conn = DriverManager.getConnection(dbUrl, username, password)) {
              // Get list of requested tables and build a mapping
              List<DbTable> tables = new ArrayList<>();
              Map<Long, DbTable> tableIdMap = new HashMap<>();
              
              for (Long tableId : request.getTableId()) {
                  DbTable table = tableRepository.findById(tableId)
                          .orElseThrow(() -> new RuntimeException("Table not found with ID: " + tableId));
                  tables.add(table);
                  tableIdMap.put(tableId, table);
              }

              // Create path objects for all tables - use actual table names
              Map<Long, RelationalPath<?>> tablePaths = new HashMap<>();
              for (DbTable table : tables) {
                  // Use the actual table name for the path
                  RelationalPathBase<?> path = new RelationalPathBase<>(
                          Object.class, 
                          table.getName(),  
                          table.getName(), 
                          table.getName()             
                      );
                  tablePaths.put(table.getId(), path);
                  System.out.println("Added table path: " + table.getName() + " for ID: " + table.getId());
              }
              
              // Get columns with their tables
              List<TabColumn> columns = columnRepository.findAllById(request.getColumnId());
              
              // Prepare select expressions and alias mapping
              List<Expression<?>> selectExpressions = new ArrayList<>();
              Map<String, Expression<?>> aliasMapping = new HashMap<>();

              for (TabColumn column : columns) {
                  String tableName = column.getTable().getName();
                  String columnName = column.getName();
                  
                  // Use template expressions to create qualified column references
                  Expression<?> colExpr = Expressions.template(Object.class, "{0}.{1}", 
                      Expressions.template(Object.class, tableName),
                      Expressions.template(Object.class, columnName)
                  );
                  
                  // Create alias for column
                  String columnAlias = tableName + "_" + columnName;
                  Expression<?> aliasedExpr = Expressions.as(colExpr, columnAlias);
                  
                  selectExpressions.add(aliasedExpr);
                  aliasMapping.put(columnAlias, aliasedExpr);
              }

              // Add aggregations
             // addAggregations2(request, selectExpressions, aliasMapping, tables);

              if (selectExpressions.isEmpty()) {
                  throw new IllegalArgumentException("No columns selected for query");
              }

              // Start building query with primary table
              RelationalPath<?> primaryPath = tablePaths.get(primaryTableId);
              SQLQuery<Tuple> query = queryFactory.select(selectExpressions.toArray(new Expression<?>[0])).from(primaryPath);
              
              // Check if there are join conditions
              if (request.getJoinConditions() != null && !request.getJoinConditions().isEmpty()) {
                  addJoinsFromList(query, tablePaths, request.getJoinConditions());
              }
              
              // Add filters
              addDynamicFilters(query, request.getFilters(), tables);
              
              // Add group by
              if (request.getGroupByColumns() != null && !request.getGroupByColumns().isEmpty()) {
                  List<Expression<?>> groupByExpressions = new ArrayList<>();
                  for (Long groupByColumnId : request.getGroupByColumns()) {
                      TabColumn groupByColumn = columnRepository.findById(groupByColumnId).orElse(null);
                      if (groupByColumn != null) {
                          RelationalPath<?> tablePath = tablePaths.get(groupByColumn.getTable().getId());
                          if (tablePath != null) {
                              groupByExpressions.add(Expressions.path(Object.class, tablePath, groupByColumn.getName()));
                          }
                      }
                  }
                  if (!groupByExpressions.isEmpty()) {
                      query.groupBy(groupByExpressions.toArray(new Expression<?>[0]));
                  }
              }
              
              System.out.println("Final query: " + query.toString());
              
              // Execute query and process results
              List<Tuple> result = query.fetch();
              System.out.println("Query executed successfully. Rows fetched: " + result.size());
              
              // Update request metadata
              request.setSentAt(LocalDateTime.now());
              System.out.println(LocalDateTime.now());     
              //request.setContent(query.toString());
          
              request.setTableReq(primaryTable);
              requeteRepository.save(request);
              
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
      

    
}