package com.example.demo.querydsl;

import com.querydsl.sql.SQLQueryFactory;
import com.example.demo.condition.AggregationRequest;
import com.example.demo.condition.FilterCondition;
import com.example.demo.condition.JoinCondition;
import com.example.demo.connexions.DatabaseType;
import com.example.demo.dto.QueryRequestDTO;
import com.example.demo.tablecolumns.ColumnRepository;
import com.example.demo.tablecolumns.TabColumn;
import com.example.demo.tables.DbTable;
import com.example.demo.tables.TableRepository;
import com.querydsl.core.Tuple;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.Expression;

import java.sql.Connection;
import java.sql.DriverManager;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
    
    public DynamicQueryService(QueryDSLFactory queryDSLFactory) {
        this.queryDSLFactory = queryDSLFactory;
    }

    public List<Map<String, Object>> fetchTableDataWithCondition(QueryRequestDTO request) {
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
            
            System.out.println("Final query: " + query.toString());
            
            // Execute query and process results
            List<Tuple> result = query.fetch();
            System.out.println("Query executed successfully. Rows fetched: " + result.size());

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
    
    
    private void addJoinsFromList(SQLQuery<?> query, Map<Long, RelationalPath<?>> tablePaths, 
            List<JoinCondition> joinConditions) {
for (JoinCondition joinCondition : joinConditions) {
RelationalPath<?> firstTablePath = tablePaths.get(joinCondition.getFirstTableId());
RelationalPath<?> secondTablePath = tablePaths.get(joinCondition.getSecondTableId());

if (firstTablePath == null || secondTablePath == null) {
throw new RuntimeException("One of the tables in the join condition is missing. First table ID: " + 
 joinCondition.getFirstTableId() + ", Second table ID: " + joinCondition.getSecondTableId());
}

String firstTable = firstTablePath.getMetadata().getName();
String secondTable = secondTablePath.getMetadata().getName();

System.out.println("Joining tables: " + firstTable + " and " + secondTable);

// Use properly qualified column references with appropriate operators
BooleanExpression joinOnCondition = Expressions.booleanTemplate(
"{0}.{1} = {2}.{3}", 
Expressions.template(Object.class, firstTable),
Expressions.template(Object.class, joinCondition.getFirstColumnName()),
Expressions.template(Object.class, secondTable),
Expressions.template(Object.class, joinCondition.getSecondColumnName())
);

// Apply join type
switch (joinCondition.getJoinType().toUpperCase()) {
case "INNER":
 query.innerJoin(secondTablePath).on(joinOnCondition);
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
            String value = filter.getValue();
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

    private void addAggregations(QueryRequestDTO request, List<Expression<?>> selectExpressions, 
            Map<String, Expression<?>> aliasMapping, List<DbTable> tables) {
if (request.getAggregations() != null) {
for (AggregationRequest agg : request.getAggregations()) {
TabColumn column = columnRepository.findById(agg.getColumnId()).orElse(null);
if (column != null) {
String tableName = column.getTable().getName();
String columnName = column.getName();
String alias = agg.getFunction().toLowerCase() + "_" + tableName + "_" + columnName;
Expression<?> aggregateExpr = null;

switch (agg.getFunction().toUpperCase()) {
    case "MAX":
        // Utiliser un template pour créer une expression numérique typée
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
        // Pour COUNT, on peut utiliser n'importe quel type d'expression
        aggregateExpr = SQLExpressions.count(
        		Expressions.numberTemplate(Double.class, tableName + "." + columnName)

        );
        break;
    default:
        throw new IllegalArgumentException("Unsupported aggregation function: " + agg.getFunction());
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
}