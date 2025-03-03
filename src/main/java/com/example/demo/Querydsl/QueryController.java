package com.example.demo.Querydsl;

import java.util.List;

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
    public ResponseEntity<List<Tuple>> fetchTableData(@RequestBody Requete request) {
        List<Tuple> result = dynamicQueryService.fetchTableDataWithCondition(
        		"jdbc:mysql://localhost:3306/youssef",
                request.getTable().getDatabase().getConnexion().getUsername(),
                request.getTable().getDatabase().getConnexion().getPassword(),
                "com.mysql.cj.jdbc.Driver",
                request.getTable(), null
                
        );
        return ResponseEntity.ok(result);
    }
}