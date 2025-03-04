package com.example.demo.Querydsl;

import com.querydsl.sql.SQLQueryFactory;
import com.example.demo.TableColumns.TabColumn;
import com.example.demo.tables.DbTable;
import com.querydsl.core.Tuple;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLQuery;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DynamicQueryService {

    private final QueryDSLFactory queryDSLFactory;

    public DynamicQueryService(QueryDSLFactory queryDSLFactory) {
        this.queryDSLFactory = queryDSLFactory;
    }

    public List<Map<String, Object>> fetchTableDataWithCondition(String dbUrl, String username, String password, String driver,
            String tableName, List<TabColumn> columnNames) {
    	
    	
        System.out.println("Initializing database connection...");
        System.out.println("DB URL: " + dbUrl);
        System.out.println("Username: " + username);
        System.out.println("Driver: " + driver);
        
        SQLQueryFactory queryFactory = queryDSLFactory.createSQLQueryFactory(dbUrl, username, password, driver);

        System.out.println("QueryFactory created successfully.");

        try {
            SQLQuery<Tuple> query = queryFactory.select(
                columnNames.stream()
                    .map(col -> Expressions.path(Object.class , col.getName()))
                    .toArray(Expression[]::new)
            ).from(Expressions.path(Object.class, tableName));

            System.out.println("Executing SQL: " + query.toString());

            List<Tuple> result = query.fetch();

            System.out.println("Query executed successfully. Rows fetched: " + result.size());
            
            
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (Tuple tuple : result) {
                Map<String, Object> rowMap = new HashMap<>();
                for (int i = 0; i < columnNames.size(); i++) {
                    rowMap.put(columnNames.get(i).getName(), tuple.get(i, null));
                }
                resultList.add(rowMap);
            }

            return resultList;
            
        } catch (Exception e) {
            System.err.println("Error executing query: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
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