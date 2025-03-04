package com.example.demo.Querydsl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.QueryRequestDTO;


@RestController
@RequestMapping("/api/query")
public class QueryController {
	
	
	private  DynamicQueryService dynamicQueryService;

    @Autowired
    public void DynamicQueryController(DynamicQueryService dynamicQueryService) {
        this.dynamicQueryService = dynamicQueryService;
    }

    @PostMapping("/fetch")
    public ResponseEntity<List<Map<String, Object>>> fetchTableData(@RequestBody QueryRequestDTO request) {
    	
    
    	List<Map<String, Object>> result = dynamicQueryService.fetchTableDataWithCondition(request);
                

       

        return ResponseEntity.ok(result);
    }
  
}
