package com.example.demo.Querydsl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.requete.Requete;
import com.querydsl.core.Tuple;

@RestController
@RequestMapping("/api/query")
public class QueryController {
	
	
	private  DynamicQueryService dynamicQueryService;

    @Autowired
    public void DynamicQueryController(DynamicQueryService dynamicQueryService) {
        this.dynamicQueryService = dynamicQueryService;
    }

    @PostMapping("/fetch")
    public ResponseEntity<List<Map<String, Object>>> fetchTableData(@RequestBody Requete request) {
        List<Map<String, Object>> result = dynamicQueryService.fetchTableDataWithCondition(
                "jdbc:mysql://localhost:3306/youssef",
                request.getTable().getDatabase().getConnexion().getUsername(),
                request.getTable().getDatabase().getConnexion().getPassword(),
                "com.mysql.cj.jdbc.Driver",
                request.getTable(),
                null,
                request.getTable().getColumns()
        );

        return ResponseEntity.ok(result);
    }
  
}
