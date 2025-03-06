package com.example.demo.Querydsl;

import com.querydsl.sql.SQLQueryFactory;
import com.example.demo.Condition.FilterCondition;
import com.example.demo.TableColumns.ColumnRepository;

import com.example.demo.TableColumns.TabColumn;
import com.example.demo.dto.QueryRequestDTO;
import com.example.demo.tables.DbTable;
import com.example.demo.tables.TableRepository;
import com.querydsl.core.Tuple;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.RelationalPathBase;
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
    	
    	DbTable t = tableRepository.findById(request.getTableId()).get();
    	
    	String dbUrl = "jdbc:mysql://localhost:3306/" + t.getDatabase().getName(); 
    	String username = t.getDatabase().getConnexion().getUsername();
    	String password = t.getDatabase().getConnexion().getPassword();
    	String driver = "com.mysql.cj.jdbc.Driver";
        
        SQLQueryFactory queryFactory = queryDSLFactory.createSQLQueryFactory(dbUrl, username, password, driver);
             
        try (Connection conn = DriverManager.getConnection(dbUrl, username, password)) {
        	
        	List<TabColumn> list =  columnRepository.findAllById(request.getColumnId());
        	
            List<Expression<?>> columnExpressions = new ArrayList<>();
            for (TabColumn l : list) {
                columnExpressions.add(Expressions.stringPath(l.getName()));
            }
            
            if (columnExpressions.isEmpty()) {
                throw new IllegalArgumentException("No columns found for table: " + t.getName());
            }

            // Define table reference
            RelationalPath<?> qTable = new RelationalPathBase<>(Object.class, t.getName(), null, "");

            // Execute Query
            // Start building the query
            SQLQuery<Tuple> query = queryFactory.select(columnExpressions.toArray(new Expression<?>[0])).from(qTable);
          
            // Add dynamic WHERE conditions
            addDynamicFilters(query, request.getFilters());
            
            if (request.getGroupByColumns() != null && !request.getGroupByColumns().isEmpty()) {
                List<Expression<?>> groupByExpressions = new ArrayList<>();
                for (Long groupByColumnId : request.getGroupByColumns()) {
                    TabColumn groupByColumn = columnRepository.findById(groupByColumnId).orElse(null);
                    if (groupByColumn != null) {
                        groupByExpressions.add(Expressions.stringPath(groupByColumn.getName()));
                    }
                }
                if (!groupByExpressions.isEmpty()) {
                    query.groupBy(columnExpressions.toArray(new Expression<?>[0]));
                }
            }
            // Execute query
            List<Tuple> result = query.fetch();
            
            System.out.println("Query executed successfully. Rows fetched: " + result.size());
            System.out.println(result.toString());
            
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



}