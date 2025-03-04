package com.example.demo.Querydsl;

import com.querydsl.sql.SQLQueryFactory;
import com.example.demo.TableColumns.ColumnRepository;
import com.example.demo.TableColumns.TabColumn;
import com.example.demo.tables.DbTable;
import com.querydsl.core.Tuple;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLQuery;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
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
    
    public DynamicQueryService(QueryDSLFactory queryDSLFactory) {
        this.queryDSLFactory = queryDSLFactory;
    }

    public List<Map<String, Object>> fetchTableDataWithCondition(
            String dbUrl, String username, String password, String driver,
            DbTable table, Map<String, Object> conditions, List<TabColumn> list) {

        System.out.println("Initializing database connection...");
        
        SQLQueryFactory queryFactory = queryDSLFactory.createSQLQueryFactory(dbUrl, username, password, driver);
        
        try (Connection conn = DriverManager.getConnection(dbUrl, username, password)) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, table.getName(), null);

            List<Expression<?>> columnExpressions = new ArrayList<>();
            List<String> columnNames = new ArrayList<>();
            
           // columnNames =  columnRepository.findC((long) table.getId());
            
            for (TabColumn l : list) {
                columnExpressions.add(Expressions.stringPath(l.getName()));
            }
            
            if (columnExpressions.isEmpty()) {
                throw new IllegalArgumentException("No columns found for table: " + table.getName());
            }

            // Define table reference
            RelationalPath<?> qTable = new RelationalPathBase<>(Object.class, table.getName(), null, "");

            // Execute Query
            List<Tuple> result = queryFactory
                    .select(columnExpressions.toArray(new Expression<?>[0])) 
                    .from(qTable)
                    .fetch();
            
            System.out.println("Query executed successfully. Rows fetched: " + result.size());
            System.out.println(result.toString());
            // Convert Tuple result to JSON-friendly List<Map<String, Object>>
            List<Map<String, Object>> jsonResponse = new ArrayList<>();
            
            for (Tuple tuple : result) {
            	System.out.println(tuple);
                Map<String, Object> row = new HashMap<>();
                for (int i = 0; i < list.size(); i++) {
                	System.out.println(list.get(i));
                    row.put(list.get(i).getName(), tuple.get(columnExpressions.get(i)));
                    
                }
                jsonResponse.add(row);
            }

            return jsonResponse;

        } catch (Exception e) {
            System.err.println("Error executing query: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList(); // Return an empty list instead of null
        }
    }




    private Predicate buildWhereClause(RelationalPathBase<?> table, Map<String, Object> conditions) {
        Predicate predicate = null;
        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            String columnName = entry.getKey();
            Object value = entry.getValue();

            // Dynamically determine column type
            if (value instanceof String) {
               // StringPath column = (StringPath) table.get(columnName);
                //predicate = (predicate == null) ? column.eq((String) value) : predicate.and(column.eq((String) value));
            } else if (value instanceof Integer || value instanceof Long) {
                //NumberPath<Number> column = (NumberPath<Number>) table.get(columnName);
                //predicate = (predicate == null) ? column.eq((Number) value) : predicate.and(column.eq((Number) value));
            } else if (value instanceof Boolean) {
             //   BooleanPath column = (BooleanPath) table.get(columnName);
               // predicate = (predicate == null) ? column.eq((Boolean) value) : predicate.and(column.eq((Boolean) value));
            } else {
                throw new IllegalArgumentException("Unsupported data type for column: " + columnName);
            }
        }
        return predicate;
    }
}