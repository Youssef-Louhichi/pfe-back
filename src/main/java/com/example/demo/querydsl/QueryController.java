package com.example.demo.querydsl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.DeleteRequestDTO;
import com.example.demo.dto.InsertRequestDTO;
import com.example.demo.dto.QueryRequestDTO;
import com.example.demo.dto.UpdateRequestDTO;


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
    
    
    @PostMapping("/insert")
    public ResponseEntity<Map<String, Object>> insertTableData(@RequestBody InsertRequestDTO request) {
        Long rowsAffected = dynamicQueryService.insertTableData(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("rowsAffected", rowsAffected);
        
        return ResponseEntity.ok(response);
    }
  
    
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> UpdateTableData(@RequestBody UpdateRequestDTO request) {
        Long rowsAffected = dynamicQueryService.updateTableDataWithJoins(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("rowsAffected", rowsAffected);
        
        return ResponseEntity.ok(response);
    }
    
    
    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> DeleteTableData(@RequestBody DeleteRequestDTO request) {
        Long rowsAffected = dynamicQueryService.deleteTableDataWithJoins(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("rowsdeleted", rowsAffected);
        
        return ResponseEntity.ok(response);
    }
  
}
