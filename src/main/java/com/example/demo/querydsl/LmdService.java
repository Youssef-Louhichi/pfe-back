package com.example.demo.querydsl;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;

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

import com.example.demo.condition.FilterCondition;
import com.example.demo.condition.JoinCondition;
import com.example.demo.connexions.DatabaseType;
import com.example.demo.database.DatabaseRepository;
import com.example.demo.dto.DeleteRequestDTO;
import com.example.demo.dto.InsertRequestDTO;
import com.example.demo.dto.JoinRequestDTO;
import com.example.demo.dto.QueryRequestDTO;
import com.example.demo.dto.UpdateRequestDTO;
import com.example.demo.requete.RequeteRepository;
import com.example.demo.tablecolumns.ColumnRepository;
import com.example.demo.tablecolumns.TabColumn;
import com.example.demo.tables.DbTable;
import com.example.demo.tables.TableRepository;
import com.example.demo.users.UserRepository;

@Service
public class LmdService {


    private final QueryDSLFactory queryDSLFactory ;
    
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
    
    
    public LmdService(QueryDSLFactory queryDSLFactory) {
        this.queryDSLFactory = queryDSLFactory;
    }
	
    
    public List<Map<String, Object>> fetchTableDataWithConditionforlmd(QueryRequestDTO request) {
      	
        if (request.getTableId() == null || request.getTableId().isEmpty()) {
            throw new IllegalArgumentException("At least one table ID must be provided");
        }
        
        // Get primary table (first in the list)
        Long primaryTableId = request.getTableId().get(0);
        DbTable primaryTable = tableRepository.findById(primaryTableId)
                .orElseThrow(() -> new RuntimeException("Primary table not found with ID: " + primaryTableId));
        
        
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
                //addDynamicFilterWithSubquery(query, filter, tables, tableNameMap);
            } else {
                // Handle simple value comparison
                addSimpleFilter(query, filter, tables, tableNameMap);
            }
        }
    }
    
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
            //fetchRequest.setJoinConditions(joinRequest);
            
        }
        
        
        // Add the same filters as the update request
        fetchRequest.setFilters(request.getFilters());
        System.out.println("eeeeeeeeeeeeeeeeeeeeeeeee"+fetchRequest);
        // STEP 2: Fetch the records that will be updated
        List<Map<String, Object>> recordsToUpdate = fetchTableDataWithConditionforlmd(fetchRequest);
        
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
        List<Map<String, Object>> recordsToDelete = fetchTableDataWithConditionforlmd(fetchRequest);
        
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
    
	
}
